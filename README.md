# SOPHIA 5.0 — The Illustrated Academy (corrected build)

A genuinely native Kotlin + Jetpack Compose Android game, created from the SOPHIA 4.0 curriculum and the 5.0 Android project. No WebView.

## Repaired in this build
- 28 courses, 280 units, 3,360 meaningful *stage-defined* lesson paths. Each 12-stage unit focuses on six successive concepts before building comparisons, arguments, criticism, applications and spiral review. These paths are algorithmically scaffolded, **not 3,360 individually written textbook chapters**.
- Eight multi-style exercises per ordinary lesson: concept recognition, explanation, contrast, application, logic validity, criticism, spaced recall and premise analysis. The upper tier requires a written reflection.
- Illustrated 4-phase boss arena with animated portrait, scene background, health meters, collectible premise sequence, critical shield and conditional victory. Not an ordinary Q&A screen.
- A 39-excerpt Great Library with genuine wording from historical Project Gutenberg editions in 13 works, metadata, translation credits, annotations, study prompts, glossary and TTS. Full texts are not bundled.
- A proper in-app Android TTS engine and voice selector, playback-speed and pitch sliders, preview and persistent preferences. ElevenReader is NOT assumed to be an Android TTS service. Text-file handoff allows import into a compatible reader, or copy/paste into ElevenReader.
- Bright rendered academy background, painterly philosopher portrait gallery, full graphic course cards, winding nodes, daily quests, XP, badges, streaks and animated rewards.

## Build
`.github/workflows/android.yml` builds `app-debug.apk`. Requires Android SDK 35, JDK17, Gradle8.10.2. No network permission used by the application. It will need a compatible Android system TTS service to speak locally.

## App ID and progress safety
Package is `com.sophia.philosophy.odyssey5`, intentionally different from prior debug-signed releases so installing will not destroy or conflict with your existing SOPHIA installation. Progress is stored separately. The production application ID and release signing should be stabilized before a later public release.

## Limitations
- Automatic written-answer AI semantic grading and richly animated interactive character rigs are not included. Boss uses a deterministic, four-stage graphical encounter with original portrait art and sound feedback.
- In-app TTS works with apps which register as Android TextToSpeech engines; ElevenReader may not. Android's Share chooser can send text to reader apps but cannot make ElevenReader speak inside SOPHIA without official provider support.
- The curated excerpts are historical/public-domain-in-US editions; check rights for your region. Quoted text comes from the original SOPHIA 5 APK's embedded Project Gutenberg editions; avoid mistaking them for modern translations.
- 3,360 stage-defined encounters still share a structured exercise generator; they are more distinct but not hand authored individually. Repeated concepts appear deliberately as retrieval practice.