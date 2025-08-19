# Android WebView Integration – Step-by-Step Guide

This guide shows **exactly** what to add or change _and_ **why** each step is required so the client understands every decision.

Package: `com.webview_integration.android`  
Min SDK **21** (Android 5.0) — earliest version that supports modern Chromium-based WebView with WebRTC and third-party cookies.

Target URL pattern: `https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{userId}`

Target URL example: `https://exhibitors-dev.roziesynopsis.com/e/DTM2025/u/201183`

---

## 1  Add core dependencies (Gradle)
Only the core AndroidX libraries are required – the screen is now just a full-screen WebView.

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.webview_integration.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.webview_integration.android"
        minSdk = 21           // WebRTC & file-chooser supported from 21
        targetSdk = 35        // always target latest API
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
}

dependencies {
    implementation(libs.androidx.core.ktx)          // Kotlin helpers
    implementation(libs.androidx.appcompat)          // AppCompat theme + ActionBar
    implementation(libs.material)                    // Material buttons / text fields
    implementation(libs.androidx.activity)           // Activity-KTX helpers
}
```

---

## 2  Declare permissions & activities (Manifest)
Why? WebView itself only needs Internet, but the website records audio/video and uploads files, so we request CAMERA + RECORD_AUDIO.  
Hardware `<uses-feature … required="false"/>` keeps the app installable on devices without a camera/mic.

```xml
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- 2.1  Permissions -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>

    <!-- 2.2  Optional hardware so Play Store doesn’t block installs -->
    <uses-feature android:name="android.hardware.camera.any" android:required="false"/>
    <uses-feature android:name="android.hardware.microphone" android:required="false"/>

    <application android:theme="@style/Theme.Android">
        <!-- 2.3  Launcher (collects parameters) -->
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <!-- 2.4  WebView screen (NoActionBar theme) -->
        <activity android:name=".XmlWebview"
                  android:theme="@style/Theme.Android.WebView"/>
    </application>
</manifest>
```

---

## 3  Themes
Why? Main screen shows the ActionBar. WebView screen hides it for full-bleed content.

```xml
<!-- res/values/themes.xml -->
<style name="Theme.Android" parent="Theme.MaterialComponents.DayNight.DarkActionBar"/>
<style name="Theme.Android.WebView" parent="Theme.MaterialComponents.DayNight.NoActionBar"/>
```

---

## 4  Dynamic URL

The launcher (or any other entry point) must supply a **dynamic exhibitor URL** with the structure:

`https://exhibitors.roziesynopsis.com/e/{eventCode}/u/{badgeId}`

`eventCode` identifies the event, while `badgeId` (sometimes called `userId`) is unique for each attendee. How you collect or generate this URL is entirely up to the host application. The rest of this guide focuses on how the app passes that URL to the WebView screen and renders it securely.

---

## 5  WebView implementation (`XmlWebview.kt`)
Each block below explains **why** the code exists.

### 5.1  Intent factory (makes it easy to pass parameters)
```kotlin
companion object {
    fun createIntent(ctx: Context, url: String) =
        Intent(ctx, XmlWebview::class.java).apply {
            putExtra("EXTRA_URL", url)
        }
}
```

### 5.2  Domain allow-listing
Why? Keep internal links in-app, push external links to Chrome.
```kotlin
override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?): Boolean {
    val host = req?.url?.host ?: return false
    return if (host.endsWith("roziesynopsis.com")) false else {
        startActivity(Intent(Intent.ACTION_VIEW, req.url))
        true
    }
}
```

### 5.3  Camera & microphone (WebRTC `getUserMedia`)
Why? Grant only after runtime permissions are approved.
```kotlin
override fun onPermissionRequest(request: PermissionRequest?) {
    val wants = mutableListOf<String>()
    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE in request!!.resources &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != GRANTED) wants += Manifest.permission.CAMERA
    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE in request.resources &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != GRANTED) wants += Manifest.permission.RECORD_AUDIO
    if (wants.isEmpty()) request.grant(request.resources) else permissionLauncher.launch(wants.toTypedArray())
}
```
---



