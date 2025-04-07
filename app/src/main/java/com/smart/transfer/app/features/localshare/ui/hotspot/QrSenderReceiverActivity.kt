package com.smart.transfer.app.features.localshare.ui.hotspot

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

import com.google.zxing.integration.android.IntentIntegrator

import com.google.zxing.integration.android.IntentIntegrator.*
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE

import com.google.zxing.integration.android.IntentIntegrator.parseActivityResult

import com.google.zxing.integration.android.IntentIntegrator.parseActivityResult
import com.google.zxing.integration.android.IntentResult
import com.google.zxing.qrcode.encoder.QRCode
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody

import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface


class QrSenderReceiverActivity : AppCompatActivity() {

    private val port = 8080
    private lateinit var server: FileReceiverServer

    private var filePaths = listOf( // replace these with real file paths
        "/sdcard/Download/sample1.jpg",
        "/sdcard/Download/sample2.txt"
    )

    private val qrScanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
        } else {
            try {
                val json = JSONObject(result.contents)
                val ip = json.getString("ip")
                val port = json.getInt("port")
                sendFiles(ip, port)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val qrImage = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(600, 600)
        }

        val button = Button(this).apply {
            text = "Scan QR"
        }

        layout.addView(qrImage)
        layout.addView(button)
        setContentView(layout)

        when (intent.getStringExtra("mode")) {
            "receiver" -> {
                val ip = getLocalIpAddress() ?: "0.0.0.0"
                startServer()
                val qrInfo = JSONObject()
                qrInfo.put("ip", ip)
                qrInfo.put("port", port)

                qrImage.setImageBitmap(generateQrCodeBitmap(qrInfo.toString()))
                button.visibility = View.GONE
                Toast.makeText(this, "Receiver Mode: $ip:$port", Toast.LENGTH_SHORT).show()
            }

            "sender" -> {
                qrImage.visibility = View.GONE

                button.setOnClickListener {
//                    qrScanLauncher.launch(ScanOptions())
                    sendFiles("192.168.100.239", 8080);
                }
            }

            else -> {
                Toast.makeText(this, "Missing mode in intent: sender/receiver", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::server.isInitialized) server.stop()
    }

    private fun startServer() {
        server = FileReceiverServer(port)
        server.start()
    }
    fun generateQrCodeBitmap(content: String, size: Int = 600): Bitmap {
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val bitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size, size,
            hints
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun sendFiles(ip: String, port: Int) {
         filePaths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }


        val client = OkHttpClient()

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (path in filePaths) {
            val file = File(path)
            if (file.exists()) {
                builder.addFormDataPart(
                    "file", file.name,
                    file.asRequestBody("application/octet-stream".toMediaType())
                )
            }
        }

        val request = Request.Builder()
            .url("http://$ip:$port")
            .post(builder.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@QrSenderReceiverActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("Failed","Failed: ${e.message} ${e}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    Toast.makeText(this@QrSenderReceiverActivity, "Success: ${response.body?.string()}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    class FileReceiverServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            if (session.method == Method.POST) {
                val files = HashMap<String, String>()
                session.parseBody(files)
                for ((key, value) in files) {
                    Log.d("Receiver", "File param: $key = $value")
                }
                return newFixedLengthResponse("Files received!")
            }
            return newFixedLengthResponse("Send a POST request with files.")
        }
    }
}

