package com.webview_integration.android

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class XmlWebview : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra.URL"

        fun createIntent(ctx: Context, url: String) =
            Intent(ctx, XmlWebview::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
    }

    private lateinit var webView: WebView
    // Progress bar & pull-to-refresh removed – simple full-screen WebView now

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var pendingPermissionRequest: PermissionRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the WebView programmatically; no XML layout required
        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(webView)

        WebView.setWebContentsDebuggingEnabled(true)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            pendingPermissionRequest?.let { req ->
                val needsVideo = req.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                val needsAudio = req.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)
                val videoOk = !needsVideo || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                val audioOk = !needsAudio || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (videoOk && audioOk) req.grant(req.resources) else req.deny()
                pendingPermissionRequest = null
            }
        }

        configureWebView(savedInstanceState)
        setupBackNavigation()
    }

    private fun configureWebView(savedInstanceState: Bundle?) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = false
            displayZoomControls = false
            // Allow getUserMedia without requiring an extra user tap
            mediaPlaybackRequiresUserGesture = false
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                val host = url.host ?: return false
                val inOwnDomain = host.endsWith("roziesynopsis.com")
                return if (inOwnDomain) {
                    false
                } else {
                    try { startActivity(Intent(Intent.ACTION_VIEW, url)) } catch (_: ActivityNotFoundException) {}
                    true
                }
            }

            // Progress bar & swipe refresh callbacks removed

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    view?.loadData(
                        """
                        <html><body style='font-family:sans-serif;padding:24px'>
                        <h3>Something went wrong</h3>
                        <p>Check your internet connection and try again.</p>
                        </body></html>
                        """.trimIndent(),
                        "text/html", "utf-8"
                    )
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            // onProgressChanged removed – no progress UI

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null) return
                val wants = mutableListOf<String>()
                if (request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) &&
                    ContextCompat.checkSelfPermission(this@XmlWebview, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    wants += Manifest.permission.CAMERA
                }
                if (request.resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE) &&
                    ContextCompat.checkSelfPermission(this@XmlWebview, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    wants += Manifest.permission.RECORD_AUDIO
                }
                if (wants.isEmpty()) {
                    request.grant(request.resources)
                } else {
                    pendingPermissionRequest = request
                    permissionLauncher.launch(wants.toTypedArray())
                }
            }
        }

        // Pull-to-refresh removed

        if (savedInstanceState == null) {
            val url = intent.getStringExtra(EXTRA_URL) ?: "https://exhibitors-dev.roziesynopsis.com"
            webView.loadUrl(url)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    // buildExhibitorUrl removed as URL is now passed directly via Intent
}