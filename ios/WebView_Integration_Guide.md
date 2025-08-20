# iOS WebView Integration – Step-by-Step Guide

This guide shows exactly what to add or change and why, mirroring the Android guide but tailored for iOS.

- **Bundle Identifier**: Use your app’s bundle ID
- **Minimum iOS**: 12+ (app compiles broadly), with two runtime paths:
  - **iOS 15+**: Embedded `WKWebView` (supports camera/mic via WebKit permission API)
  - **iOS 14 and under**: Fallback to `SFSafariViewController`
- **Target URL pattern**: `https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{userId}`
- **Example**: `https://exhibitors.roziesynopsis.com/e/DTM2025/u/201183`

---

## 1  Add frameworks
Only system frameworks are needed.

- **SwiftUI**: app scaffolding and navigation
- **WebKit**: `WKWebView`
- **SafariServices**: `SFSafariViewController` (fallback pre–iOS 15)

---

## 2  Info.plist — Privacy permissions (must-have)
Why? The embedded page uses camera/mic (WebRTC). iOS requires explicit usage descriptions or the app will crash on access.

Add these keys to your target’s `Info.plist` and use user-friendly strings:

```xml
<key>NSCameraUsageDescription</key>
<string>Needed to capture business cards and scan QR codes inside the embedded web page.</string>
<key>NSMicrophoneUsageDescription</key>
<string>Needed for coversation recording for live insights generation inside the embedded web page.</string>
```

Networking policy shown in this sample enables arbitrary HTTPS loads for simplicity:

```xml
<key>NSAppTransportSecurity</key>
<dict>
	<key>NSAllowsArbitraryLoads</key>
	<true/>
</dict>
```

- If you need stricter ATS, prefer domain-scoped exceptions or `NSAllowsArbitraryLoadsInWebContent`.

---

## 3  Dynamic URL
The host app must provide a full exhibitor URL at runtime:

- Structure: `https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{badgeId}`
- Pass this string into the WebView screen or Safari fallback.

---

## 4  Two approaches at runtime

### 4.1  iOS 15+ — Embedded `WKWebView`
Why? From iOS 15, WebKit provides an API to prompt for camera/mic permission inside a `WKWebView`.

Key configuration in `WKWebViewConfiguration`:

```swift
configuration.allowsInlineMediaPlayback = true
configuration.mediaPlaybackRequiresUserAction = false
configuration.mediaTypesRequiringUserActionForPlayback = []
configuration.websiteDataStore = .default()
configuration.defaultWebpagePreferences.allowsContentJavaScript = true
```

Permission prompt (WebRTC) in the coordinator:

```swift
@available(iOS 15.0, *)
func webView(_ webView: WKWebView,
             requestMediaCapturePermissionFor origin: WKSecurityOrigin,
             initiatedByFrame frame: WKFrameInfo,
             type: WKMediaCaptureType,
             decisionHandler: @escaping (WKPermissionDecision) -> Void) {
    decisionHandler(.prompt)
}
```

Where this lives: `WebviewIntegration/WebViewContainer.swift`

### 4.2  iOS 14 and under — `SFSafariViewController`
Why? Older iOS versions don’t expose the WebKit capture permission hook. Using Safari provides a consistent, secure user experience with working camera/mic prompts.

Runtime branching example:

```swift
if #available(iOS 15, *) {
    // Push in-app WebView screen
} else {
    // Present SFSafariViewController with the same URL
}
```

Where this lives: `WebviewIntegration/ContentView.swift` (navigation vs. `.sheet` with `SafariView`).

---

## 5  Minimal screens (how it’s wired)

- **Launcher screen** (`ContentView`): accepts a URL string, normalizes it, and then
  - iOS 15+: navigates to `WebViewScreen`
  - iOS 14-: presents `SafariView`
- **WebView screen** (`WebViewScreen`): hosts `WebViewContainer`
- **WebView wrapper** (`WebViewContainer`): builds the configured `WKWebView`, sets delegates, and loads the target URL

---

## 6  Domain handling and navigation
- The sample enables back/forward gestures in `WKWebView`.
- If you need to keep internal links in-app but open external domains in Safari, implement `WKNavigationDelegate` decision handling (optional, not required for basic embedding).

---

## 7  Quick checklist
- **Info.plist**: `NSCameraUsageDescription`, `NSMicrophoneUsageDescription` (required)
- **Networking**: ATS configured (sample uses `NSAllowsArbitraryLoads = true`)
- **iOS 15+**: Use `WKWebView` with media playback and WebKit permission handler
- **iOS 14-**: Use `SFSafariViewController` fallback
- **Pass dynamic URL**: `https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{badgeId}`

---

## 8  File map (for reference)
- `ios/WebviewIntegration/WebviewIntegrationApp.swift` — App entry
- `ios/WebviewIntegration/ContentView.swift` — URL input + routing (WebView vs Safari)
- `ios/WebviewIntegration/WebViewScreen.swift` — Hosts the WebView
- `ios/WebviewIntegration/WebViewContainer.swift` — `WKWebView` configuration + permission
- `ios/WebviewIntegration/SafariView.swift` — `SFSafariViewController` wrapper
- `ios/WebviewIntegration/Info.plist` — Privacy permissions and ATS
