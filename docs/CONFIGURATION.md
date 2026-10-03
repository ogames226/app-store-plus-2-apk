# Configuration

Everything below is project-specific and **not** committed. This file is the
replacement guide for a fresh clone.

Files you must create or edit after cloning:

| File | Tracked? | What it is |
|---|---|---|
| `app/google-services.json` | ignored | Firebase config with your live keys |
| `app/google-services.json.example` | tracked | placeholder template |
| `app/src/main/res/values/admin_emails.xml` | tracked | admin allow-list, ships empty |
| `firebase.rules` | tracked | paste into the console |
| `gradle.properties` | tracked | signing passwords come from env, not here |

---

## 1. Firebase project

1. Create a project in the [Firebase console](https://console.firebase.google.com/).
2. **Build -> Firestore Database -> Create.** Pick a region deliberately; it is
   effectively permanent and determines read latency for your users.
3. **Build -> Authentication -> Sign-in method -> Google -> Enable.**
4. **Project settings -> Your apps -> Android**, register your package name
   (must match `applicationId` in `app/build.gradle.kts`), then download
   `google-services.json` and place it at `app/google-services.json`.

The download is the only supported way to obtain this file. Copy
`google-services.json.example` as a structural reference.

### Required `google-services.json` shape

```
project_info.project_id                 -> your project id
client[0].client_info.android_client_info.package_name
                                        -> MUST equal applicationId
client[0].api_key[0].current_key        -> real key, "AIzaSy" + 33 chars
client[0].oauth_client[type 1].android_info.certificate_hash
                                        -> your signing key's SHA-1
```

A package-name mismatch compiles fine and then fails silently at runtime, so
verify it before building.

---

## 2. Firestore security rules

Paste the contents of [`firestore.rules`](../firestore.rules) into
**Firestore -> Rules** and publish.

The default rules created by the console are deny-all:

```
match /{document=**} { allow read, write: if false; }
```

With those in place every query fails with `PERMISSION_DENIED` and the app
shows its "could not load store" state.

### How admin access works

Catalog reads are public; writes require `isAdmin()` to be true, which reads
`users/{uid}.isAdmin` for the signed-in caller.

To grant yourself admin access, sign in through the app's Google button. On
first sign-in the client writes the user document, which seeds `isAdmin` from
the allow-list below.

> **Security caveat, read this before going public.** The `/users` rule lets a
> caller write their own document, so a determined user can set
> `isAdmin: true` on themselves. For a public deployment, use a **Firebase
> custom claim** instead, which is set server-side and cannot be spoofed by the
> client. See step 5.

---

## 3. Admin allow-list

`app/src/main/res/values/admin_emails.xml` ships with an **empty** array, which
is the safe default: nobody can write through the admin dashboard.

For local development, add your own address:

```xml
<string-array name="admin_emails" translatable="false">
    <item>you@example.com</item>
</string-array>
```

Do not commit a real address. A published address identifies the project owner
and, worse, every fork inherits those privileges.

---

## 4. Release signing

Never commit keystores (`.gitignore` already covers `*.jks` and `*.keystore`).
Generate an upload key and export the values as environment variables:

```bash
keytool -genkey -v -keystore my-upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias upload

export KEYSTORE_PATH="$PWD/my-upload-key.jks"
export STORE_PASSWORD='...'
export KEY_ALIAS='upload'      # 'keyAlias' is hardcoded to "upload" in build.gradle.kts
export KEY_PASSWORD='...'
```

`app/build.gradle.kts` reads `KEYSTORE_PATH`, `STORE_PASSWORD` and `KEY_PASSWORD`.
If the alias differs, change `keyAlias` in the `signingConfigs.release` block.

Register the **release** key's SHA-1 in the console as well as the debug one:

```bash
keytool -list -v -keystore my-upload-key.jks -alias upload
```

Google sign-in fails outright on a SHA-1 mismatch, so this step is required
before shipping.

---

## 5. Optional: custom claims instead of the allow-list

The robust replacement for `admin_emails.xml`. Custom claims live in the ID
token and are set only by trusted server code, so a client cannot forge them.

```bash
npm i -g firebase-tools
firebase login
firebase functions:config:set  # or use your own service account
```

A callable function that promotes a user:

```js
const admin = require('firebase-admin').initializeApp();
exports.setAdmin = onRequest((req, res) => {
  if (!req.auth?.token.admin) {
    res.status(403).send('Forbidden');
    return;
  }
  admin.auth().setCustomUserClaims(req.body.uid, { admin: true });
  res.send('ok');
});
```

Promote your own account once:

```bash
firebase functions:shell
> admin.auth().setCustomUserClaims('<YOUR_UID>', { admin: true })
```

Then change `isAdmin()` in `firestore.rules` to check the claim directly:

```
function isAdmin() {
  return request.auth != null && request.auth.token.admin == true;
}
```

This requires upgrading to the Blaze plan for Cloud Functions.

---

## 6. APK hosting

`StoreItem.downloadUrl` is fetched with a plain HTTP GET, so any HTTPS host
works and Firebase Storage is not required.

Free options that need no payment method: a Telegram bot channel
(`https://api.telegram.org/file/bot<TOKEN>/<file_path>`, capped at 50 MB per
file), or any static host. For multi-gigabyte APKs you will need a real
hosting budget or a Blaze upgrade.
