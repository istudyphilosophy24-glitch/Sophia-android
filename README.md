# SOPHIA 6.0 — The Living Academy

A **native Kotlin + Jetpack Compose** offline philosophy-learning Android app. This release expands the educational content while retaining the 5.0 illustrated game world and boss arena.

## What's new

- **Exactly 30 courses** (Positivism and Schopenhauerian Philosophy are new), ten units per course, six defined concepts per unit, and twelve learning stages per unit (3,600 lesson checkpoints).
- **300 structured chapter commentaries** distinguishing textual orientation, close reading, argument reconstruction, philological concerns, interpretive controversy and critical evaluation. Editorial study notes are not misrepresented as verbatim quotations or peer-reviewed scholarship. The two new courses contain chapter-specific expositions and carefully delineated objections.
- **420 primary-source excerpts across 13 historical works**, including 381 additional selections from previously acquired Project Gutenberg editions. Passages are derived from complete source paragraphs, with source identifiers and declared translation credits. Do not regard a source-paragraph ordinal as a printed edition’s section or page number. Individual selections other than Nietzsche Part I §1 have not been hand-checked against printed critical editions.
- **Nietzsche repair:** *Beyond Good and Evil*, excerpt 1 now contains Part I §1, “The Will to Truth”, instead of an awkward fragment assembled from later §212.
- **Philosophical Radio:** true hands-free shuffled TTS, automatic item advance, excerpt/commentary alternation, study selection, favorite selections, 15/30/60 minute sleep timer or endless listening, and media notification/lockscreen controls. Runs as a foreground audio service independent of the activity UI.
- **Saved Anthology:** bookmark source excerpts, search the Great Library, and listen to your saved selections.
- **Concept constellations:** interactive six-node concept diagrams per chapter.
- **Dialectical Duels 2.0:** the illustrated four-phase boss arena now shuffles substantive alternative philosophical moves and preserves correct-answer identity.
- **Daily Discovery:** highlights a different primary excerpt each day.
- **Scholar's Notebook:** existing research desk and on-device annotations remain available, along with comparative commentary views.
- **Narrator choices:** choose Android TTS engine/voice within the app; ElevenReader can receive exported text, but cannot be treated as a native engine unless installed as an Android TTS service.

## Sources and editorial integrity

The excerpt data was assembled **offline** from 13 Project Gutenberg eBook texts that had already been bundled in the earlier SOPHIA 5.0 APK (see `tools/build_excerpts.py`). Project Gutenberg is not being scraped at runtime. Bibliographic catalog URLs remain in each work's record.

The primary passages are source text; the automatically assembled guided analyses are original SOPHIA content and **have not all been independently peer-reviewed**. The passage source hash and source paragraph ordinal are recorded so material can be editorially audited. For countries outside the USA, check public-domain status of the relevant editions/translations independently.

The course content includes earlier structured texts and explicitly authored new Positivism/Schopenhauer units. **3,600 lesson checkpoints** does not mean 3,600 unique authored textbook chapters or 3,600 independently evaluated essays.

## Android implementation

- Kotlin and Jetpack Compose, Android 7.0+ (minSdk 24), target SDK 35.
- Service `PhilosophyRadioService` with Android MediaSession notification controls, foreground type `mediaPlayback`, and Android TextToSpeech utterance callbacks.
- `POST_NOTIFICATIONS` requested on supported Android versions when starting radio; playback is still possible without permission but lockscreen controls may be less visible.
- No network permission, analytics, ads or account required. **No proprietary neural voice model** bundled; naturalness depends on the installed Android voices.
- Package `com.sophia.philosophy.odyssey6` installs separately from SOPHIA 5.0 to avoid debug-signing conflicts and preserve the older installation. **Progress is not transferred automatically.** You can manually export/import a compatible progress backup from within the app.

## Build

GitHub Actions workflow `.github/workflows/android.yml`: validates content and builds `:app:assembleDebug`, then uploads a sideloadable debug APK artifact. Build is debug-signed for personal testing, not a Play Store distribution.

## Remaining limitations

SOPHIA Radio background audio is based on the chosen Android TTS engine and has not been hardware-tested on every device, OEM battery-management implementation or voice provider. The automatic commentary pipeline is text-sensitive but **not equivalent to an academic critical edition with human-reviewed commentary on each of 420 excerpts**. Advanced natural-language examination still relies on user reflection instead of independent AI grading.
