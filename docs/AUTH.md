# OAuth 1.0a local account connection

## What the app does

The app opens directly to the live Following feed and signs read-only requests with OAuth 1.0a HMAC-SHA1. Following uses documented `/v2/user/dashboard` with `limit`/`offset` pagination and NPF response content. My Likes uses documented `/v2/user/likes`; it pages by offset through the first 1,000 liked posts and then by the documented `before` timestamp cursor. Both screens show photo posts in the same image-first masonry grid. Tumblr documents page sizes from 1 to 20, and each request is limited to 20 posts. The mapper handles NPF image blocks on a post or its documented reblog `trail`, plus the documented legacy `photos`/`alt_sizes` shape for older photo posts. Coil's GIF decoder animates GIF media in live Tumblr posts; the mock samples are still images. For You is a clearly labelled local sample-picks view; no personalized recommendation endpoint is used. Video playback and full NPF layouts are not implemented. The love and follow controls are visual-only and do not send write requests. There is no browser authorization flow, credential entry screen, token exchange, or refresh flow. If the live Following feed cannot load, its error screen offers a clearly labelled mock sample fallback.

OAuth 1.0a signs the HTTP method, normalized base URI, and the percent-encoded/sorted query, form, and OAuth parameters. The signer uses a fresh cryptographic nonce and Unix-seconds timestamp per request. It emits an OAuth Authorization header, uses HMAC-SHA1, and does not log request headers or credentials. This follows [RFC 5849](https://datatracker.ietf.org/doc/html/rfc5849) and Tumblr's documented [user info, dashboard, and following endpoints](https://github.com/tumblr/docs/blob/master/api.md#user-methods) with the [NPF specification](https://github.com/tumblr/docs/blob/master/npf-spec.md).

## Configure local credentials

1. Copy `oauth.properties.example` to `oauth.properties` in the repository root.
2. Fill in the already-authorized application's `consumerKey`, `consumerSecret`, `oauthToken`, and `oauthTokenSecret` locally.
3. Keep the file private on your device. Git ignores `oauth.properties`; verify it is not staged before committing.
4. Build/install the debug app and launch it. The Following tab requests Tumblr posts directly; if a value is missing, the app reports the configuration problem and offers the labelled sample feed.

Do not set these values in GitHub Actions, CI variables, test fixtures, documentation, screenshots, or logs. Test data uses only the public RFC 5849 example values, not this application's credentials.

## APK security limitation

Local configuration prevents accidental source-control disclosure, but Gradle writes these values into generated `BuildConfig` fields and the APK. **Any credential packaged into an APK can be extracted, including when the APK or values are obfuscated or encrypted.** This personal-device arrangement is not a secure multi-user production credential architecture. Anyone who obtains the APK may recover the consumer secret and token pair. Do not distribute the APK. Revoke and replace the authorized token pair if the APK is exposed. Android Keystore protection cannot hide a static secret that must be recoverable by the installed app.

## Failure behavior

- 401 reports that Tumblr rejected the token pair. Check token validity and authorization outside this app; no automatic re-login is attempted.
- 429 reports rate limiting and displays `Retry-After` when supplied.
- IO/DNS/connectivity failures report a network error.
- Invalid JSON or missing required response objects report an unreadable response.
- Other HTTP errors include only the status code.

The application has no HTTP body/header logging interceptor. Never add one without redacting `Authorization` and other sensitive values. Tumblr's dashboard endpoint documentation cautions against recreating the complete Tumblr Dashboard; this app stays read-only and limited to photo views for Following and the account's Likes, without like/unlike actions, reblogs, replies, posting, notifications, or dashboard-specific controls. Review Tumblr's current API agreement before expanding this scope.
