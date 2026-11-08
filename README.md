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


## Email verification and calling

- Android sign-in uses Supabase email OTP: users enter their email, receive a one-time code, and verify it in the app.
- In Supabase Dashboard, open **Authentication → Email Templates → Magic Link** and use the OTP variable `{{ .Token }}` in the email body if you want a six-digit code rather than a clickable magic link.
- Email provider settings and allowed signup settings are managed in **Authentication → Providers → Email**. Supabase's default test email service has sending limits; configure a custom SMTP provider for reliable production delivery.
- The Calls tab currently creates authenticated voice/video call invitations in the `calls` table. The server's `call_signals` table is ready for WebRTC signaling.
- Actual live audio/video, incoming-call realtime UI, and notifications while the app is closed still need the WebRTC and push-notification integration. No phone-network calls or SMS are used.

Never put a Supabase service-role/secret key in the Android app. Only use the publishable key on the client.
