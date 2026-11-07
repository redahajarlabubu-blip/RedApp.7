# RedApp

RedApp is a native Android app built with Kotlin and Jetpack Compose. It is not an HTML page or WebView.

## Current foundation
- Native Compose application and dark green/blue Oudine-inspired theme
- Supabase Kotlin client configured for Auth, PostgREST and Realtime
- Email/password sign-up and sign-in UI
- Local-only configuration for the Supabase publishable key

## Configure Supabase
1. Copy `local.properties.example` to `local.properties`.
2. Set `SUPABASE_URL` and `SUPABASE_PUBLISHABLE_KEY` in that file.
3. Never put a Supabase service-role/secret key in the Android app.
4. Open the project in Android Studio and run the `app` configuration.

The existing Supabase project URL is already configured as the default. The publishable key must be added locally and is intentionally not committed.

## Build
Use Android Studio with JDK 17 and Android SDK 35, then run the app or execute `gradle assembleDebug`.
