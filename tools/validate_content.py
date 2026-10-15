import json
from pathlib import Path
folder=Path(__file__).resolve().parents[1]/"app"/"src"/"main"/"assets"
courses=json.loads((folder/"curriculum.json").read_text(encoding="utf-8"))
books=json.loads((folder/"excerpts.json").read_text(encoding="utf-8"))
assert len(courses)==28
assert sum(len(c["units"]) for c in courses)==280
assert all(len(unit["concepts"])==6 for course in courses for unit in course["units"])
assert len(books)==13
assert sum(len(book["excerpts"]) for book in books)==39
for book in books:
 for item in book["excerpts"]:
  assert item["passage"].strip() and item["question"] and item["glossary"]
print("PASS: 28 courses, 280 units, 1680 concepts, 39 curated excerpts")
