# Authentication status

## Current behavior

The login button does not attempt authentication. It reports that sign-in is not configured. The separate **Preview sample feed** action opens visibly labelled local sample data. No credentials, OAuth secrets, or tokens are stored or logged.

## Documentation review

Tumblr's published API documentation supports both OAuth 1.0a and OAuth 2.0. OAuth 2.0 documents an authorization-code flow, a registered redirect URL, required `state` validation, and refresh-token grants. It lists `basic`, `write`, and `offline_access` scopes. The authorization-code and refresh-token exchanges both require the application's `client_secret`. The published guide does not document PKCE or a secretless native public-client exchange. No PKCE or custom-scheme behavior is assumed here.

Tumblr's API license agreement requires OAuth login and prohibits substantial replication of Tumblr products, while explicitly allowing specialized views of followed-blog content. This app remains a read-only Following viewer.

## Integration boundary

The documented OAuth 2.0 flow requires the consumer secret at the token-exchange endpoint. An APK is a public client and cannot keep an embedded secret confidential. Until Tumblr documents or approves a secure native flow, the app needs either an approved confidential-client backend/proxy or written confirmation of an acceptable personal-use flow. Do not place the consumer secret in the APK or use an undocumented endpoint/redirect convention.

Before implementation, verify current developer registration requirements and accepted redirect URI forms with Tumblr, and determine which documented scopes each read endpoint requires. The guide shows a 2,520-second access-token lifetime in its sample; code should use the actual `expires_in` response. It documents refresh-token rotation. Store user tokens using an Android Keystore-backed design once a supported flow is established. Add redacted error handling and tests without emitting credentials to logs.

## References

- [Tumblr API documentation](https://github.com/tumblr/docs/blob/master/api.md)
- [Tumblr API license agreement](https://github.com/tumblr/policy/blob/master/api-license-agreement.txt)
- [Tumblr developer application registration](https://www.tumblr.com/oauth/apps)
