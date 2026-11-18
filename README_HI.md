# My Video Downloader

Android source project for a no-ads video downloader prototype.

## Included
- Official Meta Facebook Login SDK integration (basic `public_profile` permission).
- Facebook login/logout and basic profile-name display.
- Receives shared Facebook URLs through Android Share / VIEW intents.
- Downloads only permitted direct MP4, M4V, or WebM URLs using Android DownloadManager.
- No ad SDK included.

## Important Facebook limitation
Facebook Login does **not** grant access to every personal or public profile Reel. Meta APIs expose only supported data and endpoints with the required permissions and app review. This project does not scrape Facebook pages or bypass private-video, login, DRM, or platform download restrictions. A full list of every public profile's Reels with a download button cannot be guaranteed through basic Facebook Login.

## Required Meta setup
1. Create your own app at Meta for Developers and add Facebook Login.
2. Configure Android package name: `com.example.myvideodownloader`.
3. Add the Android key hash for your signing key in the Meta app dashboard.
4. Replace the values in `app/src/main/res/values/strings.xml`:
   - `facebook_app_id`
   - `facebook_client_token`
   - `fb_login_protocol_scheme` (must be `fb` + your App ID)
5. Complete Meta's required privacy policy, app review, and permission setup.

Never share your Facebook password or Meta secret with anyone. The app should use the official Facebook login flow.

## Build
Open this repository in Android Studio, sync Gradle, then choose **Build > Build APK(s)**.

This is source code, not a prebuilt APK. The GitHub Actions workflow in `.github/workflows/android-build.yml` attempts to build a debug APK and publish it as a workflow artifact on each push.
