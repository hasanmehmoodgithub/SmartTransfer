package com.smart.transfer.app.features.localshare.ui.hotspot

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.smart.transfer.app.R
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.Socket


class SenderHotSpotActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var cameraPermission: String
    private var hotspotInfo: HotspotInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_hot_spot)

        cameraPermission = Manifest.permission.CAMERA
        barcodeView = findViewById(R.id.barcode_view)

        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), 1)
        } else {
            startScanning()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startScanning() {
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (result.text.startsWith("HOTSPOT:")) {
                    val parts = result.text.split(":")
                    if (parts.size == 5) {
                        hotspotInfo = HotspotInfo(
                            ssid = parts[1],
                            password = parts[2],
                            ip = parts[3],
                            port = parts[4].toInt()
                        )
                        connectToHotspotAndSendFile()
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {}
        })
        barcodeView.resume()
    }

    private fun connectToHotspotAndSendFile() {
        hotspotInfo?.let { info ->
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"${info.ssid}\""
                preSharedKey = "\"${info.password}\""
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            }

            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val netId = wifiManager.addNetwork(wifiConfig)

            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()

            Handler(Looper.getMainLooper()).postDelayed({
                selectAndSendFile(info.ip, info.port)
            }, 3000)
        }
    }

    private fun selectAndSendFile(ip: String, port: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayName = cursor.getString(
                            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                        )

                        hotspotInfo?.let { info ->
                            sendFile(info.ip, info.port, uri, displayName)
                        }
                    }
                }
            }
        }
    }

    private fun sendFile(ip: String, port: Int, fileUri: Uri, filename: String) {
        Thread {
            try {
                val socket = Socket(ip, port)
                val outputStream = socket.getOutputStream()
                val dataOutputStream = DataOutputStream(outputStream)

                dataOutputStream.writeUTF(filename)

                contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }

                socket.close()

                runOnUiThread {
                    Toast.makeText(this, "File sent successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "File send failed", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    data class HotspotInfo(
        val ssid: String,
        val password: String,
        val ip: String,
        val port: Int
    )
}