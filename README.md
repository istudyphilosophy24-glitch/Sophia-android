# SOPHIA — Native Android Preview

This repository now contains **actual Kotlin + Jetpack Compose Android source**, no WebView. It is a **compilable preview candidate**, not the full SOPHIA 4.0 port. It contains 28 philosophy disciplines with a small, hand-authored introductory concept bank; the much larger SOPHIA 4 curriculum is not yet copied into this repository.

## Native preview features

- Bright gamified Android interface, 28 philosophical traditions, 12 practice checkpoints per discipline.
- Guided reading, eight-question recognition exercises, explanations, XP, coins, streaks, local completion records.
- Android system text-to-speech and a research notes journal.
- Offline, no internet permission or tracking. No premium neural voice included.

## Build a sideloadable APK

Use **Actions → Build SOPHIA Android**. On a successful run, open **Artifacts → SOPHIA-Native-Debug-APK**, download the artifact ZIP, unzip and install `app-debug.apk`.

For local builds use Android Studio (Java 17, Android SDK 35, Gradle 8.10.2). This repository intentionally relies on GitHub's Gradle setup action rather than checking in a binary wrapper JAR.

## Limitations / migration

This is a minimal initial GitHub version to validate a true native build. Advanced argumentative exercises, adaptive long-term mastery, AI Socratic dialogue, boss battles, the full 1,680-concept curriculum and reviewed primary texts are **not yet implemented in this branch**. The separate SOPHIA 4.0 WebView edition remains the fuller gameplay version.

App ID: `com.sophia.philosophy.nativepreview`; it **does not overwrite** an existing SOPHIA 4 installation. GitHub Actions creates a debug-signed testing APK (not a stable production signing identity). Do not commit release signing keys.
