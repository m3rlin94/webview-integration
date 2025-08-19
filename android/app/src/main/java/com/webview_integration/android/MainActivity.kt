package com.webview_integration.android

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUrl = findViewById<EditText>(R.id.etUrl)
        val btnXml = findViewById<Button>(R.id.btnXml)

        fun hideKeyboard() {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            currentFocus?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }
        }

        btnXml.setOnClickListener {
            hideKeyboard()
            // Retrieve the URL (or fall back to a sensible default)
            val url = etUrl.text.toString().ifBlank {
                "https://exhibitors-dev.roziesynopsis.com/e/DTM2025/u/201183"
            }
            startActivity(XmlWebview.createIntent(this, url))
        }
    }
}