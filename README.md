## WebView Integration (Android + iOS)

A clean, modern starter for embedding rich web apps in native shells — with camera/microphone (WebRTC) support, sensible defaults, and production-ready guardrails.

- **Platforms**: Android (WebView) and iOS (WKWebView with Safari fallback pre–iOS 15)
- **Use case**: Open a dynamic URL (e.g., exhibitor pages) inside a native container, preserving device capabilities
- **What you get**: Ready-to-run apps, permission handling, domain allow-listing, and a minimal, readable codebase

---

## Highlights

- **WebRTC-ready**: Camera and mic prompts handled correctly on both platforms
- **Secure navigation**: Internal links stay in-app; external links open in the system browser (Android)
- **Dynamic URL**: Enter any HTTPS URL at runtime; sensible defaults included
- **Production-minded**: Clear places to tighten ATS (iOS) and network security config (Android)

---

## Quick Start

### Android

Prereqs: Android Studio (Giraffe+), JDK 11, Android SDK Platform 35

1) Open the `android/` folder in Android Studio
2) Select the `app` run configuration and press Run ▶
3) On first launch, grant camera/mic permissions when prompted
4) Paste a full URL (https://…) or use the default dev URL and tap “Open WebView”

Key versions:
- Compile/Target SDK: 35
- Min SDK: 21
- Kotlin: 2.0.21, AGP: 8.10.1

### iOS

Prereqs: Xcode 15+, iOS 12+ device or simulator (camera/mic only work on real devices)

1) Open `ios/WebviewIntegration.xcodeproj`
2) Choose a simulator or a physical device and press Run ▶
3) Enter a full URL and proceed
   - iOS 15+: uses embedded `WKWebView`
   - iOS 14 and below: falls back to `SFSafariViewController`

---

## Configuration

### Default URLs

- Android: edit the `@string/default_url` in `android/app/src/main/res/values/strings.xml`
- iOS: update `defaultUrlString` (and initial `urlText`) in `ios/WebviewIntegration/ContentView.swift`

### Domain allow‑listing (Android)

Internal links for `*.roziesynopsis.com` remain in-app; everything else opens in the default browser. Adjust this in `android/app/src/main/java/com/webview_integration/android/XmlWebview.kt` (`shouldOverrideUrlLoading`).

### Permissions & Networking

- Android Manifest requests: `INTERNET`, `CAMERA`, `RECORD_AUDIO`, and `MODIFY_AUDIO_SETTINGS`
- Android network security config currently permits cleartext (for flexibility in development)
- iOS `Info.plist` includes `NSCameraUsageDescription`, `NSMicrophoneUsageDescription`, and a permissive ATS (`NSAllowsArbitraryLoads = true`)

Production hardening suggestions:
- Android: set a stricter `network_security_config` (disable cleartext) and keep domain checks tight
- iOS: replace global ATS allow with domain-scoped exceptions or `NSAllowsArbitraryLoadsInWebContent`

---

## How It Works (Why each piece exists)

- Android `XmlWebview` configures `WebView` with JavaScript, storage, media playback without extra taps, and third‑party cookies; it handles runtime camera/mic permissions and keeps navigation in-domain
- iOS `WebViewContainer` builds a `WKWebView` with inline media playback and uses WebKit’s media capture permission handler on iOS 15+
- iOS `ContentView` routes to embedded WebView on iOS 15+ and falls back to Safari on older versions

---

## Troubleshooting

- Camera/mic not prompting:
  - Use HTTPS, not HTTP
  - Test on a real device (simulators have limitations for camera/mic)
  - Ensure you granted permissions when prompted
- Blank page or error:
  - Verify the URL loads in a normal mobile browser
  - Check internet connectivity and SSL validity
- External links opening inside the app (Android):
  - Update the allow‑list logic in `shouldOverrideUrlLoading`

---

## Project Structure

- `android/` — Android app module with `WebView` implementation and runtime permissions
- `ios/` — iOS app (SwiftUI) with `WKWebView` wrapper and Safari fallback
- `android/WebView_Integration_Guide.md` — Android step‑by‑step with rationale
- `ios/WebView_Integration_Guide.md` — iOS step‑by‑step with rationale

---

## FAQ

- Can I pre‑set the URL instead of typing it? Yes — set the default URL in the locations listed above or pass it at runtime from your app’s entry point.
- Does this support file uploads? The setup is WebRTC‑ready and compatible with input capture; test your exact upload flow and extend the `WebChromeClient` (Android) as needed.
- Can I inject custom headers or cookies? Yes — hook into request loading and cookie managers (`CookieManager` on Android, `WKHTTPCookieStore` on iOS).


---

## License

MIT — see `LICENSE`.


