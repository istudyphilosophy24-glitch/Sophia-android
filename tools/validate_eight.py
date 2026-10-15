import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
app=root/'app/src/main'
c=json.loads((app/'assets/curriculum.json').read_text())
e=json.loads((app/'assets/excerpts.json').read_text())
a=json.loads((app/'assets/audio_programs.json').read_text())
assert len(c)==30 and sum(len(x['units']) for x in c)==300
assert sum(len(x['concepts']) for x in c for x in x['units'])==1800
assert sum(len(x['excerpts']) for x in e)==420
assert len(a)==50 and sum(x['type']=='seminar' for x in a)==20 and sum(x['type']=='lecture' for x in a)==30
assert len({x['id'] for x in a})==50
assert all(len(x['chapters'])>=4 for x in a)
assert all(all(len(ch['body'])>=90 for ch in x['chapters']) for x in a)
kotlin='\n'.join(p.read_text() for p in (app/'java/com/sophia/nativeapp').glob('*.kt'))
assert 'ArgumentArchitect(' not in kotlin
assert 'DialecticalMiniGame7(' not in kotlin
assert 'ExerciseGame(' not in kotlin
assert 'GrandCampus8(' in kotlin and 'AudioHall8(' in kotlin
assert 'PhilosophyRadioService' in kotlin and 'MediaSession(' in kotlin
for file in ['grand_campus.jpg','grand_audio.jpg','grand_library.jpg','grand_odyssey.jpg']:
 assert (app/'res/drawable-nodpi'/file).exists()
print('PASS: 30 courses, 300 units, 1800 concepts, 420 excerpts, 20 seminars, 30 lectures, no mini-games, 8.0 scene assets')