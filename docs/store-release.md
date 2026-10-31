# Shipping to TestFlight and Google Play (internal)

App id on both stores: `com.zella.ocpp.mobile`. Apple Team ID: `XB5CS344AV`.

## 0. Before building

1. Deploy the backend (migrations V24, V25, V26) and set its variables:
   `ZELLA_FCM_CREDENTIALS_JSON`, `PVIT_CODE_URL_PAYOUT`, the social variables in `docs/social-sign-in.md`,
   and `ZELLA_AGREEMENT_URL` (terms page of the website) plus the `ZELLA_SUPPORT_*` values.
2. Deploy the website (`client/`) and note its URL: the stores need the privacy policy and deletion links.
3. Do the Google and Apple setup in `docs/social-sign-in.md`.
4. Create a **review account** (verified email, wallet with a small balance) for Apple and Google reviewers.

## 1. Google Play internal testing

```bash
cd mobile
flutter build appbundle --release \
  --dart-define=GOOGLE_SERVER_CLIENT_ID=<web client id>
```

- Output: `build/app/outputs/bundle/release/app-release.aab`.
- `android/key.properties` and `android/app/upload-keystore.jks` hold the upload key (never committed). **Back both up.**
  Upload key SHA-1 for Firebase and Google Cloud is printed by
  `keytool -list -v -keystore android/app/upload-keystore.jks -alias upload`.
- Play Console: create the app, then Testing, Internal testing, create a release and upload the `.aab`. Turn on Play App
  Signing and add the **Play App Signing SHA-1** (App integrity) to Firebase / Google Cloud for Google sign-in.
- Each upload needs a higher build number: change `version: 1.0.0+N` in `pubspec.yaml`.
- Required forms: App content (privacy policy URL, data safety, ads: none, target audience 18+, financial features:
  mobile money wallet), and the account deletion URL `https://<site>/fr/delete-account`.
- New personal developer accounts must run a closed test (12 testers, 14 days) before production. Internal testing is
  not affected.

## 2. TestFlight

```bash
cd mobile
flutter build ipa --release \
  --dart-define=GOOGLE_SERVER_CLIENT_ID=<web client id> \
  --dart-define=GOOGLE_IOS_CLIENT_ID=<ios client id>
```

- Set `GOOGLE_REVERSED_CLIENT_ID` in `ios/Flutter/Social.xcconfig` first.
- Upload `build/ios/ipa/*.ipa` with Transporter, or open `ios/Runner.xcworkspace`, Product, Archive, Distribute.
- Xcode signs with the production push environment automatically; the APNs key is already in Firebase.
- App Store Connect: create the app with bundle id `com.zella.ocpp.mobile`, then TestFlight, add internal testers.
- Export compliance is pre-answered (`ITSAppUsesNonExemptEncryption = false`, HTTPS only).

## 3. Store answers

**Apple App Privacy** (all linked to the user, not used for tracking, purpose App Functionality):
Name, Email Address, Phone Number, User ID, Payment Info (mobile money number and transaction references, no card
data), Photos (optional profile picture). Location is used to find nearby chargers and is not stored.

**Google Data Safety**: collected and linked to the account: name, email, phone number, user IDs, financial info (payment
history), photos. Data is encrypted in transit. Users can request deletion in the app and on the website.

**Review notes** (both stores): "Sign in with the review account. Top-up uses real mobile money, so the wallet is
pre-funded. Charging needs a physical charger: the app lists stations and shows the flow, and a charger can be
demonstrated on request." Provide the review account email and password in the reviewer fields.

**Account deletion**: in app at Profile, Settings, Delete my account; on the web at `/fr/delete-account`.

## 4. Checks done in this repository

- Release bundle builds minified and signed with the upload key.
- Sign in with Apple entitlement, privacy manifest and URL scheme for Google are in the iOS project.
- Backend and mobile test suites pass; the iOS app compiles for device.
