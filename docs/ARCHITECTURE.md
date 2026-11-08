# Architecture

The Android project is one Gradle module, divided by responsibility:

- `core`: shared theme and future network/security/UI utilities.
- `domain`: immutable feed model and `FeedRepository` contract.
- `data`: sample and OAuth-authenticated paged feed repositories, Tumblr endpoint interface, DTOs, mapping, and Hilt bindings.
- `feature/auth`: retained account-check UI and state; it is no longer part of the startup path.
- `feature/feed`: Paging-backed ViewModel and single-LazyColumn feed UI.
- `feature/navigation`: three-tab app shell; Following and My Likes are live read-only photo feeds, while For You is a visibly labelled local sample-picks view.
- `feature/profile`: reserved for a future read-only blog view.

The Following and Likes `Pager`s are exposed by repositories and cached in ViewModel scope. The shared photo grid handles refresh, append, errors, and empty states. API DTOs map into `FeedPost` domain models before reaching the UI. The likes source uses the documented offset cursor through the first 1,000 items, then the documented timestamp cursor. The mock source has a small fixed photo dataset so its finite end is intentional; it is used for the For You sample-picks view and the Following development fallback. Sample media are illustrative and never represented as authenticated Tumblr content.

The Android `Application` supplies one Coil `ImageLoader` singleton. It decodes images at the `AsyncImage` constraints, caps the memory cache at 20% of available memory (10% while backgrounded), and uses a bounded 2% cache directory under Android's evictable cache storage.

The Retrofit client uses an OkHttp RFC 5849 OAuth 1.0a HMAC-SHA1 interceptor, the Kotlin serialization converter, and local ignored `oauth.properties` configuration. The account repository reads `/v2/user/info` and `/v2/user/following`; the live feed repository reads the documented `/v2/user/dashboard` endpoint with `limit`/`offset` pagination and `npf=true`. Both deserialize typed API DTOs, map data into immutable domain models, and classify authorization, rate-limit, network, HTTP, and malformed-response failures. The sample feed remains separate and labelled.

The reviewed API guide reports rate limits of 300 requests/minute and 18,000/hour per IP, and 1,000/hour and 5,000/day per consumer key. It returns HTTP 429 when a limit is reached. Recheck these limits before enabling live requests.

Tumblr's dashboard endpoint documentation cautions against reimplementing the Dashboard or complete Tumblr clients. This app is scoped as a narrow read-only Following reader. It performs no posting, likes, reblogs, replies, archiving, background polling, analytics, or persistent feed mirroring. Review Tumblr's current API agreement before expanding the app's scope.

## Security note

The local OAuth properties become generated `BuildConfig` values and are packaged in the APK. Obfuscation or encryption does not make packaged credentials secret. This is suitable only for the owner's private test APK. No production login flow or token storage/refresh lifecycle is implemented. Keep mock preview available as a separate mode.
