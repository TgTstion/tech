# tech — Milestone 1 starter project (Kotlin + Jetpack Compose)

Three-screen UI scaffold for the *tech* contextual vocabulary app. AI generation and sentence
checking are simulated (`MockDeckGenerator`, `MockSentenceValidator`); everything else is real:
navigation, Explicit/Implicit Intents, accelerometer + proximity sensors, TextToSpeech and
Room CRUD.

## Open it in Android Studio

**File → Open** → select the `tech` folder (the one containing `settings.gradle.kts`) → wait for Gradle sync → Run ▶ on an emulator (API 26+).
If Studio offers to download SDK Platform 35 or to upgrade the Android Gradle Plugin, accept.

### Fallback: paste into a fresh project

1. **File → New → New Project → Empty Activity** (Compose).
   Name: `tech` · Package: `com.example.tech` · Minimum SDK: API 26 · Language: Kotlin · Build: Kotlin DSL.
2. Close Studio's generated `MainActivity.kt` and **delete** `app/src/main/java/com/example/tech/MainActivity.kt`
   and the generated `ui/theme` folder (this project provides its own).
3. Copy this project's `app/src/main/java/...`, `AndroidManifest.xml` and `res/values/*.xml` over the
   generated ones. **Keep** Studio's `res/mipmap-*` and `res/drawable` folders (launcher icon).
4. In the **root** `build.gradle.kts` add the KSP plugin (version must match your Kotlin version —
   see https://github.com/google/ksp/releases):
   `id("com.google.devtools.ksp") version "<kotlin-version>-<ksp-version>" apply false`
5. In `app/build.gradle.kts` add `id("com.google.devtools.ksp")` to `plugins` and copy the extra
   dependencies from this project's `app/build.gradle.kts` (lifecycle-viewmodel-compose,
   lifecycle-runtime-compose, material-icons-extended, Room + `ksp(room-compiler)`).
6. Sync Gradle and run on an emulator (API 26+).

The included Gradle files are a known-compatible set (AGP 8.7.3, Kotlin 2.0.21, KSP 2.0.21-1.0.28,
Compose BOM 2024.12.01, Room 2.6.1). If Studio created newer versions, keep Studio's and add only
what step 4–5 lists.

## Structure

```
app/src/main/java/com/example/tech/
├── ai/            DeckGenerator / SentenceValidator interfaces + mock implementations
├── data/          VocabCardEntity, VocabDao (CRUD), TechDatabase, VocabRepository
├── util/          IntentHelpers (all Intents), MotionSensors, Speaker (TTS), AppSettings
└── ui/
    ├── theme/      TechTheme (Material 3)
    ├── components/ FormalityChip, ContextBlock
    ├── hub/        Screen 3 — MainActivity (launcher), HubScreen, HubViewModel
    ├── generator/  Screen 1 — GeneratorActivity, GeneratorScreen, GeneratorViewModel
    └── practice/   Screen 2 — PracticeActivity, PracticeScreen, PracticeViewModel
```

## Where each marking point lives

| Requirement | File |
|---|---|
| Explicit Intent (Screen 3 → 1, Screen 3 → 2 with extras, Screen 1 → 2) | `util/IntentHelpers.kt` `openGenerator`, `openPractice` |
| Implicit Intent `ACTION_SEND` (share card / progress) | `util/IntentHelpers.kt` `shareText`, `shareCard` |
| Implicit Intent `ACTION_VIEW` (YouGlish) | `util/IntentHelpers.kt` `openYouGlish` |
| Slider 10/20/30 + checkbox batch list | `ui/generator/GeneratorScreen.kt` |
| Accelerometer tilt / shake | `util/MotionSensors.kt` `TiltShakeDetector`, `MotionControls` |
| Proximity → audio | `util/MotionSensors.kt` `RaiseToEar` + `util/Speaker.kt` |
| Room Create / Read / Update / Delete | `data/VocabDao.kt` |

## Testing sensors on the emulator

Emulator **⋮ → Extended controls → Virtual sensors**:
* **Device Pose / Accelerometer** — rotate around the Y axis (roll) right or left to tilt; drag quickly for a shake
  (or use the on-screen buttons/shuffle icon, which do the same thing).
* **Additional sensors → Proximity** — set to 0 cm to simulate holding the phone to your ear.
