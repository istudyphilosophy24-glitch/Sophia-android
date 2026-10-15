import json
from pathlib import Path
base=Path(__file__).parents[1]/'app'/'src'/'main'/'assets'
courses=json.loads((base/'curriculum.json').read_text(encoding='utf8'))
books=json.loads((base/'excerpts.json').read_text(encoding='utf8'))
assert len(courses)==28, len(courses)
assert sum(len(c['units']) for c in courses)==280
for c in courses:
 assert len(c['units'])==10,(c['name'],len(c['units']))
 for u in c['units']:
  assert len(u['concepts'])==6,(c['name'],u['name'])
  assert len({x['term'] for x in u['concepts']})==6
  assert all(x['term'] and x['definition'] for x in u['concepts'])
assert len(books)==13
assert sum(len(b['excerpts']) for b in books)==39
seen=set()
for b in books:
 assert len(b['excerpts'])==3,(b['title'],len(b['excerpts']))
 for x in b['excerpts']:
  assert x['id'] not in seen;xid=x['id'];seen.add(xid)
  assert 90<len(x['passage'])<1250,(b['title'],x['title'],len(x['passage']))
  assert x['question'].endswith('?')
  assert len(x['glossary'])>=2
print('PASS: 28 courses, 280 units, 1680 source concepts, 39 distinct historical excerpts')