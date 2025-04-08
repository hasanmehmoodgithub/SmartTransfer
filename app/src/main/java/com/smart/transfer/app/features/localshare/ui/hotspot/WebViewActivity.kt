package com.smart.transfer.app.features.localshare.ui.hotspot
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.smart.transfer.app.R
import kotlinx.coroutines.*
import okhttp3.OkHttpClient


import okhttp3.Request

import okhttp3.Response
import org.json.JSONArray

import java.io.IOException
import java.io.File
import java.io.FileOutputStream




class WebViewActivity : AppCompatActivity() {

    private lateinit var viewFileButton: Button
    private lateinit var downloadAllButton: Button
    private lateinit var imageView: ImageView
    private lateinit var webView: WebView
    private var fileServerPort = 8080  // Example port

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

         webView = findViewById(R.id.webView)

        // Enable JavaScript
        webView.settings.javaScriptEnabled = true

        // Set a WebViewClient to handle the navigation within the WebView
        webView.webViewClient = WebViewClient()

        // Set a WebChromeClient for handling JavaScript dialogs, favicons, etc.
        webView.webChromeClient = WebChromeClient()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // JavaScript to extract media URLs (images, videos, docs, music)
                val js = """
                    var mediaUrls = [];
                    var images = document.getElementsByTagName('img');
                    for (var i = 0; i < images.length; i++) {
                        mediaUrls.push(images[i].src);
                    }
                    var videos = document.getElementsByTagName('video');
                    for (var i = 0; i < videos.length; i++) {
                        mediaUrls.push(videos[i].src);
                    }
                    var links = document.getElementsByTagName('a');
                    for (var i = 0; i < links.length; i++) {
                        var href = links[i].href;
                        if (href.match(/\.(pdf|docx|pptx|mp3)$/)) {
                            mediaUrls.push(href);
                        }
                    }
                    mediaUrls;
                """

                // Execute JavaScript to get media URLs
                webView.evaluateJavascript(js) { value ->
                    val mediaUrls = value.substring(1, value.length - 1).split(",")
                    //downloadFiles(mediaUrls)
                }
            }
        }


        // Load a URL in the WebView

        val scanBtn: Button = findViewById(R.id.scanBtn)
        scanBtn.setOnClickListener(View.OnClickListener {
            startQrScanner()

        })
        // Start QR code scanning

    }
    private fun downloadFiles(mediaUrls: List<String>) {

        Log.e("files mediaUrls","${mediaUrls.size}")
        Log.e("files mediaUrls","${mediaUrls}")
        CoroutineScope(Dispatchers.IO).launch {
            mediaUrls.forEach { url ->
                // Clean up URL (remove any unnecessary quotes)
                val cleanedUrl = url.trim().removeSurrounding("\"")

                // Check if the URL is valid before attempting to download
                if (isValidUrl(cleanedUrl)) {
                    try {
                        Log.e("downloaded","$cleanedUrl")
                        val response = downloadFile(cleanedUrl)
                        if (response != null) {
                            saveFileToStorage(response, cleanedUrl)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "Error downloading: $cleanedUrl", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "All files downloaded", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun downloadFile(url: String): ByteArray? {
        // Create the request object using the given URL
        val request = Request.Builder().url(url).build()

        // Execute the request and get the response
        val response: Response
        try {
            val client = OkHttpClient()

            response = client.newCall(request).execute()
        } catch (e: IOException) {
            e.printStackTrace()  // Log the error if the request fails
            return null
        }

        // Check if the response is successful and return the file bytes
        return if (response.isSuccessful) {
            response.body?.bytes()
        } else {
            null  // Return null if the response is not successful
        }
    }
    // Save the downloaded file to storage
    private fun saveFileToStorage(fileData: ByteArray, url: String) {
        // Get the Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Ensure the directory exists
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        // Extract filename from URL (fallback to "downloadedFile" if invalid)
        val fileName = Uri.parse(url).lastPathSegment ?: "downloadedFile"
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(fileData)
            }

            // Show a Toast on success
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Saved to Downloads: $fileName",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d("Download", "Saved to: ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(
                    this,
                    "Failed to save: $fileName",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun startQrScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR code")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    // Handle the back button press to go back in WebView history
    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()  // Navigate back within WebView history
        } else {
            super.onBackPressed()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {

            if (result.contents != null) {
                fetchAndDownloadFiles((result.contents.toString()))
                try {

                    webView.loadUrl(result.contents.toString())

                } catch (e: Exception) {
                    Toast.makeText(this, "Error loading URL: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Toast.makeText(this, "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                // You can use the scanned data here
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Function to check if the content is a valid URL
    private fun isValidUrl(url: String?): Boolean {
        return try {
            val uri = Uri.parse(url)
            uri != null && uri.scheme != null && uri.host != null
        } catch (e: Exception) {
            false
        }
    }
    val client = OkHttpClient()


    fun fetchAndDownloadFiles(serverUrl: String) {
        Toast.makeText(this, "Start", Toast.LENGTH_LONG).show()
        // Launching the coroutine in the background thread
        GlobalScope.launch(Dispatchers.IO) {
            try {

                val request = Request.Builder().url("$serverUrl/files-json").build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val jsonArray = JSONArray(response.body?.string())
                        withContext(Dispatchers.Main) {
                            // Update UI on main thread with file download info
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val name = obj.getString("name")
                                val url = "$serverUrl${obj.getString("url")}"

                                println("Downloading: $name")
                                Log.e("Downloading","Downloading: $name")
                                // Call the download function on the background thread as well
                              //  downloadFile(url, File(downloadDir, name))
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            // Update UI to show failure
                            Log.e("Downloading Failed","Downloading:${response.code}")
                            println("Failed to get file list: ${response.code}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle any error on the main thread
                    e.printStackTrace()
                }
            }
        }
    }

}
