# Project conventions

- Keep the app in the single `app` module and use feature packages under `feature/`.
- Use immutable domain models, constructor injection, coroutines, and `StateFlow`.
- UI collects state with lifecycle-aware Compose APIs. Keep the feed's main scrolling surface to one `LazyColumn`.
- Keep API DTOs in `data/api`, map them into domain models, and never let API response types enter UI code.
- Mock content must be visibly labelled and must not be represented as authenticated Tumblr data.
- Do not add OAuth credentials, access tokens, or keystores to source control. Never log secrets.
- Only use Tumblr's documented public API and keep the product a specialized read-only Following view.
- Prefer small composables and straightforward repository implementations. Add dependencies only when a feature uses them.
