# WebView Integration – iOS

This guide walks you through setting up the native iOS counterpart to the Android WebView demo.

> **Goal:** Provide a SwiftUI-based app that opens a configurable URL inside `WKWebView`, with support for pull-to-refresh, file uploads (camera / photo library), progress indication, and external-link hand-off to Safari.

## 1. Create an Xcode project

1. Open Xcode 15 (or later) and choose **File ▸ New ▸ Project…**.
2. Select **App** under the **iOS** tab.
3. Name it **WebviewIntegration** (or anything you like). Choose **Swift** & **SwiftUI**. Disable Core Data and Tests for now.
4. Save the project inside the repository’s `ios/` folder so the structure looks like:

```
webview-integration/
└─ ios/
   └─ WebviewIntegration/
       ├─ (Xcode project files)
       └─ …
```

> If you prefer to keep the `.xcodeproj` elsewhere, just make sure to add the Swift source files from `ios/WebviewIntegration/` into the target.

## 2. Replace template files with provided sources

Delete the auto-generated `ContentView.swift` and `YourAppApp.swift` that Xcode created, then drag-and-drop the following files from `ios/WebviewIntegration/` into the project, selecting **Copy items if needed** and adding them to the app target:

* `WebviewIntegrationApp.swift`
* `ContentView.swift`
* `WebViewModel.swift`
* `WebViewContainer.swift`
* `Info.plist` (replace the existing one)

## 3. Info.plist settings

The included `Info.plist` already contains camera, microphone, and photo-library usage descriptions, plus `NSAppTransportSecurity ▸ NSAllowsArbitraryLoads = YES` so any HTTPS (or dev HTTP) URL can be loaded. Tweak the messages to fit your app branding.

## 4. Run & test

1. Choose an iPhone simulator or connected device.
2. Build & run. The home screen shows a URL field pre-filled with the Exhibitors demo URL.
3. Tap **Open WebView** – the page loads with a progress bar at the top.
4. Pull down to refresh; back-swipe or the arrow button to navigate.
5. File inputs on the site will bring up iOS’ picker with options to capture a new photo or choose from the library.

## 5. Feature parity checklist

| Feature                               | Android | iOS (this app) |
| ------------------------------------- | :-----: | :------------: |
| Custom URL input                      |   ✅    |      ✅        |
| WebView with JS, local storage, etc.  |   ✅    |      ✅        |
| Progress bar                          |   ✅    |      ✅        |
| Pull-to-refresh                       |   ✅    |      ✅        |
| File picker + camera capture          |   ✅    |      ✅        |
| Back navigation within WebView        |   ✅    |      ✅        |
| External links open in native browser |   ✅    |      ✅        |

## 6. Next steps (optional)

* Add graceful error pages using `WKNavigationDelegate`’s `didFail` callbacks.
* Share cookies & state across multiple web views using custom `WKProcessPool`.
* Harden security by restricting `NSAppTransportSecurity` once you know production domains.

---

Feel free to adapt this code to your project structure. Happy coding! 🎉
