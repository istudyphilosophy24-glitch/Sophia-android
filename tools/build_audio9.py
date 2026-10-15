#!/usr/bin/env python3
"""Build the finale's offline, on-demand seminar catalog.
Source credits remain per-book. Listening estimates count unique spoken words
from actual scripts, not assigned duration metadata. Source-text seminar readings
are correctly classified, rather than called independently authored lectures.
"""
from __future__ import annotations
import gzip, hashlib, json, re, zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
OUT=ASSETS/'audio9';OUT.mkdir(parents=True,exist_ok=True)
CURRICULUM=json.loads((ASSETS/'curriculum.json').read_text(encoding='utf-8'))
LEGACY=json.loads((ASSETS/'audio_programs.json').read_text(encoding='utf-8'))
BOOKS=json.loads((ASSETS/'excerpts.json').read_text(encoding='utf-8'))
EARLIER=Path('/mnt/data/SOPHIA-5.0-Native-Android.apk')
if not EARLIER.exists():
    raise SystemExit('Original Gutenberg source archive not found; preserve provenance and do not silently substitute invented text')

W=re.compile(r"\b[\w’'-]+\b",re.UNICODE)
def words(s):return len(W.findall(s))
def norm(s):
    s=s.replace('\ufeff','').replace('\r\n','\n').replace('\r','\n')
    s=re.sub(r'(?<=\w)-\n(?=\w)','',s)
    # Project Gutenberg plain text: remove hard wrapping, preserve real paragraph gaps.
    return re.sub(r'[\t ]+',' ',re.sub(r'(?<!\n)\n(?!\n)',' ',s)).strip()
def clean_source(raw):
    # Use exactly the historical edition portion; omit Project Gutenberg licence blocks
    # and delivery metadata from spoken narration (retain provenance and links separately).
    start=re.search(r'\*\*\*\s*START OF (?:THE |THIS )?PROJECT GUTENBERG EBOOK[^\n]*\*\*',raw,re.I)
    end=re.search(r'\*\*\*\s*END OF (?:THE |THIS )?PROJECT GUTENBERG EBOOK[^\n]*\*\*',raw,re.I)
    if not start or not end or end.start()<=start.end():
        raise ValueError('Cannot reliably delimit Gutenberg source text')
    return raw[start.end():end.start()].strip()
def passages(s,min_words=730,max_words=1030):
    """Spoken chunks: retain paragraph boundaries, split only oversized blocks at sentences."""
    paras=[]
    for par in re.split(r'\n\s*\n',s):
        par=norm(par)
        if not par or words(par)<2:continue
        if words(par)>max_words:
            sentences=re.split(r'(?<=[.!?])\s+(?=[A-Z0-9\"“])',par)
            sub=[];n=0
            for sentence in sentences:
                if n+words(sentence)>max_words and sub:
                    paras.append(' '.join(sub));sub=[];n=0
                if words(sentence)>max_words:
                    tokens=sentence.split(' ')
                    for i in range(0,len(tokens),max_words):paras.append(' '.join(tokens[i:i+max_words]))
                else:sub.append(sentence);n+=words(sentence)
            if sub:paras.append(' '.join(sub))
        else:paras.append(par)
    chunks=[];group=[];count=0
    for par in paras:
        n=words(par)
        if count>=min_words and count+n>max_words:
            chunks.append('\n\n'.join(group));group=[];count=0
        group.append(par);count+=n
    if group:
        if chunks and count<280: chunks[-1]+='\n\n'+'\n\n'.join(group)
        else:chunks.append('\n\n'.join(group))
    return chunks

index=[];counts={};source_digests={};total_source_words=0

def save_program(p):
    assert p['id'].replace('_','').replace('-','').isalnum(),p['id']
    assert len(p['chapters'])>=3,p['id']
    p['wordCount']=sum(words(c['body']) for c in p['chapters'])
    p['chapterCount']=len(p['chapters'])
    fn=OUT/(p['id']+'.json.gz')
    fn.write_bytes(gzip.compress(json.dumps(p,ensure_ascii=False,separators=(',',':')).encode('utf-8'),compresslevel=9,mtime=0))
    meta={key:p[key] for key in ('id','type','title','authors','question','tags','sources','related','wordCount','chapterCount')}
    index.append(meta)
    counts[p['type']]=counts.get(p['type'],0)+p['wordCount']

for p in LEGACY:
    save_program(p.copy())

# Each unit becomes an authored-by-SOPHIA short guided lecture, with its existing
# historical unit material and specific concepts rather than an empty time allocation.
for course in CURRICULUM:
    for ui,u in enumerate(course['units']):
        rd=u['reading'];concepts=u['concepts'];courseid=re.sub(r'[^A-Za-z0-9_-]','',course['id'])
        th=rd.get('thesis','').strip()
        objection=rd.get('objection','').strip()
        opening=(f"Welcome to the Definitive Academy. This is lecture {ui+1} of ten in {course['name']}. "
                 f"Our topic is {u['name']}. First, identify the question this topic answers. "
                 f"Listen for how the principal distinctions change the possibilities of an argument. "
                 f"The central working thesis is: {th or 'the concepts in this chapter require careful justification.'}")
        concepts_body='\n\n'.join(f"{i+1}. {c['term']}. {c['definition']}. "
                       f"How is this different from {concepts[(i+1)%len(concepts)]['term']}?"
                       for i,c in enumerate(concepts))
        ending=(f"Our closing question for {u['name']}: {objection or 'What alternative explanation might challenge the claim?'} "
                f"Return to {concepts[0]['term']} and {concepts[-1]['term']}. "
                "Formulate one premise you could defend, one objection you would take seriously, "
                "and the evidence or reasoning that might lead you to revise your conclusion. "
                "This is an invitation to inquiry, not a claim that there is only one possible reading.")
        p={'id':f'lecture9_{courseid}_{ui+1:02d}','type':'lecture',
            'title':f"{course['name']}: {u['name']}", 'authors':course['name'],
            'question': th or f'What problem does {u["name"]} seek to clarify?',
            'tags':[course['name'],u['name'],'course lecture'],
            'sources':rd.get('source','') or course['name']+' curriculum (SOPHIA instructional exposition)',
            'chapters':[
                {'name':'Opening philosophical problem','kind':'orientation','body':opening},
                {'name':'Foundation','kind':'exposition','body':rd.get('foundation','')},
                {'name':'Concepts and distinctions','kind':'glossary','body':concepts_body},
                {'name':'Interpretive development','kind':'commentary','body':rd.get('intermediate','')},
                {'name':'Advanced arguments','kind':'reconstruction','body':rd.get('advanced','')},
                {'name':'Objections and independent thought','kind':'reflection','body':ending},
            ],'related':f'lecture9_{courseid}_{ui+2:02d}' if ui<9 else ''}
        save_program(p)

# Historical primary-source listening seminars: substantial guided close-reading
# with authentic, continuous text from an identifiable public-domain edition.
# Great Library itself remains strictly excerpt-based, as requested since v5.
with zipfile.ZipFile(EARLIER) as z:
    for b in BOOKS:
        bid=str(b['id']);pname=f'assets/primary/{bid}.txt'
        if pname not in z.namelist():raise ValueError(f'Missing source {pname}')
        raw=z.read(pname).decode('utf-8',errors='replace')
        source=clean_source(raw);raw_chunks=passages(source)
        source_digests[bid]={
            'title':b['title'],'author':b['author'],'translator':b['translator'],
            'sha256_cleaned_source':hashlib.sha256(source.encode()).hexdigest(),
            'words':sum(words(x) for x in raw_chunks),'sessions':0}
        # Each session plays approximately half an hour at 125 words/minute,
        # with breaks at paragraph boundaries and reflective framing.
        sessions=[]
        for i in range(0,len(raw_chunks),4):sessions.append(raw_chunks[i:i+4])
        source_digests[bid]['sessions']=len(sessions)
        total_source_words+=sum(words(x) for x in raw_chunks)
        for si,group in enumerate(sessions):
            ident=f'textseminar9_{bid}_{si+1:03d}'
            prev=f'textseminar9_{bid}_{si+2:03d}' if si+1<len(sessions) else ''
            title=f"{b['title']} — Close Reading {si+1:02d}"
            intro=(f"Welcome to the Great Works Listening Series. "
                   f"This is session {si+1} of {len(sessions)} exploring {b['title']} by {b['author']}, "
                   f"in the {b['translator']} edition. "
                   "You will hear a continuous portion of the historical source text, "
                   "followed by questions to support careful reading. "
                   "The commentary is editorial guidance from SOPHIA, not the philosopher's own voice. "
                   "Listen first for the argument's claim; then distinguish its evidence, assumptions, and consequences.")
            outro=(f"End of listening session {si+1} in {b['title']}. "
                   "What was the main conclusion or insight? Can you reconstruct the reasons given, "
                   "and distinguish any stronger and weaker interpretations? "
                   "Where might a sympathetic critic press an objection? "
                   f"This historical text is drawn from the Project Gutenberg edition, eBook {bid}. "
                   "Source text and interpretive study notes are deliberately separated.")
            chapters=[{'name':'Textual orientation','kind':'orientation','body':intro}]
            chapters.extend({'name':f'Historical source · Part {j+1}','kind':'primary-source','body':segment} for j,segment in enumerate(group))
            chapters.append({'name':'Reflective seminar questions','kind':'reflection','body':outro})
            save_program({'id':ident,'type':'text-seminar','title':title,'authors':f"{b['author']} · {b['translator']}",
                 'question':f'What does the argument in session {si+1} establish, and what remains contestable?',
                 'tags':[b['tradition'],b['author'],b['title'],'primary source','close reading'],
                 'sources': f"Project Gutenberg #{bid} · {b['source']} · {b['translator']} · {b['url']}",
                 'chapters':chapters,'related':prev})

index_path=ASSETS/'audio_index_v9.json'
index_path.write_text(json.dumps(index,ensure_ascii=False,separators=(',',':')),encoding='utf-8')
# Report uses actual spoken word counts (including minimal session orientation and
# reflection), at 125 wpm, rather than false nominal episode runtime estimates.
report={'catalogCount':len(index),'typeCount':{t:sum(p['type']==t for p in index) for t in sorted(counts)},
        'typeWords':counts,'totalWords':sum(counts.values()),'totalHoursAt125WPM':round(sum(counts.values())/125/60,2),
        'sourceReadingsWords':total_source_words,'lecturesPlusAuthoredSeminarsHours':round((counts.get('lecture',0)+counts.get('seminar',0))/125/60,2),
        'sources':source_digests,'timingAssumption':'Estimated TTS duration at 125 spoken words per minute; not prerecorded audio or precise device playback runtime',
        'method':'Lectures and authored thematic seminars are distinct from lengthy guided historical-source text seminars.'}
(ASSETS/'audio_report_v9.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('Catalog',report['catalogCount'],'categories',report['typeCount'])
print('Total spoken words',report['totalWords'],'hours @ 125 wpm',report['totalHoursAt125WPM'])
print('Authored lecture + thematic hours',report['lecturesPlusAuthoredSeminarsHours'])
print('Historical source words',total_source_words,'/ source hours',round(total_source_words/125/60,1))
if report['totalHoursAt125WPM']<100:raise AssertionError('100-hour floor not met')