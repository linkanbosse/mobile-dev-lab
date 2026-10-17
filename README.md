# mobile-dev-lab

A minimal native Android app (Java + XML) built entirely in the cloud with GitHub Actions.

- Package: `se.mobiledev.lab`
- minSdk 23, targetSdk/compileSdk 36
- Android Gradle Plugin 9.4.1, Gradle 9.8.1, JDK 17

## Build

The workflow runs on every push to `main`, on every pull request into `main`,
and on manual runs via **Actions → Android APK Build → Run workflow**. Each run will:

1. Install the Android SDK components (platform 36, build-tools 36.0.0).
2. Validate the committed Gradle wrapper (done by `setup-gradle`).
3. Run `lintDebug` and `testDebugUnitTest`.
4. Build `assembleDebug`.

Manual runs accept a `warning_mode` input (`summary`, `all`, `fail`, `none`)
that is passed to Gradle's `--warning-mode`, e.g.:

```
gh workflow run android-build.yml -f warning_mode=all
```

All actions are pinned to full commit SHAs, with the version in a comment.

## Dependency updates

Dependabot (`.github/dependabot.yml`) checks weekly and opens one grouped PR per
ecosystem:

- **github-actions**: updates the pinned SHAs and their version comments.
- **gradle**: updates the Android Gradle Plugin, library dependencies and the
  committed Gradle wrapper.

Each Dependabot PR is built by the workflow above and can only be merged once
it passes (see below).

## Branch protection

`main` is protected, and the rule applies to admins too:

- The `build` check must pass before a PR can be merged.
- Direct pushes, force pushes and deleting `main` are blocked.

Every change therefore goes through a pull request:

```
git switch -c my-change
# edit, commit
git push -u origin my-change
gh pr create --fill
gh pr merge --squash --delete-branch   # once build is green
```

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
.github/dependabot.yml                                 dependency updates
gradle/wrapper/                                        committed Gradle wrapper
```

Never commit keystores, passwords or tokens. `.gitignore` excludes `*.jks`,
`*.keystore` and `keystore.properties`.
