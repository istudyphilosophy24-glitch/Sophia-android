#!/usr/bin/env python3
import collections,json,re,zipfile
from pathlib import Path
root=Path(__file__).resolve().parents[1]
a=root/'app/src/main/assets'
c=json.loads((a/'curriculum.json').read_text())
e=json.loads((a/'excerpts.json').read_text())
idx=json.loads((a/'audio_index_v9.json').read_text())
report=json.loads((a/'audio_report_v9.json').read_text())
assert len(c)==30 and sum(len(course['units']) for course in c)==300
assert sum(len(u['concepts']) for c0 in c for u in c0['units'])==1800
assert sum(len(b['excerpts']) for b in e)==420
assert len(idx)>=550 and len({x['id'] for x in idx})==len(idx)
types=collections.Counter(x['type'] for x in idx)
assert types['seminar']==20 and types['lecture']==330 and types['text-seminar']>=180,types
assert report['totalWords']==sum(x['wordCount'] for x in idx)
assert report['totalWords']/(150*60)>100
assert (a/'project_gutenberg_license.txt').stat().st_size>10000
W=re.compile(r"\b[\w’'-]+\b",re.U)
for meta in idx:
 f=a/'audio9'/f"bundle_{meta['bundle']:02d}.zip"
 assert f.exists(),f
 with zipfile.ZipFile(f) as z:program=json.loads(z.read(meta['id']+'.json'))
 assert program['id']==meta['id']
 assert len(program['chapters'])==meta['chapterCount'] and len(program['chapters'])>=3
 assert sum(len(W.findall(ch['body'])) for ch in program['chapters'])==meta['wordCount']
 assert all(ch['body'].strip() for ch in program['chapters'])
 assert not any('START OF THE PROJECT GUTENBERG EBOOK' in ch['body'] for ch in program['chapters'])
 if meta['type']=='text-seminar':
  assert any(ch['kind']=='primary-source' for ch in program['chapters'])
  assert 'Project Gutenberg' in meta['sources']
code='\n'.join(x.read_text() for x in (root/'app/src/main/java/com/sophia/nativeapp').glob('*.kt'))
assert 'ArgumentArchitect(' not in code and 'DialecticalMiniGame7(' not in code
assert 'AudioCatalog8.loadProgram' in code and 'ZipFile' in code and 'MediaSession' in code
print('PASS SOPHIA 9:',len(idx),'programs',report['totalWords'],'spoken words',
      '%.1f hours @ 125wpm' % (report['totalWords']/(125*60)),
      '%.1f hours @ 150wpm' % (report['totalWords']/(150*60)),dict(types))
