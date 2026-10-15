import json,hashlib,re
from pathlib import Path
base=Path(__file__).resolve().parents[1]/'app/src/main/assets'
courses=json.loads((base/'curriculum.json').read_text(encoding='utf8'))
books=json.loads((base/'excerpts.json').read_text(encoding='utf8'))
assert len(courses)==30
assert len({c['id'] for c in courses})==30
assert 'positivism' in {c['id'] for c in courses}
assert 'schopenhauer' in {c['id'] for c in courses}
assert len({u['name'] for c in courses for u in c['units']})>275
assert all(len(c['units'])==10 for c in courses)
assert all(len(u['concepts'])==6 for c in courses for u in c['units'])
assert all(len(set(x['term'] for x in u['concepts']))==6 for c in courses for u in c['units'])
assert all(len(u['reading']['commentary'])>=6 for c in courses for u in c['units'])
assert all(len(u['reading']['commentary']['argument'])>100 for c in courses for u in c['units'])
assert all(len(u['reading']['thesis'])>15 for c in courses for u in c['units'])
assert len(books)==13
assert sum(len(b['excerpts']) for b in books)>=339
ids=[e['id'] for b in books for e in b['excerpts']]
assert len(ids)==len(set(ids))
passages=[e['passage'] for b in books for e in b['excerpts']]
assert len(passages)==len(set(passages))
for b in books:
 assert b['source'].strip() and b['translator'].strip() and 'gutenberg.org/ebooks/' in b['url']
 for e in b['excerpts']:
  assert 200<len(e['passage'])<1800,(b['title'],e['id'],len(e['passage']))
  assert len(e['commentary'])>250
  assert e['curation']['sourceSha1']==hashlib.sha1(e['passage'].encode('utf8')).hexdigest()
  assert e['location'] and e['question'] and e['glossary']
  assert not re.search(r'project gutenberg|end of ebook',e['passage'],re.I)
n=next(b for b in books if b['id']=='4363')['excerpts'][0]
assert n['location'].startswith('Part I, §1')
assert n['passage'].startswith('1. The Will to Truth,')
assert 'VALUE of this Will' in n['passage'] and '212.' not in n['passage']
assert n['passage'].endswith('no greater risk.')
print('PASS',len(courses),'courses;',sum(len(c['units']) for c in courses),'units;',sum(len(u['concepts']) for c in courses for u in c['units']),'concept entries;',len(passages),'source excerpts; Nietzsche §1 verified')
