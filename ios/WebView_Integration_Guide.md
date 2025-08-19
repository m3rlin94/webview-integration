# iOS WebView Integration – Step-by-Step Guide

This guide shows **exactly** what to add or change _and_ **why** each step is required so the client understands every decision.

Bundle ID: `com.webviewIntegration.ios`  
Minimum iOS **15** — earliest version that supports SwiftUI NavigationStack, WKWebView pull-to-refresh and modern WebRTC.

Target URL pattern: `https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{userId}`  
Target URL example: `https://exhibitors.roziesynopsis.com/e/DTM2025/u/201183`

---

## 1  Create the Xcode project
**Why?** We use a pure SwiftUI target so there are no storyboards to maintain.

1. Open **Xcode 15** (or later) → **File ▸ New ▸ Project…** → **App** (iOS tab).  
2. Language = **Swift**, Interface = **SwiftUI**. Disable tests for brevity.  
3. Save inside `ios/` so the structure becomes:

```text
webview-integration/
└─ ios/
   └─ WebviewIntegration/
       └─ (Xcode project files)
```

---

## 2  Add core source files
**Why?** These files provide the launcher UI, WebView, progress bar, pull-to-refresh, file upload, idle-timer handling and back-swipe navigation.

Drag the following files from the repository into Xcode, tick **Copy items if needed** and add them to the app target:

```text
WebviewIntegrationApp.swift   // SwiftUI @main entry point
ContentView.swift             // URL input + NavigationStack
WebViewModel.swift            // ObservableObject wrapper around WKWebView
WebViewContainer.swift        // WKWebView + progress + refresh control
```

> Delete the template `ContentView.swift` and `YourAppApp.swift` Xcode generated so they don’t collide with the provided versions.

---

## 3  Configure `Info.plist`
**Why?** The website requests camera/mic and file uploads. These keys enable the system permission prompts. `NSAllowsArbitraryLoads` lets you hit dev URLs without HTTPS pinning during testing.

```xml
<!-- Info.plist (inside <dict>) -->
<key>NSCameraUsageDescription</key>
<string>Needed to capture photos in the embedded WebView.</string>
<key>NSMicrophoneUsageDescription</key>
<string>Needed to record audio/video in WebView forms.</string>

<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

---

## 4  Dynamic URL
The launcher must supply a **dynamic exhibitor URL** with the structure:

`https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{badgeId}`

How you obtain or generate this URL is beyond the scope of this guide (deep-link, API, QR scan, etc.). The remainder assumes you have the final URL string ready.

---

## 5  Launcher logic (`ContentView.swift`)
**Why?** Reads the full URL, validates blank input and pushes a `WebViewScreen` on the `NavigationStack`.

```swift
struct ContentView: View {
    @State private var urlText = "https://exhibitors-dev.roziesynopsis.com/e/DTM2025/u/201183"
    @State private var navigate = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                TextField("Full URL (https://…)", text: $urlText)
                    .textFieldStyle(.roundedBorder)
                    .textInputAutocapitalization(.never)
                    .keyboardType(.URL)

                NavigationLink("", isActive: $navigate) {
                    WebViewScreen(urlString: urlText)
                }

                Button("Open WebView") {
                    navigate = urlText.trimmingCharacters(in: .whitespaces).isEmpty == false
                }
                .buttonStyle(.borderedProminent)
            }
            .padding()
            .navigationTitle("WebView Demo")
        }
    }
}
```

---

## 6  WebView implementation
Each block explains **why** the code exists.

### 6.1  WKWebView configuration (`WebViewModel.swift`)
```swift
let prefs = WKWebpagePreferences()
prefs.allowsContentJavaScript = true           // enable JS

let config = WKWebViewConfiguration()
config.defaultWebpagePreferences = prefs
config.allowsInlineMediaPlayback = true        // <video playsinline>
config.mediaTypesRequiringUserActionForPlayback = []

let webView = WKWebView(frame: .zero, configuration: config)
webView.allowsBackForwardNavigationGestures = true  // native swipe
```

### 6.2  Progress bar & pull-to-refresh (`WebViewContainer.swift`)
* Observe `estimatedProgress` → drive `ProgressView`.  
* Attach `UIRefreshControl` to `scrollView` → reload on pull.

```swift
progressObserver = webView.observe(\.estimatedProgress) { [weak self] _, change in
    self?.progress = change.newValue ?? 0
}
```

### 6.3  Domain allow-listing
**Why?** Keep internal links in-app, open external links in Safari.

```swift
func webView(_ webView: WKWebView,
            decidePolicyFor navAction: WKNavigationAction,
            decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
    if let host = navAction.request.url?.host,
       !host.hasSuffix("roziesynopsis.com") {
        UIApplication.shared.open(navAction.request.url!)
        decisionHandler(.cancel)
    } else {
        decisionHandler(.allow)
    }
}
```

### 6.4  Keep screen awake
**Why?** Prevent device from auto-locking during demos.

```swift
.onAppear { UIApplication.shared.isIdleTimerDisabled = true }
.onDisappear { UIApplication.shared.isIdleTimerDisabled = false }
```

_No additional camera/mic delegate code is needed—iOS shows the standard permission dialog based on the Info.plist keys above._

---

## 7  Feature parity checklist

| Feature                               | Android | iOS |
| ------------------------------------- | :-----: | :--: |
| Custom URL input                      |   ✅    | ✅ |
| WebView with JS, local storage, etc.  |   ✅    | ✅ |
| Progress bar                          |   ✅    | ✅ |
| Pull-to-refresh                       |   ✅    | ✅ |
| File picker + camera capture          |   ✅    | ✅ |
| Back/forward swipe in WebView         |   ✅    | ✅ |
| External links open in native browser |   ✅    | ✅ |
| Keep screen awake on WebView          |   ✅    | ✅ |

---

## 8  Next steps (optional)
* Display a custom HTML error page via `webView(_:didFail:)`.  
* Share cookies between multiple WKWebViews using a shared `WKProcessPool`.  
* Harden security by replacing `NSAllowsArbitraryLoads` with a domain-specific ATS allow-list once production hosts are final.

---

🎉  You now have a 100 % feature-parity native iOS WebView module that mirrors the Android implementation.
