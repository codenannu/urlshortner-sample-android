# Account & data deletion — Shortener Deep Link Sample

**App name:** Shortener Deep Link Sample  
**Package / applicationId:** `com.infoezeer.urlshortner`  
**Effective date:** 24 November 2026  
**Last updated:** 24 November 2026  
**Publisher / operator:** CareerWill / InfoEzeer (confirm legal entity name before public launch)  
**Contact:** support@careerwill.com

**Formats**

| Format | Path / URL |
|---|---|
| Markdown (source) | [delete-account.md](delete-account.md) |
| Standalone HTML | [delete-account.html](delete-account.html) |
| Hosted Play Console URL | `https://urlshortner.crwilladmin.com/delete-account` |
| Related privacy policy | [privacy-policy.md](privacy-policy.md) · [privacy-policy.html](privacy-policy.html) · `/privacy` |

---

## 1. Important: this App does not create user accounts

Google Play requires apps that **support account creation** to also offer account deletion.

**Shortener Deep Link Sample does not create, register, or sign in user accounts.**  
There is no email/password login, no social login, and no in-app profile.

Therefore:

- There is **no in-app “Delete account” button** because there is no account to delete.  
- This page explains how users can **remove the App** and how to request deletion of any **related Shortener analytics** that may have been generated when short links were opened.

If you later add authentication to a production fork of this sample, you must update this page and provide an in-app or web account-deletion flow that meets [Play account deletion requirements](https://support.google.com/googleplay/android-developer/answer/13315146).

---

## 2. How to remove the App and on-device data

1. Open Android **Settings → Apps → Shortener Deep Link Sample** (or long-press the app icon → App info).  
2. Tap **Uninstall**.  
3. This removes the App binary and its private app storage on the device.

The App does not maintain a cloud user profile. Clearing App storage / uninstall is sufficient for **on-device** sample data (UI state). Clipboard contents you copied manually may remain until you clear the clipboard.

---

## 3. Server-side / Shortener platform data

Opening short URLs (via the App, a browser, or messaging apps) may create **click / redirect analytics** on the CareerWill Shortener backend, for example:

- Short code / link id  
- Timestamp  
- Platform / User-Agent class  
- Hashed visitor identifiers  
- Optional app-open events (only if the build enabled `POST_APP_OPEN`)

Uninstalling the Android App **does not automatically erase** those historical analytics rows.

### Request deletion of Shortener analytics linked to you

Email **support@careerwill.com** with subject:  
`Data deletion request — Android sample com.infoezeer.urlshortner`

Include, if available:

- Approximate dates/times you opened short links  
- The short URL(s) or short codes  
- Your Android device model / OS version  
- Any other information that helps locate records  

**Service levels (targets, not guarantees):**

| Step | Target |
|---|---|
| Acknowledge receipt | Within **7 days** |
| Complete delete / anonymize where feasible | Within **30 days** after verification |

Some aggregated, non-identifying statistics may be retained.

---

## 4. Identity verification

Because the App has no login, we may ask clarifying questions to reduce the risk of deleting another person’s data. We will not require unnecessary documents. Incomplete requests may be delayed until we can locate relevant records.

Privacy rights requests may also be sent to **privacy@careerwill.com** (see [privacy-policy.md](privacy-policy.md) §7).

---

## 5. What we will not delete (typical)

- Content owned by other customers (unrelated short links).  
- Data we are required to retain for security, fraud prevention, or legal compliance.  
- Fully anonymized / aggregated metrics that cannot reasonably identify you.  
- Backup copies until rotated per platform backup retention (then expire).

---

## 6. Play Console configuration guidance (operators)

| Play requirement | How this App complies |
|---|---|
| Account creation? | **No** — declare accordingly in Data safety / App content |
| Account deletion URL | Publish this page URL; state that accounts are not offered |
| In-app deletion | Not applicable while the App has no accounts |
| If accounts are added later | Add in-app delete + update this document **before** release |
| Privacy policy URL | `https://urlshortner.crwilladmin.com/privacy` |

### Operator publishing checklist

1. Ensure admin middleware treats `/delete-account` and `/privacy` as public.  
2. Paste HTTPS URLs into Play Console.  
3. Monitor `support@careerwill.com` / `privacy@careerwill.com`.  
4. If you add accounts later: ship in-app deletion + update HTML/MD/Next pages in the same release.  
5. Keep `.md`, `.html`, and Next.js `/delete-account` content synchronized.

---

## 7. Contact

Deletion / support: **support@careerwill.com**  
Privacy policy: [privacy-policy.md](privacy-policy.md) / [privacy-policy.html](privacy-policy.html) / hosted `https://urlshortner.crwilladmin.com/privacy`
