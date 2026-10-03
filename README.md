# AppStore Plus

<div dir="rtl">

متجر تطبيقات وألعاب لنظام أندرويد، مبني بـ Jetpack Compose وواجهة عربية من
اليمين إلى اليسار. يعرض الكتالوج من Firestore فيصل إلى كل الأجهزة
المثبّتة فور نشر أي تطبيق جديد.

</div>

An Android app and game store built with Jetpack Compose and a right-to-left
Arabic UI. The catalog is served from Cloud Firestore, so publishing an app in
the admin dashboard makes it appear on every installed device worldwide.

---

## Features

- **Storefront** — home feed with a featured banner, most-downloaded carousel,
  and featured games
- **Browse** — apps and games lists, category chips, categories grid
- **Updates** — catalog of items flagged as having a newer version
- **Detail** — screenshots, description, mod info, version and ABI metadata
- **Search** — filters by name and developer
- **Admin dashboard** — publish, edit, delete, and feature apps, games, and
  categories; parses uploaded APKs for their metadata
- **Download and install** — streams an APK to app-private storage and hands it
  to the system installer
- **Google sign-in** via Firebase Auth

Arabic (RTL) UI throughout, dark theme, Material 3.

## Tech

| | |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, `StateFlow`, repository layer |
| Backend | Cloud Firestore, Firebase Auth |
| Images | Coil |
| Networking | OkHttp |
| Build | AGP 9.1.1, Gradle 9.3.1 |
| SDK | minSdk 24, targetSdk 36 |

## Screens

Splash · Home · Apps · Games · Categories · Updates · Detail · Profile ·
Admin Dashboard

## Project layout

```
app/src/main/java/com/example/
├── MainActivity.kt              # Compose entry point, navigation host
├── AppStoreApplication.kt       # DownloadManager init
├── data/
│   ├── StoreRepository.kt       # Firestore catalog, paged + bounded queries
│   ├── DownloadManager.kt       # Real APK download + install intent
│   └── AuthRepository.kt        # Google sign-in, admin flag
├── model/Models.kt              # StoreItem, CategoryItem, UserAccount
├── ui/
│   ├── StoreViewModel.kt        # Back stack, filters, catalog state
│   ├── screens/                 # 9 screens
│   └── components/              # Shared composables, press/touch primitives
└── util/ApkParser.kt            # APK metadata extraction
```

## Build

Requires JDK 17+ and the Android SDK (compileSdk 36.1).

```bash
git clone <your-fork-url>
cd appstore-plus

# 1. Add your Firebase config (NOT committed - see docs/CONFIGURATION.md)
cp app/google-services.json.example app/google-services.json
# then replace every YOUR_* placeholder with your real values,
# downloaded from the Firebase console.

# 2. Grant yourself admin access (optional, for the dashboard)
#    add your email to the empty array in
#    app/src/main/res/values/admin_emails.xml

./gradlew assembleDebug          # or: ./gradlew assembleRelease
```

The APK lands in `app/build/outputs/apk/<buildType>/`.

There are three build types:

| Build type | R8 | Signed with | Use |
|---|---|---|---|
| `debug` | off | debug keystore | development |
| `perf` | on | debug keystore | realistic performance testing |
| `release` | on | your upload key | distribution |

Profile with `perf`, not `debug` — debug builds disable bytecode optimisation
and are not a valid performance sample.

## Setup

**[`docs/CONFIGURATION.md`](docs/CONFIGURATION.md) is required reading.** It
covers creating the Firebase project, the required `google-services.json` shape,
deploying `firestore.rules`, the admin allow-list, release signing and SHA-1
registration, and APK hosting options.

Two steps are easy to miss and fail at runtime rather than build time:

- **Deploy `firestore.rules`.** The console's default is deny-all, which makes
  every query fail with `PERMISSION_DENIED`.
- **Register your signing key's SHA-1.** Google sign-in fails outright on a
  mismatch.

## Security notes

Catalog reads are public; writes require an admin flag checked by the
`isAdmin()` helper in [`firestore.rules`](firestore.rules).

> The shipped allow-list (`admin_emails.xml`) is **empty** and only works for
> local development, because the `/users` rule lets a caller write their own
> document — including `isAdmin`. Before running this publicly, switch to
> **Firebase custom claims**, which are set server-side and cannot be forged by
> the client. See step 5 of `docs/CONFIGURATION.md`.

Do not commit `google-services.json`, keystores, or built APKs. They are already
in `.gitignore`; `app/google-services.json.example` shows the expected shape
with placeholders.

## Demo data

The catalog ships empty and the demo seeding is **disabled by default**
(`SEED_DEMO_DATA_ON_EMPTY` in `StoreRepository`). The app shows explicit
loading, empty, and error states rather than fabricating listings.

## Contributing

Contributions are welcome under the GPL-3.0 terms below. If you add a Firestore
field, update `firebase-blueprint.json` and the explicit mapper in
`StoreRepository` — it is hand-written, not reflection-based, so field names
must match `Models.kt` exactly or the value is silently dropped.

## License

GNU General Public License v3.0. See [`LICENSE`](LICENSE).

GPL-3.0 was chosen deliberately: this project redistributes third-party APKs,
and copyleft ensures that improvements to the store itself stay open. Note that
the license covers this codebase only — it does not grant any rights to the
applications being distributed through it.
