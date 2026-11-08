# Architecture

The Android project is one Gradle module, divided by responsibility:

- `core`: shared theme and future network/security/UI utilities.
- `domain`: immutable feed model and `FeedRepository` contract.
- `data`: fake paged repository, Tumblr endpoint interface, DTOs, and Hilt bindings.
- `feature/auth`: sign-in state and login screen.
- `feature/feed`: Paging-backed ViewModel and single-LazyColumn feed UI.
- `feature/profile`: reserved for a future read-only blog view.

The feed's `Pager` is exposed by the repository and cached in the ViewModel scope. Paging handles incremental requests and refresh; the mock source has a small fixed sample dataset so its finite end is intentional. Remote image URLs in the sample are illustrative; GIF samples are still images and videos have a visible placeholder.

The Retrofit API interface includes only documented user-following and blog-post endpoint shapes. It has no configured Retrofit client, token interceptor, API-backed repository, or serializer setup. `user/following` requires OAuth and documents a `limit` of 1–20 with `offset`; blog posts require the registered app API key and document `limit` 1–20 with `offset` or `before` (never both). The blog-post method exposes both documented pagination choices, so a future caller must enforce their mutual exclusion. Tumblr's response guide describes the standard root `meta`/`response` envelope and NPF post content; current DTOs are intentionally partial and are not used to deserialize live responses.

The reviewed API guide reports rate limits of 300 requests/minute and 18,000/hour per IP, and 1,000/hour and 5,000/day per consumer key. It returns HTTP 429 when a limit is reached. Recheck these limits before enabling live requests.

The Tumblr API agreement permits specialized views of followed-blog content and says not to reimplement the Dashboard or complete Tumblr clients. This app is scoped as a read-only Following viewer. It performs no posting, likes, reblogs, replies, archiving, background polling, analytics, or persistent feed mirroring. The API guide itself warns against reimplementing the Dashboard.

## Next milestone

Resolve the secure OAuth integration path first. Then implement a narrowly scoped real repository using verified response schemas, token handling, rate-limit/session-expiry errors, and tests with MockWebServer. Keep mock preview available as a separate mode.
