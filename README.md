# SOPHIA 7.0 — The Dialectical Academy

Native Kotlin + Jetpack Compose educational Android game. **Offline-first**, with 30 philosophy courses, 300 units, 3,600 structured lesson paths and 420 edited historical excerpts. This is a genuine native iteration, not a WebView skin.

## Key changes from 6.0

1. **Four interactive challenge engines**: Concept Forge (term/definition association), Argument Architect (reorder premises/conclusion), Dialectical Tribunal (multi-step reason/objection/revision), and Interpretive Detective (source-vs-interpretation inference discipline). Lessons 6–11 of each chapter now route through these different interactions rather than repeating one quiz template.
2. **Dialectical Mastery Engine**: six distinct skill records, weighted performance updates, self-reported confidence support, transparent review intervals, weakest-skill guidance, diagnostic placement questions. These are *heuristics*, not validated psychometrics. Progress is tracked by concept *and* skill, not only by chapter.
3. **Boss 3.0**: philosopher-specific opening challenges for several traditions plus four visual phases, HP and mastery updates. Existing illustrated assets remain, not all 30 opponents have individually hand-drawn character art or 30 entirely hand-authored branching scripts.
4. **Socratic Chamber**: a branching offline examiner, written responses, objections and guided feedback. **No offline AI/LLM semantic grading is claimed.** The Doctoral Trial requires five long responses and checklist-based self-evaluation; completion is not academic certification.
5. **Living Knowledge Atlas**: curated, typed cross-course links (historical encounter, critique, thematic comparison). Links are not logical implications.
6. **Research Institute 2.0**: separate thesis, two premises, objection, response, and citation fields, visual scaffolding, version snapshots, compare/restore workflow, checklists; remains a research aid, not an automated logical proof checker.
7. **Great Reader 4.0**: user-imported alternative `.txt` edition/translation comparison, tappable glossary notes, argument annotations. The second translation is **not automatically bundled or text-aligned**; import only materials the user lawfully possesses.
8. **Radio 2.0**: new themed seminars combining opening, primary source, interpretation, counterpoint and reflection, in addition to the existing hands-free shuffled excerpt modes and Android media controls.
9. **Scholar collection and analytics**: unlockable course portraits through study rather than purchases; diagnostic assessments, mastery summaries and adaptive question ordering.

## Educational integrity

The 3,600 lesson *paths* combine curated definitions and variable activities. They are **not** 3,600 independently commissioned textbook chapters. Most chapter-specific commentary remains editorial teaching material and has not been certified through expert peer review. The excerpt library contains 420 selections taken from 13 historical editions, not 420 independently verified modern critical translations. Be mindful of jurisdiction-specific copyright rules.

AI free-text semantic grading, full multi-translation parallel critical apparatus, voice cloning/premium ElevenReader playback, and psychometrically validated adaptive difficulty **are not included**. Android's TTS engines work through their system APIs; ElevenReader can receive exported text but cannot be embedded as an internal TTS voice unless it implements an Android speech engine.

## Build & install

Github Actions (`.github/workflows/android.yml`) runs static content checks then `gradle :app:assembleDebug`, uploading the debug APK artifact. Min Android 7.0, target API 35. No internet permission or analytics. This debug package uses `com.sophia.philosophy.odyssey7`, so it installs alongside 6.0; **progress is not automatically migrated across the app ID**. Keep the older installation until you have backed up your data. Importing JSON backups requires compatible schemas; the earlier export format is preserved within this project.

Tests: `python3 tools/validate_content.py && python3 tools/validate_seven.py` followed by compilation. App functionality and background audio need physical Android hardware smoke tests in addition to a successful build.