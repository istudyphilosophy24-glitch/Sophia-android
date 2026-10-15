#!/usr/bin/env python3
"""SOPHIA 9: build the measured offline audio catalog from 13 genuine historic
Project Gutenberg texts archived in the SOPHIA 5 native GitHub Actions APK.
Never label the historical text as freshly written lecture content.
"""
import gzip, hashlib, json, re, sys, zipfile
from pathlib import Path
root=Path(__file__).resolve().parents[1]
assets=root/'app/src/main/assets'
courses=json.loads((assets/'curriculum.json').read_text())
books=json.loads((assets/'excerpts.json').read_text())
old=json.loads((assets/'audio_programs.json').read_text())
apk=Path(sys.argv[1])
if not apk.exists():raise RuntimeError('Historical text APK unavailable')
W=re.compile(r"\b[\w’'-]+\b",re.U)
def wc(t):return len(W.findall(t))
def clean(raw):
 a=re.search(r'\*\*\*\s*START OF (?:THE |THIS )?PROJECT GUTENBERG EBOOK[^\n]*\*\*\*',raw,re.I)
 b=re.search(r'\*\*\*\s*END OF (?:THE |THIS )?PROJECT GUTENBERG EBOOK[^\n]*\*\*\*',raw,re.I)
 if not a or not b or a.end()>=b.start():raise ValueError('Unverifiable Gutenberg delimiters')
 return raw[a.end():b.start()].strip(),raw[b.end():].strip()
def paragraphs(s):
 s=s.replace('\r\n','\n').replace('\r','\n')
 for p in re.split(r'\n\s*\n',s):
  p=re.sub(r'(?<=\w)-\n(?=\w)','',p)
  p=re.sub(r'(?<!\n)\n(?!\n)',' ',p)
  p=re.sub(r'\s+',' ',p).strip()
  if wc(p)<2:continue
  while wc(p)>1030:
   match=re.search(r'^.{1200,6000}?[.!?]\s',p)
   if match and wc(p[:match.end()])<=1030:
    cut=match.end()
   else:
    toks=p.split(' ')
    cut=len(' '.join(toks[:970]))
   yield p[:cut].strip()
   p=p[cut:].strip()
  if p:yield p
def chunks(s):
 result=[];batch=[];size=0
 for p in paragraphs(s):
  n=wc(p)
  if size>=700 and size+n>1030:
   result.append('\n\n'.join(batch));batch=[];size=0
  batch.append(p);size+=n
 if batch:result.append('\n\n'.join(batch))
 return result
items=[];payload=[];types={};sourceWords=0;digests={}
def add(o):
 o['wordCount']=sum(wc(x['body']) for x in o['chapters'])
 o['chapterCount']=len(o['chapters'])
 o['bundle']=len(items)//50
 items.append({k:o[k] for k in ['id','type','title','authors','question','tags','sources','related','wordCount','chapterCount','bundle']})
 payload.append(o)
 types[o['type']]=types.get(o['type'],0)+o['wordCount']
for p in old:add(p.copy())
for c in courses:
 for i,u in enumerate(c['units']):
  r=u['reading'];cc=u['concepts'];cid=re.sub('[^a-zA-Z0-9_-]','',c['id'])
  intro=f"Welcome to the Definitive Academy. Course: {c['name']}. Lecture {i+1}: {u['name']}. The central claim is: {r.get('thesis','')}. Consider why this argument matters."
  terms='\n\n'.join(f"{j+1}. {x['term']}: {x['definition']}. Contrast with {cc[(j+1)%len(cc)]['term']}." for j,x in enumerate(cc))
  end=f"Reflection: {r.get('objection','')}. Which premise is weakest? Which objection is strongest? What would justify revising the position?"
  add({'id':f'lecture9_{cid}_{i+1:02d}','type':'lecture','title':f"{c['name']}: {u['name']}",'authors':c['name'],
   'question':r.get('thesis','') or u['name'],'tags':[c['name'],u['name'],'lecture'],'sources':r.get('source','') or c['name'],
   'related':f'lecture9_{cid}_{i+2:02d}' if i<9 else '',
   'chapters':[{'name':'Opening','kind':'orientation','body':intro},{'name':'Foundation','kind':'exposition','body':r['foundation']},
    {'name':'Concepts','kind':'glossary','body':terms},{'name':'Interpretation','kind':'commentary','body':r['intermediate']},
    {'name':'Advanced argument','kind':'analysis','body':r['advanced']},{'name':'Critical reflection','kind':'reflection','body':end}]})
with zipfile.ZipFile(apk) as z:
 for b in books:
  id=str(b['id'])
  raw=z.read('assets/primary/'+id+'.txt').decode('utf8','replace')
  original,licenseText=clean(raw)
  if not (assets/'project_gutenberg_license.txt').exists():
   (assets/'project_gutenberg_license.txt').write_text(licenseText)
  pieces=chunks(original)
  sourceWords+=sum(wc(p) for p in pieces)
  digests[id]={'title':b['title'],'sourceWords':sum(wc(p) for p in pieces),'sourceSHA256':hashlib.sha256(original.encode()).hexdigest()}
  groups=[pieces[x:x+4] for x in range(0,len(pieces),4)]
  for i,group in enumerate(groups):
   title=f"{b['title']} — Close Reading {i+1:02d}"
   intro=f"Historical source study, session {i+1} of {len(groups)}: {b['title']} by {b['author']}, edition {b['translator']}. The following is primary text, followed by separate editorial questions. Listen for the central claim, its grounds, and its limitations."
   outro=f"End of historical reading session {i+1}. What did the passage argue? Which premises remain disputed? How would you assess a possible objection? Source: Project Gutenberg eBook {id}."
   add({'id':f'textseminar9_{id}_{i+1:03d}','type':'text-seminar','title':title,'authors':b['author']+' · '+b['translator'],
    'question':'What is established in this source reading, and what remains questionable?',
    'tags':[b['title'],b['author'],b['tradition'],'primary reading'],
    'sources':f"Project Gutenberg #{id} · {b['source']} · {b['translator']} · {b['url']}",
    'related':f'textseminar9_{id}_{i+2:03d}' if i+1<len(groups) else '',
    'chapters':[{'name':'Editorial orientation','kind':'orientation','body':intro}]+
      [{'name':f'Historical source · Part {j+1}','kind':'primary-source','body':t} for j,t in enumerate(group)]+
      [{'name':'Reflective questions','kind':'reflection','body':outro}]})
(assets/'audio_index_v9.json').write_text(json.dumps(items,ensure_ascii=False,separators=(',',':')))
folder=assets/'audio9';folder.mkdir(exist_ok=True)
for b in range((len(payload)+49)//50):
 with zipfile.ZipFile(folder/f'bundle_{b:02d}.zip','w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
  for item in payload[b*50:(b+1)*50]:
   archive.writestr(item['id']+'.json',json.dumps(item,ensure_ascii=False,separators=(',',':')))
total=sum(types.values());hours=total/125/60
report={'catalogCount':len(items),'totalWords':total,'totalHoursAt125WPM':round(hours,2),
        'totalHoursAt150WPM':round(total/150/60,2),'sourceWords':sourceWords,
        'authoredLectureAndSeminarHours':round((types.get('seminar',0)+types.get('lecture',0))/125/60,2),
        'typeWords':types,'books':digests,'measurement':'text-to-speech estimate, not recorded audio'}
(assets/'audio_report_v9.json').write_text(json.dumps(report,indent=2))
assert total>900000 and total/150/60>100,report
print('SOPHIA9 audio:',len(items),'programs',total,'spoken words',round(hours,1),'hours at 125 wpm')
