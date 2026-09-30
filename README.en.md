# SweetLime

[简体中文](README.md) | **English**

A text-effect / fancy-nickname generator, with a small toolbox attached.
Type any text and get lots of fancy styles in real time.

UI built on [Miuix](https://github.com/compose-miuix-ui/miuix) (a Compose Multiplatform take on the HyperOS design language).

## Features

| Module | Description |
|---|---|
| Convert | Live conversion while you type — Latin fancy letters plus CJK / overlay styles, shown as grouped cards |
| Detail | Large preview, original text alongside, copy / favorite |
| Favorites | Persisted per `style + input`, with a recently-used list |
| Search | One box searches the whole app: hanzi info, pinyin, decomposition, fancy styles, symbols, toolbox |
| Toolbox | Symbols, pinyin, hanzi decomposition, Chinese money amounts, Base64, radix, color, currency, BMI, code editor |
| Settings | Dynamic color (Monet), dark mode + flowing background (follow system / light / dark), privacy & compatibility notes |
| Gestures | Predictive back: drag the detail page with your finger while the screen behind it blurs (up to 28dp) |

Styles (`core/styles/`):

* Latin: bold, italic, bold italic, script, bold script, fraktur, double-struck, circled, small caps, monospace
* Overlay: underline, strikethrough, slash, dot above, tilde below (combining marks — works on CJK too)
* Plus super/subscript, boxed and template styles

## Code editor

A text box that can handle everyday small jobs:

* **Import** a plain text file, or **append-import** it to the current content
* **Read archives**: browse level by level inside a zip and edit a file in place
* **Find / replace**: single (with wrap-around), replace all, match count
* **Typing helpers**: auto-close brackets / quotes, keep the previous line's indent on Enter, split paired symbols onto their own lines
* **Encoding is never silently changed**: detected on read (UTF-8 / UTF-8-BOM / UTF-16LE / UTF-16BE / GB18030) and written back **in the original encoding** — an old GBK file won't be quietly turned into UTF-8
* **Writes can be rolled back**: the previous content is kept in memory before overwriting, and written back immediately if the write fails
* **Unzip guards**: refuse more than 96 MB total or more than 4096 entries; 32 MB limit per imported file

Limitations (by design): zip only (no rar / 7z / tar.gz); writes are not atomic, so a hard power loss could still leave a half-written file.

## Tech stack / versions

| Item | Value |
|---|---|
| Language / UI | Kotlin Multiplatform **2.4.10** + Compose Multiplatform **1.12.1** |
| UI library | Miuix **0.9.4** (`miuix-ui` / `icons` / `preference` / `blur` / `squircle`) |
| AGP / Gradle | **9.1.0** / **9.3.1** |
| Dependencies | androidx activity-compose 1.13.0 |
| SDK | compileSdk **37**, minSdk **33** (floor for blur and predictive back), targetSdk **36** |
| Version | versionCode **2610** / versionName **2.6.10** |

> compileSdk has to be 37: the AAR metadata of Miuix 0.9.4 and androidx.compose 1.12.1 requires it;
> compose 1.12.1 requires AGP ≥ 9.1.0, and AGP 9.1.0 requires Gradle ≥ 9.3.1.

## Build

```bash
./gradlew :composeApp:assembleDebug
# output: composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

Release build:

```bash
./gradlew :composeApp:assembleRelease
```

### Note

Since AGP 9, `com.android.application` no longer works directly alongside
`org.jetbrains.kotlin.multiplatform`. This project uses the official compatibility
switches in `gradle.properties`:

```properties
android.builtInKotlin=false
android.newDsl=false
```

### On arm64 Linux build machines

Google's `aapt2` (linux) has been x86_64-only since AGP 9.1 and cannot run on arm64.
On an arm64 Linux host, provide a working arm64 aapt2 and point Gradle at it in your
user-level `~/.gradle/gradle.properties`:

```properties
android.aapt2FromMavenOverride=/absolute/path/to/aapt2
```

## Tests

```bash
./gradlew :composeApp:testDebugUnitTest
```

Unit tests for pure functions and text processing; they all run on the JVM and need no device.
Covering Base64, radix conversion, color, BMI, currency, search, editor typing helpers, and encoding detection / write-back.

> Main code is compiled for `JVM_21`, so the test process needs JDK 21 (the build script
> pins it via toolchain). With only JDK 17 installed you'll get
> `UnsupportedClassVersionError: class file version 65.0`.

## Project layout

```
composeApp/src/
├── commonMain/kotlin/com/qingning/sweetlime/
│   ├── core/                pure logic, no Android dependency
│   │   ├── TextTransform.kt / TransformRegistry.kt  style interface and registry
│   │   ├── SearchEngine.kt  whole-app search
│   │   ├── ToolCatalog.kt   toolbox catalog (shared by search and the toolbox page)
│   │   ├── Platform.kt      expect: clipboard / files / zip / encoding / key-value store
│   │   ├── mapping/         code-point maps, pinyin, decomposition, symbol data
│   │   ├── styles/          the fancy-style definitions
│   │   └── tools/           Base64 / radix / color / BMI / currency (pure functions)
│   ├── data/                Settings / Favorites / Recent
│   └── ui/
│       ├── App.kt           navigation, wired to the blur
│       ├── nav/Route.kt     routes
│       ├── components/      glass containers, code highlighting, …
│       ├── screen/          the screens
│       └── theme/           ThemeController (Monet/System, Spec2025)
├── androidMain/             Android implementations of Platform + entry point
├── commonTest/              JVM unit tests
└── androidUnitTest/         tests that need java.nio (encoding detection, …)
```

Adding a new style is one line in `styles/` — `mathStyle(...)` or `overlayStyle(...)`.

## Verified

On arm64 Ubuntu (proot) + Android 15/16/17 devices:

* `assembleDebug` / `assembleRelease` build successfully
* the release APK passes signature verification (self-signed key)

## License

[MIT](LICENSE)
