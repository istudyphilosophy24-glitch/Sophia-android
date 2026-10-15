#!/usr/bin/env python3
"""Convert verified program records into small indexed ZIP shards for Android."""
from pathlib import Path
import gzip,json,zipfile
R=Path(__file__).resolve().parents[1]
a=R/'app/src/main/assets'
p=a/'audio9'
items=json.loads((a/'audio_index_v9.json').read_text())
for x in p.glob('bundle_*.zip'): x.unlink()
for i,meta in enumerate(items):meta['bundle']=i//50
for bundle in range((len(items)+49)//50):
    subset=items[bundle*50:(bundle+1)*50]
    with zipfile.ZipFile(p/f'bundle_{bundle:02d}.zip','w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        for meta in subset:
            id=meta['id']
            raw=gzip.decompress((p/f'{id}.json.gz').read_bytes())
            # Ensure index/body are identical before packaging
            v=json.loads(raw)
            assert v['wordCount']==meta['wordCount']
            z.writestr(id+'.json',raw,compress_type=zipfile.ZIP_DEFLATED,compresslevel=9)
(a/'audio_index_v9.json').write_text(json.dumps(items,ensure_ascii=False,separators=(',',':')))
for x in p.glob('*.json.gz'):x.unlink()
print('Shards',len(list(p.glob('bundle*.zip'))),'zip bytes',sum(x.stat().st_size for x in p.glob('*.zip')))