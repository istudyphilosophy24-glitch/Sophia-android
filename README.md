# SOPHIA 8.0 — The Grand Academy

Native Android Kotlin / Jetpack Compose philosophy learning and listening application.

## The eighth-generation redesign

- **Four destination navigation**: immersive Campus, Audio Hall, Great Library, and Odyssey. Full-bleed original painted academy landscapes, premium dark-gold audio treatments, editorial serif typography, illustrated scholars, and a custom dock replace the inherited v5 menu-card home.
- **Removed mini-games and Argument Architect**. Standard comprehension checks, reflective study, the Socratic Chamber, the Doctoral Examination, scholarly commentary, and the illustrated culminating boss remain.
- **Audio Hall centerpiece**: 20 individually authored thematic seminar scripts (five chapters each), 30 course-specific capsule lectures (four chapters each), complete readable transcripts, indexable program selection, bookmarks, continuous philosophical radio, chapter jumps, saved progress, Android background playback/MediaSession transport controls, theme-following queue, playback rate and sleep timer.
- **30 disciplines**, **300 units**, **1,800 curriculum concepts**, **420 curated historical excerpts** retained from SOPHIA 7.
- **Great Reader**: historical edition credits, bookmarks, markup, user-imported translation comparison, reader TTS and lawful external-reader handoff retained; new illustrated author-browsing screen.
- **Mastery**: quiz records and concept-level mastery tracking retained. Progress uses same private SharedPreferences keys as earlier editions, although the package ID changes to avoid overwriting 7.

## Editorial limits

The initial twenty curated seminar **scripts are short-form chapters**, not twenty finished 20–60-minute audiobooks. Narration-time estimates are based on word count (~125 wpm) and omit pauses and optional source reading. This version adds the full reusable content and player framework for future 20–60-minute productions, with no invented audio files or prerecorded licensed voices. Similarly, the thirty course lectures are brief guided lessons derived from the existing chapter readings, not separately peer-reviewed university lectures. The commentary remains SOPHIA-authored instructional interpretation.

Audio uses Android's installed TTS engine. ElevenReader is accessible through exporting excerpts to a compatible external app, not embedded as an Android engine unless installed and exposed as one. Speech pause resumes the current ~2,300-character segment, not the exact phoneme. Thematically related programs are linked through editorial IDs rather than automatic claims of agreement between philosophical doctrines. Offline Android TTS availability depends on the installed engine and voices.

## Build

GitHub Actions with Android SDK 35, Gradle 8.10.2, AGP 8.7.3, Kotlin 2.1.20, JDK 17. Run `python3 tools/validate_eight.py` and `gradle :app:assembleDebug`.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`, Android >=7.0 (API 24), application ID `com.sophia.philosophy.grandacademy8`.

## Content roadmap

Next content packs: add longer editorial programs, improve primary-text location checks, align transcripts with generated speech output, extend audio listening history, and support optional pre-rendered offline licensed narration without falsely treating ElevenReader as a built-in TTS engine. Audio marks should distinguish estimate from media-duration.