# SOPHIA 9.0 — The Definitive Academy

**The final numbered SOPHIA Android release.** Native Kotlin + Jetpack Compose, offline-first, no advertisements, no mini-games, no Argument Architect.

## The listening library: measured, not guessed

SOPHIA 9 ships **588 audio-ready programs** in a source-indexed, compressed, on-demand catalog:

- **20 authored thematic philosophical seminars**, preserved and searchable from 8.0.
- **330 SOPHIA course lectures** (30 original topical introductions plus 300 new unit-level lessons with foundations, conceptual explanations, advanced exposition, and critical reflections).
- **238 guided primary-text listening seminars** built from 13 historical Project Gutenberg editions. Each is roughly 25–40 minutes of continuous historical source narration with introductory context and separate SOPHIA reflection prompts. Related sessions play in sequence.

Across these programs are **1,067,845 spoken words**. The complete catalog estimates **142.38 hours at 125 spoken words per minute**, or **118.65 hours at 150 spoken words per minute**. Crucially, **only ~18.94 hours at 125 wpm comprise authored thematic seminars and course lecture material**; most of the remaining listening time is **historical-source readings within guided textual seminars**. That distinction is made explicit in the interface and test report. The content is **text synthesized by installed Android TTS at playback time**, not 142 hours of prerecorded, professionally produced audio. Actual listening time depends on voice, pace, and pause settings.

The scholarly/commentary material is SOPHIA's original educational exposition, **not peer reviewed**. Historical works should be checked against the displayed bibliographic record. The Great Library remains **420 curated excerpts**, not the 13 full books; longer source passages are confined to optional listening sessions.

## Final-release features

- **The Campus:** full-bleed illustrated home, learning progress, listening entry point.
- **The Audio Hall:** four source-based categories (thematic seminars, course lectures, textual study seminars, saved programs), continuous philosophical radio, searchable catalog, transcripts, episode chapters, resumable segments, MediaSession notification and lock-screen controls, voice rate, sleep timer, and linked sequels.
- **The Great Library:** 420 excerpt cards with attribution and reading questions, custom typography, bookmarks, notes, and licensed/user-supplied translation comparison.
- **The Odyssey:** 30 courses, 300 chapters, 1,800 concept entries, 3,600 lesson paths and traditional philosophical examinations, not mini-games.
- **The Scholar's Quarters:** mastery review, guided Socratic dialogue, doctoral examination, research notebook, argument annotations, exportable/importable learning backup.
- **Reliability:** content integrity validation verifies 588 zipped scripts, real speech length, absence of Gutenberg boilerplate in narration, and intact curriculum/library counts.

## Historical-source provenance and rights

The 13 historical texts were transferred from original Gutenberg text assets in an earlier SOPHIA build (rather than scraped or fabricated), and the edition catalog records Project Gutenberg IDs and translator credits. `app/src/main/assets/project_gutenberg_license.txt` preserves the Gutenberg legal terms. **Copyright status outside the United States may differ**; readers should check their jurisdiction before redistribution. No licensed modern translations or proprietary ElevenReader narration are embedded.

## Architecture

`app/src/main/assets/audio_index_v9.json` lists only program metadata. The scripts reside in **12 ZIP shards** under `app/src/main/assets/audio9/`, copied on demand to the app's private cache. The player opens just the requested ZIP member, avoiding the expense of parsing millions of characters on the UI thread. Episode narration divides long chapters into speech segments below Android's per-utterance limit; playback position is saved at chapter and segment boundaries. Background playback uses Android foreground media service / MediaSession and installed TTS voices.

This release has a unique package ID `com.sophia.philosophy.definitive9` because prior GitHub debug signing keys may differ. It installs alongside version 8. **Earlier app data is not automatically migrated**, but compatible SOPHIA-native backups can be restored in Settings. There is no direct ElevenReader engine control; historical excerpts can still be shared through the Android chooser.

## Build

JDK 17; Android SDK 35; Gradle 8.10.2; AGP 8.7.3; Kotlin 2.1.20. Run:

```sh
python3 tools/validate_content.py
python3 tools/validate_eight.py
python3 tools/validate_nine.py
gradle :app:assembleDebug --no-daemon
```

The GitHub Actions workflow runs those tests before packaging the sideloadable debug APK. Source-level and APK-level tests do **not** constitute physical-device validation. Playback should be tested with the target Android TTS engine, locked screen, and battery management settings.

## Final-edition maintenance priorities

Future content can be delivered as small, optional source-verified packs without releasing SOPHIA 10.0. The highest-value work thereafter is text accuracy, better thematic editorial commentary, accessibility, speech-engine compatibility, and security maintenance. Adding arbitrary courses or nominal lecture durations is expressly disfavored.