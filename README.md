# CareerWill Shortener — Android App Links sample

Client-shareable sample that demonstrates:

1. Verified HTTPS **Android App Links** opening this app when installed  
2. Extracting the short code from the incoming Intent  
3. Calling `GET /api/v1/deep-links/resolve/{code}`  
4. **DEEP_LINK** → display screen/params; **STANDARD** → open `destinationUrl` in the browser (classic shortener; no app payload)  

**Link types:** App Links (`pathPrefix="/"`) catch **every** path on the short host — WhatsApp will open the app for STANDARD and DEEP_LINK when the app is installed.  
- **DEEP_LINK** → resolve → show screen/params.  
- **STANDARD** → resolve → open `destinationUrl` in the browser → **`finish()`** (user ends in browser).  
For STANDARD that must never open the app, use a second short host without Asset Links (see API domain-association docs).

**Package / applicationId:** `com.infoezeer.urlshortner` (see `app/build.gradle.kts`)  
**Not supported in this sample:** Play Install Referrer deferred deep linking after install. After a Play Store install, the user must open a short URL again (or use authenticated pending/claim on the backend).

**Flow Inspector (`?d=1`):** If the incoming App Link includes exact `d=1`, the sample opens the **public** inspector at `{WEB_FALLBACK_BASE_URL}/deeplink/{code}?d=1&host=…` in the system browser instead of resolving the payload. Third-party apps should do the same, or users can use “Open in browser” — verified App Links may otherwise open the app and never hit the redirect handoff.

**Full beginner runbook (local + production, SHA-256, USB phone):**  
[physical-device-verification.md](../careerwill-shortner-api/docs/integrations/deep-links/physical-device-verification.md)

## Prerequisites

- Android Studio / JDK 17+  
- Shortener App Platform **ACTIVE** with this app’s `applicationId` + signing SHA-256  
- Live `https://{short-domain}/.well-known/assetlinks.json`  
- App Platform **Android store / install fallback URL** (`androidFallbackUrl`) set to your Play Store listing (for not-installed Scenario B)

## How to get the debug SHA-256

The fingerprint in App Platform must match the cert that signed the APK you install. For Android Studio **debug** builds:

```bash
keytool -list -v \
  -keystore ~/.android/debug.keystore \
  -alias androiddebugkey \
  -storepass android \
  -keypass android
```

Copy the **SHA256:** line (colon-separated hex). Do **not** use SHA-1.

Alternate:

```bash
./gradlew :app:signingReport
```

Use the `SHA-256` under **Variant: debug**.

**Production / Play apps:** Play Console → **App integrity** → copy **App signing** SHA-256 values (**Classical** and **Post-quantum** when listed). Prefer the **Digital Asset Links JSON** `sha256_cert_fingerprints` array — paste **all** entries into App Platform (comma-separated). Upload-key SHA alone is **not** enough for Play installs. Debug SHA ≠ Play signing SHA.

Full cookbook: [play-signing-sha256.md](../careerwill-shortner-api/docs/integrations/deep-links/play-signing-sha256.md).

Paste fingerprints into admin **App Platforms → SHA256 fingerprints**. Package name must be exactly `com.infoezeer.urlshortner` (or your renamed `applicationId`).

## Configure (do not commit secrets)

Copy [`gradle.properties.example`](gradle.properties.example) to `gradle.properties` (project or `~/.gradle/gradle.properties`), or pass `-P` flags.

### Production (CareerWill) example

```properties
DEEP_LINK_HOST=cwsu.crwilladmin.com
ALLOWED_HOSTS=cwsu.crwilladmin.com
API_BASE_URL=https://urlshortner-api.crwilladmin.com/api/v1
WEB_FALLBACK_BASE_URL=https://urlshortner.crwilladmin.com
```

### Local (cloudflared) example

Phone cannot use `localhost`. Use three HTTPS tunnels (short/redirect, API, admin) — see runbook Track A. Hosts have **no** `https://` in `DEEP_LINK_HOST` / `ALLOWED_HOSTS`.

```properties
DEEP_LINK_HOST=xxxx.trycloudflare.com
ALLOWED_HOSTS=xxxx.trycloudflare.com
API_BASE_URL=https://yyyy.trycloudflare.com/api/v1
WEB_FALLBACK_BASE_URL=https://zzzz.trycloudflare.com
```

Update `applicationId` / package in `app/build.gradle.kts` to match the App Platform package name before **real** client production handoff. This sample keeps `com.infoezeer.urlshortner` for verification against a dedicated ACTIVE App Platform (debug SHA).

## Play Store — generate App Bundle (AAB)

Play Console expects an **Android App Bundle** (`.aab`), not a debug APK.

### 1. Set production deep-link config

In project `gradle.properties` (or `-P` flags):

```properties
DEEP_LINK_HOST=cwsu.crwilladmin.com
ALLOWED_HOSTS=cwsu.crwilladmin.com
API_BASE_URL=https://urlshortner-api.crwilladmin.com/api/v1
WEB_FALLBACK_BASE_URL=https://urlshortner.crwilladmin.com
```

`applicationId` must match the Play listing: `com.infoezeer.urlshortner`.

### 2. Create (or reuse) an upload keystore

**First time only** (back up the `.jks` and passwords — required for every future update):

```bash
keytool -genkey -v \
  -keystore upload-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload
```

Copy `keystore.properties.example` → `keystore.properties` and fill paths/passwords (gitignored).

If this Play app already has an upload key, use **that** keystore — a new key will be rejected.

### 3. Bump version for updates

In `app/build.gradle.kts` → `defaultConfig`:

- First upload: `versionCode = 1` / `versionName = "1.0"` is fine  
- Every new Play upload: increase `versionCode` (must be higher than last published)

### 4. Build the release AAB

```bash
cd careerwill-shortner-android
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### 5. Upload in Play Console

1. Play Console → your app → **Test and release** (Internal / Closed / Production)  
2. **Create new release** → upload `app-release.aab` → review → roll out  

### 6. App Links after Play App Signing

Play re-signs with the **App signing** certificate(s). Put **all** App signing SHA-256 values from Play Console → **App integrity** into Shortener **App Platforms**:

| App integrity panel | Action |
|---|---|
| Classical key | Copy SHA-256 → App Platform |
| Post-quantum cryptography key | Copy SHA-256 → App Platform (if shown) |
| Digital Asset Links JSON | Easiest: copy every `sha256_cert_fingerprints` entry |

Debug SHA ≠ Play App signing SHA. Details: [play-signing-sha256.md](../careerwill-shortner-api/docs/integrations/deep-links/play-signing-sha256.md).

Upload keystore fingerprint (optional, sideload only):

```bash
keytool -list -v -keystore upload-keystore.jks -alias upload
```

### 7. Privacy policy & account deletion URLs (Play Console)

Play requires a **public privacy policy URL**. Account deletion URL is required when the app creates accounts (this sample does **not**).

| Resource | Hosted URL | Repo sources |
|---|---|---|
| Privacy policy | `https://urlshortner.crwilladmin.com/privacy` | [docs/privacy-policy.md](docs/privacy-policy.md) · [docs/privacy-policy.html](docs/privacy-policy.html) · admin `src/app/privacy` |
| Account / data deletion | `https://urlshortner.crwilladmin.com/delete-account` | [docs/delete-account.md](docs/delete-account.md) · [docs/delete-account.html](docs/delete-account.html) · admin `src/app/delete-account` |

Paste the hosted HTTPS URLs into Play Console → **App content** / **Data safety**. Keep markdown, HTML, and Next.js pages in sync when legal text changes.

## Build & run (USB device)

```bash
./gradlew :app:assembleDebug
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or Android Studio → Run → select the physical device.

## Client integration checklist

### Mandatory

1. Create App Platform with Android package + SHA-256 fingerprints; set status **ACTIVE**  
2. Set **Android store / install fallback URL** to the Play Store listing  

3. Confirm `GET https://{short-domain}/.well-known/assetlinks.json` returns your package + fingerprints  
4. Configure manifest `intent-filter` with `android:autoVerify="true"` for `https://{short-domain}`  
5. Handle `VIEW` intents in cold start and `onNewIntent`  
6. Extract short code from path; allowlist hosts  
7. Resolve via public API; navigate/display using `screen` + `params` only (treat as untrusted hints)  
8. Test **installed** and **not installed** with the **same** short URL  

### Optional

- `POST /api/v1/events/app-open` (enable `POST_APP_OPEN` BuildConfig)  
- Per-link `androidUrl` override  
- Authenticated `pending` / `claim` for login-gated screens  

### Not supported (sample v1)

- Automatic payload recovery after Play Store install (Install Referrer)

## Verify App Links

```bash
adb shell pm get-app-links com.infoezeer.urlshortner
adb shell pm verify-app-links --re-verify com.infoezeer.urlshortner
adb shell am start -a android.intent.action.VIEW \
    -c android.intent.category.BROWSABLE \
    -d "https://cwsu.crwilladmin.com/YOURCODE"
```

For local tunnels, replace the host with your short tunnel hostname. Full steps: [physical-device-verification.md](../careerwill-shortner-api/docs/integrations/deep-links/physical-device-verification.md).

## Expected flows

| State | Expected |
|---|---|
| App installed + verified | OS opens this app → payload on screen |
| App installed + `?d=1` (exact first) | Sample opens **Flow Inspector** in system browser (`WEB_FALLBACK_BASE_URL/deeplink/{code}?d=1&host=…`); UI shows Open Flow Inspector |
| App not installed | Browser → 302 Play Store (or interstitial with Play CTA) |
| After fresh install | **No** automatic deferred payload |

## Troubleshooting (Android Studio)

| Error | Fix |
|---|---|
| `'when' expression must be exhaustive` / missing `DiagramInspector` | Ensure `DeepLinkDemoScreen` handles all `LaunchKind` values (`Normal`, `DeepLink`, `DiagramInspector`) |
| Resolve fails / connection refused | Set `API_BASE_URL` in `gradle.properties`; use HTTPS (tunnel or prod) — sample does not allow cleartext |
| `d=1` stays in app | Tap **Open Flow Inspector**; set `WEB_FALLBACK_BASE_URL` to the admin/web host that serves `/deeplink` |
| Domain not verified | SHA/package mismatch or `assetlinks` not reachable over HTTPS — see runbook troubleshooting |

## Project layout

- `deeplink/` — config, URI parser, payload parser, HTTP resolver, ViewModel  
- `ui/DeepLinkDemoScreen.kt` — demo UI  
- `MainActivity.kt` — intent lifecycle (`singleTop` + `onNewIntent`)

## Security notes

- Never embed MMID client secrets in the app  
- Resolve is public — validate screen/params against your own allowlists before navigation  
- Do not place access tokens in deep-link query strings  
- Disable temporary sample App Platforms (debug SHA) on production after verification if not intended for clients  
