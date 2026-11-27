# Privacy Policy — Shortener Deep Link Sample

**App name:** Shortener Deep Link Sample  
**Package / applicationId:** `com.infoezeer.urlshortner`  
**Effective date:** 24 November 2026  
**Last updated:** 24 November 2026  
**Publisher / operator / App-side data controller:** CareerWill / InfoEzeer (confirm legal entity name before public launch)  
**Contact:** privacy@careerwill.com

**Formats**

| Format | Path / URL |
|---|---|
| Markdown (source) | [privacy-policy.md](privacy-policy.md) |
| Standalone HTML | [privacy-policy.html](privacy-policy.html) |
| Hosted Play Console URL | `https://urlshortner.crwilladmin.com/privacy` |
| Related deletion page | [delete-account.md](delete-account.md) · [delete-account.html](delete-account.html) · `/delete-account` |

---

## 1. Scope

This Privacy Policy describes how the Android application **Shortener Deep Link Sample** (`com.infoezeer.urlshortner`) (“the App”) handles information when you install or use it.

The App is a **demonstration client** for CareerWill URL Shortener **Android App Links** integration. It is not a social network, marketplace, or account-based consumer product.

This policy covers **only the Android sample app**. Separate CareerWill products, websites, admin consoles, and APIs may have their own notices.

---

## 2. Roles & responsibilities

| Role | Who | Responsibility |
|---|---|---|
| App publisher / controller (App-side) | CareerWill / InfoEzeer | Publishes the App on Google Play; defines App permissions and on-device behaviour |
| Shortener platform operator | CareerWill Shortener operators | Hosts resolve / redirect / analytics APIs when short links are opened |
| You (end user) | Device owner | Choose whether to open short links; uninstall; request deletion where applicable |

---

## 3. Summary (what we do and do not do)

| Topic | Practice |
|---|---|
| User accounts | **None.** The App does not register, sign in, or store user accounts. |
| Ads / ad ID | **Not used.** |
| Analytics SDKs (Firebase, etc.) | **Not embedded** in the App. |
| Contacts, photos, location, microphone, camera | **Not requested.** |
| Permissions | **INTERNET** only. |
| Children | Not directed at children under 13 (see §11). |
| Automated decision-making / ad profiling | **None.** |

---

## 4. Information the App processes

### 4.1 Information processed on device

- **Incoming App Link URL** (HTTPS short URL opened into the App), including host and short-code path segment.
- **Resolved deep-link payload** returned by the Shortener API (for example `screen`, `params`, campaign metadata). Displayed in the App UI for demonstration.
- **Clipboard** — only if you tap **Copy URL** or **Copy JSON** on the demo action toolbar (two buttons per row); content is written to the system clipboard at your request and is not uploaded by the App for that action alone.
- **In-memory UI state** while the App is running (cleared when you clear the screen or the process ends). The App does not maintain a persistent user profile store.

### 4.2 Information sent over the network

When you open a deep link (or retry resolution), the App may call the configured Shortener API:

| Call | Data sent | Purpose |
|---|---|---|
| `GET /api/v1/deep-links/resolve/{code}` | Short code; optional short-domain `host` query | Resolve navigation payload |
| `POST /api/v1/events/app-open` | `linkCode`, `platform: "android"`, optional `host` | Optional analytics (only if build flag `POST_APP_OPEN` is enabled; **default is off**) |

These requests use HTTPS. The App does **not** send Google account credentials, MMID client secrets, or passwords.

### 4.3 Information that may be processed by the Shortener backend

When a short URL is opened (including cases where the OS or browser hits the redirect service instead of the App), the Shortener platform may process **click / redirect analytics** such as:

- Short code / link identifier  
- Approximate timestamp  
- User-Agent / platform class (e.g. Android)  
- Referrer (if provided by the client)  
- Visitor cookie / hashed visitor identifier  
- IP address processed as a **keyed HMAC hash** for analytics (not stored as raw IP for unique-visitor style metrics, per platform privacy design)

See Shortener platform documentation: `careerwill-shortner-api/docs/security/privacy-and-pii.md`.

Uninstalling the App does **not** by itself delete historical click analytics already stored on Shortener servers. See the [Account & data deletion](delete-account.md) document.

### 4.4 Cookies / similar technologies

The **Android App itself** does not set web cookies. When a short link is opened in a **browser**, the Shortener redirect service may set a first-party visitor identifier cookie for analytics continuity. See platform privacy docs for cookie behaviour on web surfaces.

---

## 5. How we use information

- Demonstrate App Links → resolve → payload display.  
- Optionally record an app-open analytics event when explicitly enabled in the build.  
- Improve reliability of deep-link resolution (errors, timeouts).  

We do **not** use App data for advertising, credit scoring, or sale of personal data.

---

## 6. Legal bases (where GDPR / similar laws apply)

Depending on your jurisdiction, processing may rely on:

- **Legitimate interests** — operating a technical sample and measuring short-link opens.  
- **Contract / requested service** — resolving a link you chose to open.  
- **Consent** — where required for optional analytics flags or clipboard actions you initiate.

Operators deploying this sample for production brands should confirm legal bases with counsel.

---

## 7. Your rights

Depending on applicable law (for example GDPR/UK GDPR, India’s DPDP Act, CCPA/CPRA), you may have rights to:

- Access / know what personal information is processed about you  
- Correct inaccurate personal information  
- Delete / erase (see [delete-account.md](delete-account.md))  
- Restrict or object to certain processing  
- Data portability (where applicable)  
- Opt out of “sale” / “sharing” for cross-context behavioural advertising — **we do not sell** App data  
- Lodge a complaint with a supervisory authority  

Because the App has **no user accounts**, identity verification for rights requests may rely on information you provide (dates, short codes, device details). Contact **privacy@careerwill.com**.

**California (CCPA/CPRA):** We do not sell or share personal information for cross-context behavioural advertising as those terms are commonly understood for this App. You may still request know / delete via the contacts above.

**India (DPDP):** Where you are a Data Principal under Indian law, you may exercise applicable rights by contacting the privacy email above; the Shortener operator acts as the relevant Data Fiduciary / processor for platform analytics as configured by CareerWill.

---

## 8. Sharing

The App communicates with the **configured Shortener API / redirect hosts** (for example CareerWill production or staging environments you set in Gradle properties).

We do not sell personal information. Service providers that host the Shortener API, databases, or CDN may process technical logs under operator agreements.

Play / Android system services may process install and update metadata under Google’s policies when you distribute via Google Play.

---

## 9. Retention

| Data | Retention |
|---|---|
| On-device UI / in-memory payload | Session / until cleared or process killed |
| Clipboard copies | Controlled by the Android OS / user |
| Optional app-open events & click analytics | Per Shortener platform retention (operator-configured; raw click events may be retained for a limited period) |

---

## 10. Security

- HTTPS for API calls; cleartext HTTP traffic disabled in the App manifest.  
- No MMID or API client secrets embedded in the App.  
- Treat deep-link `params` as **untrusted navigation hints**, not authorization.  

No method of transmission or storage is 100% secure. Report suspected privacy/security incidents to **privacy@careerwill.com**.

---

## 11. Children’s privacy

The App is **not directed to children under 13** (or the equivalent minimum age in your region). We do not knowingly collect personal information from children. If you believe a child has provided personal information through related Shortener services, contact us to request deletion where applicable.

---

## 12. Your choices

- **Do not open** short links in the App.  
- **Clear** the on-screen payload using the App’s Clear action.  
- **Uninstall** the App via Android settings.  
- **Disable optional app-open** by ensuring release builds keep `POST_APP_OPEN=false` (default).  
- Request deletion of server-side analytics where available — see [delete-account.md](delete-account.md).

---

## 13. International transfers

API and analytics infrastructure may be hosted in regions chosen by the operator. Where required, appropriate transfer safeguards should be implemented by the operator.

---

## 14. Changes

We may update this Policy. The “Last updated” date will change. Material changes for a Play-distributed build should be reflected in the hosted URL used in Play Console (`https://urlshortner.crwilladmin.com/privacy`). Keep markdown, HTML, and the Next.js `/privacy` route in sync.

---

## 15. Contact

Privacy inquiries: **privacy@careerwill.com**  
Publisher support: update with your Play Console support email / website.

---

## Play Console Data safety mapping (operator checklist)

Use this when completing Google Play **Data safety** and **App content**:

| Data type | Collected by App? | Notes |
|---|---|---|
| Location | No | |
| Personal info (name, email) | No | |
| Financial info | No | |
| Health | No | |
| Messages | No | |
| Photos / video | No | |
| Audio | No | |
| Files / docs | No | |
| Calendar | No | |
| Contacts | No | |
| App activity | Optional | Only if `POST_APP_OPEN` enabled; otherwise resolve calls are functional |
| Web browsing | No | |
| App info / performance | No crash SDK | |
| Device or other IDs | No advertising ID | Optional `installId` not sent by default sample |

**Data shared with third parties for ads:** No.  
**Data collected for account creation:** No accounts.  
**Privacy policy URL (Play):** `https://urlshortner.crwilladmin.com/privacy`  
**Account deletion URL (Play):** `https://urlshortner.crwilladmin.com/delete-account`

### Operator publishing checklist

1. Deploy admin with public `/privacy` and `/delete-account` (middleware allowlist).  
2. Paste those HTTPS URLs into Play Console → App content / Data safety.  
3. Confirm contact emails are monitored.  
4. If `POST_APP_OPEN` is enabled in a release build, update Data safety “App activity” answers before shipping.  
5. Sync changes across `.md`, `.html`, and Next.js pages in the same release.
