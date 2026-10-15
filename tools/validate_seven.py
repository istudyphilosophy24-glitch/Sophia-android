"""Static content and integration tests in addition to Kotlin compilation."""
import json, re
from pathlib import Path
base=Path(__file__).resolve().parents[1]
java=base/'app/src/main/java/com/sophia/nativeapp'
courses=json.loads((base/'app/src/main/assets/curriculum.json').read_text())
books=json.loads((base/'app/src/main/assets/excerpts.json').read_text())
assert len(courses)==30 and sum(len(x['units']) for x in courses)==300
assert sum(len(b['excerpts']) for b in books)==420
assert len({e['id'] for b in books for e in b['excerpts']})==420
atlas=(java/'KnowledgeAtlas7.kt').read_text()
names={c['name'] for c in courses}
links=re.findall(r'AtlasEdge7\("([^"]+)","([^"]+)"',atlas)
assert len(links)>=20
assert all(a in names and b in names for a,b in links),set(v for a,b in links for v in (a,b))-names
main=(java/'MainActivity.kt').read_text()
for screen in ('MasteryDashboard7','SocraticChamber7','DoctoralExamination7','KnowledgeAtlas7','ResearchLab7','DialecticalMiniGame7','PlacementTrial7','ScholarCollection7'):
 assert screen+'(' in main,screen
mini=(java/'DialecticalGames7.kt').read_text()
assert all(x in mini for x in ['ConceptForge7','ArgumentArchitect7','DialecticalTribunal7','InterpretiveDetective7'])
assert len(re.findall(r'ArgumentPuzzle7\(',mini))>=6
radio=(java/'PhilosophicalRadio.kt').read_text()
assert 'mode=="program"' in radio and 'Thematic Audio Seminar' in radio
assert 'progress' in radio and 'loadPrimaryBooks' in radio
mastery=(java/'Mastery7.kt').read_text()
assert all(x in mastery for x in ('record(', 'dueConcepts','summary(','difficultConcepts'))
reader=(java/'PrimaryLibrary.kt').read_text()
assert 'parallelText' in reader and 'argumentNote' in reader and 'ActivityResultContracts.OpenDocument' in reader
print('PASS: thirty courses, 420 excerpt records, curated links, four games, mastery ledger, audio seminar, reading enhancements')