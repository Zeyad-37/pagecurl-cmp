<img align="right" src="https://user-images.githubusercontent.com/20944869/200791917-a2436c9a-d062-4c14-9c71-c94fe8703061.png">

# PageCurl for Compose Multiplatform

Page curl (page turn) effect for **Compose Multiplatform** — Android and iOS from one Kotlin codebase.

This is a multiplatform fork of [oleksandrbalan/pagecurl](https://github.com/oleksandrbalan/pagecurl)
(Apache-2.0). The original library is Android-only; this fork moves all gesture, state and
curl-geometry code to `commonMain` and keeps only the page-edge shadow platform-specific
(`expect fun DrawScope.drawCurlPageShadow`): Android uses the original native blur
(`Paint.setShadowLayer`), iOS draws a Compose-native gradient shadow.

## Motivation

Create an effect of turning pages, which can be used in book reader applications, custom
on-boarding screens or elsewhere — on both mobile platforms, with identical behavior.

## Platforms

| Target | Status |
|---|---|
| Android (`minSdk 21`) | ✅ Same behavior as upstream, native blur shadow |
| iOS (`iosArm64`, `iosSimulatorArm64`) | ✅ Verified on device at a steady 60 fps |

## Usage

### Get a dependency

Coordinates: `io.github.zeyad-37:pagecurl-cmp` *(Maven Central publishing is configured but the
first release has not shipped yet — until then, build from source with
`./gradlew :pagecurl:publishToMavenLocal` and add `mavenLocal()` to your repositories.)*

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.zeyad-37:pagecurl-cmp:2.0.0")
        }
    }
}
```

> Note: the classes live in the `com.zeyadgasser.pagecurl` package (renamed from upstream's
> `eu.wewox.pagecurl` so both libraries can coexist on an Android classpath).

### Use in Compose

Provide a count of pages and content for each page, exactly like the upstream API:

```kotlin
@OptIn(ExperimentalPageCurlApi::class)
@Composable
fun Book(pages: List<String>) {
    PageCurl(count = pages.size) { index ->
        Text(pages[index])
    }
}
```

Drag from the right edge to turn a page forward, from the left edge to go back; taps on the
right / left half work too. Use `rememberPageCurlState()` to observe or drive the current page
programmatically, and `rememberPageCurlConfig()` to configure shadow color / alpha / radius /
offset, back-page color, and drag & tap interaction zones.

## Samples

The `sample/` directory contains one shared demo UI (`sample/shared`, plain `commonMain`
Compose) consumed by both platform apps — including an on-screen FPS readout, useful when
judging curl smoothness on a real device:

- **Android**: `./gradlew :sample:androidApp:installDebug`
- **iOS**: open `sample/iosApp/iosApp.xcodeproj` in Xcode and run. The project is generated
  from `project.yml` with [XcodeGen](https://github.com/yonaskolb/XcodeGen); the generated
  `.xcodeproj` is committed, so XcodeGen is only needed if you change `project.yml`.

The legacy Android-only `demo/` module from upstream has been removed — the multiplatform
sample replaces it.

## Versioning

Forked from upstream `v1.5.1`. This fork starts at `2.0.0` to signal the package rename and
the multiplatform conversion; it is not binary-compatible with `io.github.oleksandrbalan:pagecurl`.

## Credits

All of the curl mathematics, gesture handling and API design come from
[Oleksandr Balan](https://github.com/oleksandrbalan)'s excellent
[pagecurl](https://github.com/oleksandrbalan/pagecurl) library. This fork only ports it to
Compose Multiplatform.

## License

```
Copyright 2022 Oleksandr Balan
Copyright 2026 Zeyad Gasser (Compose Multiplatform conversion)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
