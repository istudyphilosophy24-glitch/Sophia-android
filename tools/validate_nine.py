"""Definitive edition content/rights/feature regression checks."""
from pathlib import Path
import collections,json,re,sys,zipfile
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
idx=json.loads((ASSETS/'audio_index_v9.json').read_text())
report=json.loads((ASSETS/'audio_report_v9.json').read_text())
assert len(idx)>=580 and len({x['id'] for x in idx})==len(idx)
assert set(x['type'] for x in idx)=={'seminar','lecture','text-seminar'}
assert sum(x['type']=='text-seminar' for x in idx)>=200
assert sum(x['type']=='lecture' for x in idx)>=330
assert sum(x['type']=='seminar' for x in idx)==20
assert report['totalWords']==sum(x['wordCount'] for x in idx)
assert report['totalWords']>=100*150*60, '100 hours should remain at a brisk 150 wpm'
assert sum(x['type']=='text-seminar' and x['sources'].startswith('Project Gutenberg') for x in idx)>=200
assert (ASSETS/'project_gutenberg_license.txt').stat().st_size>10000
assert len(json.loads((ASSETS/'curriculum.json').read_text()))==30
assert sum(len(b['excerpts']) for b in json.loads((ASSETS/'excerpts.json').read_text()))==420
kotlin_path=ROOT/'app/src/main/java/com/sophia/nativeapp'
code='\n'.join(f.read_text() for f in kotlin_path.glob('*.kt'))
assert 'GrandCampus8' in code and 'AudioHall8' in code
assert 'ArgumentArchitect(' not in code and 'DialecticalMiniGame7(' not in code
assert 'AudioCatalog8.loadProgram' in code and 'ZipFile' in code
assert 'MediaSession' in code and 'speechChunks' in code
counts=collections.Counter()
for i,item in enumerate(idx):
    filepath=ASSETS/'audio9'/f"bundle_{item['bundle']:02d}.zip"
    assert filepath.exists(),filepath
    with zipfile.ZipFile(filepath) as z:
        program=json.loads(z.read(item['id']+'.json'))
    assert program['id']==item['id']
    assert len(program['chapters'])==item['chapterCount'] and item['chapterCount']>=3
    measured=sum(len(re.findall(r"\b[\w’'-]+\b",chapter['body'])) for chapter in program['chapters'])
    assert measured==item['wordCount'], (item['id'],measured,item['wordCount'])
    assert all(ch['body'].strip() for ch in program['chapters'])
    assert not any('START OF THE PROJECT GUTENBERG EBOOK' in c['body'] or
        'END OF THE PROJECT GUTENBERG EBOOK' in c['body'] for c in program['chapters'])
    if item['type']=='text-seminar':
        assert any(ch['kind']=='primary-source' for ch in program['chapters'])
        assert program['sources'] and 'Project Gutenberg' in program['sources']
    counts[item['type']]+=1
print('PASS: 9.0 | %s programs | %s spoken words | %.2f h @ 125 wpm, %.2f h @ 150 wpm' % (
   len(idx), report['totalWords'],report['totalWords']/(125*60),report['totalWords']/(150*60)))
print('BREAKDOWN:',dict(counts),'authored hours=',report['lecturesPlusAuthoredSeminarsHours'],
      'primary source readings=',round(report['sourceReadingsWords']/125/60,2),'hours')