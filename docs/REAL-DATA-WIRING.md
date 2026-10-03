# Backend / real-data wiring audit

A Compose app can render perfectly and still be 100% fake. Before writing any
integration code, establish which layer is actually live. This is a separate
pass from the UI review because the symptoms look identical — the app "works".

## Order of checks

### 1. Cross-check every Firebase identifier against one project

The failure mode is a config that names **two different projects**:

| Setting | Where | Must match |
|---|---|---|
| `project_id` | `google-services.json` → `project_info` | — |
| `firestore_database_id` | a `res/values/*.xml` string resource | same project, or deleted |
| `package_name` | `google-services.json` → `android_client_info` | `applicationId` in build.gradle |
| OAuth `certificate_hash` | `google-services.json` → `oauth_client` | SHA-1 of the *actual* signing key |

`FirebaseFirestore.getInstance("some-db-id")` where that id belongs to a project
with no API key and no client entry for your package **cannot authenticate**. It
does not throw — the listener fires with an error, and a well-written repository
falls back to bundled seed data. The app looks finished and is empty.

Grep for the indirection; the mismatched id is rarely in the Kotlin:

```bash
grep -rn "getInstance(" app/src/main/java/          # named db ids hide here
grep -rn "getString(R.string" app/src/main/java/    # resolves to a values xml
grep -rl "project_id\|firestore_database_id\|database_id" app/src/main/res/
```

### 2. Verify the API key is real, not a redacted placeholder

Placeholder keys survive in committed config and get baked into every APK:

```bash
python3 -c "import json;print(json.load(open('app/google-services.json'))['client'][0]['api_key'])"
grep -o 'name=\"google_api_key\"[^<]*<[^<]*' app/build/generated/res/process*GoogleServices/values/values.xml
```

A real Web API key is `AIzaSy` + 33 chars of `[A-Za-z0-9_-]` — **no dots**. If the
built `values.xml` contains literal `...`, every Firebase network call is
failing. Check the *generated* resource, not just the source JSON: it proves what
shipped.

### 3. Audit the security rules for public write

```bash
grep -n "allow" firestore.rules
```

`allow write: if true` on a catalog collection means any client can publish,
edit, or delete records. Public **read** on a store catalog is correct and
intended; public **write** is a hole. Gate writes behind an admin document or
custom claim:

```
function isAdmin() {
  return request.auth != null
    && exists(/databases/$(database)/documents/users/$(request.auth.uid))
    && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
}
```

### 4. Find stubs pretending to be features

Grep for the shape of a simulation rather than trusting the UI:

```bash
grep -rn "delay(\|while (progress\|TODO\|FIXME" app/src/main/java/com/example/data/
grep -rn "example.com\|placeholder\|dummy\|fake" app/src/main/java/
```

`downloadUrl = "https://example.com/..."` in seed data means the download path is
untested end to end. Say so rather than polishing its progress rendering.

### 5. Check for a declared-but-uninitialised service

Dependencies in `build.gradle.kts` with no corresponding setup call are the
quietest failure class:

```bash
for dep in appcheck crashlytics analytics remoteconfig; do
  printf "%-12s dep:%s  init:%s\n" "$dep" \
    "$(grep -c "$dep" app/build.gradle.kts)" \
    "$(grep -rc "Firebase${dep^}\|${dep^} init\|initialize" app/src/main/java/ | paste -sd+ | bc)"
done
```

App Check enforced server-side with no `FirebaseAppCheck` initialization fails
every request, including reads that appear to be public.

## Installing an APK you downloaded — the full path

```kotlin
// app-private storage cannot be handed over as file:// (throws on API 24+)
val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
Intent(Intent.ACTION_VIEW).apply {
  setDataAndType(uri, "application/vnd.android.package-archive")
  addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
```

Manifest needs all three of:

```xml
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<provider android:name="androidx.core.content.FileProvider"
          android:authorities="${applicationId}.fileprovider"
          android:exported="false" android:grantUriPermissions="true">
  <meta-data android:name="android.support.FILE_PROVIDER_PATHS"
             android:resource="@xml/file_paths" />
</provider>
<queries><intent>
  <action android:name="android.intent.action.VIEW" />
  <data android:mimeType="application/vnd.android.package-archive" />
</intent></queries>
```

Anything holding a `Context` for this (files dir, prefs) must be initialized from
`Application.onCreate`, not lazily from a composable.

## Distinct-state UI for async work

A button whose label and action disagree is worse than no button. If a control
can be idle / downloading / done / failed, each state needs its **own** label,
**own** action, and its own visual:

| State | Label | Action | Color |
|---|---|---|---|
| idle | تثبيت | start | brand |
| downloading | 42% + bar | none (disabled) | brand, dimmed |
| done | تثبيت | **open installer** | success |
| failed | إعادة المحاولة | clear + restart | danger |

The `done` state is the one that gets wired wrong: reusing the idle click handler
restarts the download instead of installing. Verify the completed branch calls a
different function.

## Admin identity

Hardcoded allowlists in the repository —

```kotlin
email.equals("owner@gmail.com") || email.contains("admin")
```

— are a placeholder for a real claim. Move it to `users/{uid}.isAdmin` or a
Firebase custom claim so it survives staff changes without an app release.

## When the blocker is a credential

Some of this cannot be fixed from code. If the API key is a placeholder or the
project identity is ambiguous, the honest move is to **stop and ask** which
project to target, write up exactly which files the user must fetch, and keep
building the parts that are independent of that choice. Do not invent a plausible
key, and do not ship an integration that silently falls back — say plainly which
layer is still fake.