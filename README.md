# SOPHIA 4.0 — Native Jetpack Compose Migration

**Native Kotlin UI, no Android WebView.** This repository migrates SOPHIA 4.0's complete **28-course, 280-unit, 1,680-concept** curriculum (including historical readings) into an offline Android app. Twelve lesson paths per unit provide 3,360 checkpoints.

## Implemented

- Three connected areas: Odyssey (courses and unit paths), Academy (three reading depths, annotations, Socratic seminar, logic arena, mastery dashboard, spaced review), and Research Institute (persistent writing and audit rubric).
- Eight assessed questions per lesson across identification, comprehension, transfer, formal inference, philosophical criticism and earlier-content recall; a written reflection at upper tiers.
- Mastery trials require 6 correct answers out of 8; ordinary practice has unlimited retries. Streaks, XP, Wisdom Coins and badges are device-local.
- Six skill signals (recognition, comprehension, reconstruction, criticism, synthesis, retention) and a simple review schedule.
- Android TextToSpeech with speed and pitch controls, haptic response and synthesized feedback tones. No bundled high-quality neural audio.
- Native backup export through Android Sharesheet and paste-based restore of **native** progress and saved research/annotations.

## Build

GitHub Actions automatically runs the debug Android build. Download artifact `SOPHIA-4-Native-Migrated-APK`, extract `app-debug.apk`, sideload on an Android 7+ device. Uses Gradle 8.10.2, Android SDK 35, Kotlin 2.1.20 and Compose BOM 2025.04.01.

## Important limitations

- The complete *curriculum* is migrated, but exercises are still algorithmic variants of 1,680 concepts—not 26,880 hand-written scholarly questions. The 4.0 source often uses simplified expositions; some older units have less detailed advanced readings.
- The Socratic seminar is a guided writing exercise, **not** an LLM examiner. No AI-based grading, premium neural speech, or valid proof-of-truth certification. The finite formal logic arena uses four patterns.
- This app is intentionally a separate native app ID (`com.sophia.philosophy.nativepreview`), updating the previous Compose *preview* only. It does **not** automatically read progress from WebView SOPHIA 4.0 (`com.sophia.philosophy`). The backup export/import format is native-specific.
- GitHub Actions APKs are debug-signed. For stable long-term releases, configure a controlled release keystore outside the repository.
- The bundled primary-text citations are source orientations; translation and textual attributions need independent edition checks.
- A browser-free, signed debug build does not guarantee flawless behavior on every phone. Test on device.

## Privacy

No advertising, telemetry, account sign-in or internet permission. This is a personal offline application.