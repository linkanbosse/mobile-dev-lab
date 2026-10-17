# mobile-dev-lab

A minimal native Android app (Java + XML) built entirely in the cloud with GitHub Actions.

- Package: `se.mobiledev.lab`
- minSdk 23, targetSdk/compileSdk 36
- Android Gradle Plugin 9.4.1, Gradle 9.8.1, JDK 17

## Build

Every push to `main` (or a manual run via **Actions → Android APK Build → Run workflow**) will:

1. Install the Android SDK components (platform 36, build-tools 36.0.0).
2. Generate the Gradle wrapper (it is not committed).
3. Run `lintDebug` and `testDebugUnitTest`.
4. Build `assembleDebug`.

## Download the APK

Open the latest successful workflow run under **Actions** and download the
`mobile-dev-lab-debug-apk` artifact (kept for 7 days). It is a debug build
signed with the default debug key.

## Project layout

```
app/src/main/java/se/mobiledev/lab/MainActivity.java   UI logic
app/src/main/res/                                     layouts, strings, colors, theme
app/src/test/java/se/mobiledev/lab/                   JUnit tests
.github/workflows/android-build.yml                    CI build
```

Never commit keystores, passwords or tokens. `.gitignore` excludes `*.jks`,
`*.keystore` and `keystore.properties`.
