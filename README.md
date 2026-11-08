# Tumblr Lite

A small, read-only Android app prototype for a specialized Following feed. The current bootstrap has a working Compose login screen and a paged sample feed. It does **not** connect to Tumblr yet; the sample preview is labelled in the UI.

## Requirements

- Android Studio with Android SDK platform 37 and Build Tools 36.0.0
- JDK 17
- Gradle 9.6.0

Open this folder in Android Studio, allow Gradle sync, then run the `app` configuration on an emulator or device. Or from a shell with Gradle installed:

```powershell
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
gradle :app:connectedDebugAndroidTest
```

The initial source drop has no Gradle wrapper because Gradle and a JDK 17/Android SDK are not installed in the authoring environment. Android Studio can import the project and generate a wrapper after the first sync.

Select **Preview sample feed** to browse the local mock posts. **Continue with Tumblr** displays an explicit setup message; it does not open a fake login or claim authentication succeeded. See [docs/AUTH.md](docs/AUTH.md).

## Architecture

The project uses one `app` module with feature-based packages, Compose Material 3, MVVM, Hilt, StateFlow, and Paging 3. `FeedRepository` is the domain boundary. `FakeFeedRepository` currently backs it; the Retrofit `TumblrApi` interface is only an endpoint/DTO boundary and is not wired to credentials or networking.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [AGENTS.md](AGENTS.md).

## Main dependency versions

Versions were checked against the official project release pages on November 8, 2026.

| Component | Version |
| --- | --- |
| Android Gradle Plugin | 9.5.0 |
| Gradle | 9.6.0 |
| Kotlin | 2.4.21 |
| Compose BOM | 2026.09.00 |
| Hilt | 2.60.1 |
| Paging | 3.5.3 |
| Retrofit | 3.0.0 |
| OkHttp | 5.5.1 |
| Kotlinx Serialization | 1.12.0 |
| Coroutines | 1.11.0 |
| DataStore | 1.2.1 |
| Coil | 3.6.3 |
