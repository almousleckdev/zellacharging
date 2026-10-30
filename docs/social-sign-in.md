# Google and Apple sign-in

The app sends the provider's ID token to `POST /v1/auth/social`. The backend verifies the signature, issuer and
audience against the provider's public keys, then signs the customer in. A new customer is created with a verified
email and no password. An existing account with the same verified email is linked; an unverified registration with that
email loses its password so it cannot be taken over.

Deleting a password-less account needs a fresh provider sign-in (`provider` + `idToken` in `POST /v1/me/delete`). For
Apple, the app also sends the `authorizationCode` so the backend can revoke the Apple token, as App Store review requires.

## Values to provide

| Where | Name | Value |
|---|---|---|
| backend | `ZELLA_GOOGLE_CLIENT_IDS` | Google **web** client ID (comma-separate extra IDs) |
| backend | `ZELLA_APPLE_CLIENT_IDS` | `com.zella.ocpp.mobile` |
| backend | `ZELLA_APPLE_TEAM_ID` | `XB5CS344AV` |
| backend | `ZELLA_APPLE_KEY_ID` | Key ID of the "Sign in with Apple" key |
| backend | `ZELLA_APPLE_PRIVATE_KEY` | Contents of the `.p8` file (newlines may be written as `\n`) |
| app build | `--dart-define=GOOGLE_SERVER_CLIENT_ID` | the same Google web client ID |
| app build | `--dart-define=GOOGLE_IOS_CLIENT_ID` | Google iOS client ID |
| `mobile/ios/Flutter/Social.xcconfig` | `GOOGLE_REVERSED_CLIENT_ID` | iOS client ID reversed (`com.googleusercontent.apps.…`) |

Without these values the buttons stay visible but the backend answers `SOCIAL_UNAVAILABLE`.

## Google

1. Firebase console, Authentication, Sign-in method: enable **Google**. This creates the web and platform OAuth clients.
2. Project settings, Android app: add the SHA-1 and SHA-256 of the debug keystore, the upload keystore, and the Play
   App Signing certificate (Play Console, App integrity).
3. Download `google-services.json` and `GoogleService-Info.plist` again and replace the two files in the app.
4. Copy the web client ID and the iOS client ID (Google Cloud console, Credentials) into the values above.

## Apple

1. developer.apple.com, Identifiers, `com.zella.ocpp.mobile`: enable **Sign in with Apple**.
2. Keys: create a key with Sign in with Apple, download the `.p8` once, note its Key ID.
3. The entitlement is already in `Runner.entitlements`; Xcode signing adds it to the provisioning profile.

## Example build

```bash
flutter build ipa --dart-define=GOOGLE_SERVER_CLIENT_ID=… --dart-define=GOOGLE_IOS_CLIENT_ID=…
```
