# Tumblr Lite

A small, read-only Android app prototype for a specialized Following feed. It supports local OAuth 1.0a credentials for user info, followed blogs, and the documented Following dashboard feed. The paged sample feed remains available and is labelled in the UI.

## Requirements

- Android Studio with Android SDK platform 37 and Build Tools 36.0.0
- JDK 17
- Gradle 9.6.0

Open this folder in Android Studio, allow Gradle sync, then run the `app` configuration on an emulator or device. Or from PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat installDebug
```

The app opens directly to the live **Following** tab. **My Likes** loads liked photo posts from Tumblr; **For You** currently shows visibly labelled sample picks because Tumblr's documented API integration does not provide a personalized recommendation feed here. Copy `oauth.properties.example` to the ignored `oauth.properties` and fill in your existing OAuth 1.0a credentials to load Tumblr posts. If the live feed cannot load, Following offers the labelled mock sample as a development fallback. There is no browser login flow. Credentials are compiled into the APK and can be extracted from it, so keep the APK private. See [docs/AUTH.md](docs/AUTH.md).

## Architecture

The project uses one `app` module with feature-based packages, Compose Material 3, MVVM, Hilt, StateFlow, Paging 3, Retrofit, Kotlin serialization, and an OkHttp OAuth signer. Account, dashboard, and likes API DTOs are mapped to immutable domain models. Following and My Likes use documented read-only Tumblr endpoints; For You is a labelled sample-picks view until a documented personalized source is available. Sample photos are not represented as authenticated Tumblr content.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [AGENTS.md](AGENTS.md).

## Main dependency versions

Versions were checked against the official project release pages on November 8, 2026.

| Component | Version |
| --- | --- |
| Android Gradle Plugin | 9.5.1 |
| Gradle | 9.6.0 |
| Kotlin | 2.4.21 |
| Compose BOM | 2026.09.00 |
| Hilt | 2.60.1 |
| Paging | 3.5.3 |
| Retrofit | 3.0.0 |
| Kotlinx Serialization | 1.12.0 |
| Coroutines | 1.11.0 |
| Coil | 3.6.3 |
