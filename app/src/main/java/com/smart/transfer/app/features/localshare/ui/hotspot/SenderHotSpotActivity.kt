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
import android.util.Log
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
import java.util.concurrent.Executors

class SenderHotSpotActivity : AppCompatActivity() {
    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var cameraPermission: String
    private var hotspotInfo: HotspotInfo? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var isConnectedToHotspot = false
    private lateinit var wifiManager: WifiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_hot_spot)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        cameraPermission = Manifest.permission.CAMERA
        barcodeView = findViewById(R.id.barcode_view)

        checkPermissions()
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED -> {
                startScanning()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, cameraPermission) -> {
                Toast.makeText(this, "Camera permission is needed to scan QR codes", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), CAMERA_PERMISSION_REQUEST)
            }
            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(cameraPermission), CAMERA_PERMISSION_REQUEST)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startScanning()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun startScanning() {
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        barcodeView.initializeFromIntent(intent)
        barcodeView.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                if (result.text.startsWith(QR_CODE_PREFIX)) {
                    processQrCode(result.text)
                } else {
                    Toast.makeText(this@SenderHotSpotActivity, "Invalid QR code", Toast.LENGTH_SHORT).show()
                    barcodeView.resume()
                }
            }

            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {}
        })
        barcodeView.resume()
    }

    private fun processQrCode(qrContent: String) {
        val parts = qrContent.split(":")
        if (parts.size == 5) {
            hotspotInfo = HotspotInfo(
                ssid = parts[1],
                password = parts[2],
                ip = parts[3],
                port = parts[4].toInt()
            )
            connectToHotspot()
        } else {
            Toast.makeText(this, "Invalid hotspot information", Toast.LENGTH_SHORT).show()
            barcodeView.resume()
        }
    }

    private fun connectToHotspot() {
        hotspotInfo?.let { info ->
            try {
                // Save current WiFi configuration to restore later
                val previousNetworkId = wifiManager.connectionInfo.networkId

                val wifiConfig = WifiConfiguration().apply {
                    SSID = "\"${info.ssid}\""
                    preSharedKey = "\"${info.password}\""
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    status = WifiConfiguration.Status.ENABLED
                }

                // Remove any existing networks with same SSID
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                wifiManager.configuredNetworks
                    ?.firstOrNull { it.SSID == wifiConfig.SSID }
                    ?.let { wifiManager.removeNetwork(it.networkId) }

                val netId = wifiManager.addNetwork(wifiConfig)
                if (netId == -1) {
                    throw Exception("Failed to add network configuration")
                }

                if (!wifiManager.enableNetwork(netId, true)) {
                    throw Exception("Failed to enable network")
                }

                if (!wifiManager.reconnect()) {
                    throw Exception("Failed to reconnect")
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    verifyHotspotConnection(info, previousNetworkId)
                }, CONNECTION_CHECK_DELAY)

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Failed to connect to hotspot: ${e.message}",

                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("Failed To Connect","Failed to connect to hotspot: ${e.message}  /  ${e}")
                    barcodeView.resume()
                }
            }
        }
    }

    private fun verifyHotspotConnection(info: HotspotInfo, previousNetworkId: Int) {
        val connectionInfo = wifiManager.connectionInfo
        val currentSsid = connectionInfo.ssid?.removeSurrounding("\"")

        if (currentSsid == info.ssid) {
            isConnectedToHotspot = true
            Toast.makeText(this, "Connected to hotspot", Toast.LENGTH_SHORT).show()
            selectAndSendFile(info.ip, info.port)
        } else {
            // Restore previous connection
            wifiManager.enableNetwork(previousNetworkId, true)
            wifiManager.reconnect()
            Toast.makeText(
                this,
                "Failed to connect to hotspot. Current SSID: $currentSsid",
                Toast.LENGTH_LONG
            ).show()
            barcodeView.resume()
        }
    }

    private fun checkConnectionAndProceed(info: HotspotInfo) {
        if (wifiManager.connectionInfo.ssid?.removeSurrounding("\"") == info.ssid) {
            isConnectedToHotspot = true
            Toast.makeText(this, "Connected to hotspot", Toast.LENGTH_SHORT).show()
            selectAndSendFile(info.ip, info.port)
        } else {
            Toast.makeText(this, "Failed to connect to hotspot", Toast.LENGTH_SHORT).show()
            barcodeView.resume()
        }
    }

    private fun selectAndSendFile(ip: String, port: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "image/*", "video/*", "audio/*", "application/pdf", "text/plain"
            ))
        }
        startActivityForResult(intent, FILE_SELECT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val displayName = cursor.getString(
                            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                        )
                        val size = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))

                        hotspotInfo?.let { info ->
                            if (size > MAX_FILE_SIZE_BYTES) {
                                Toast.makeText(this, "File is too large (max ${MAX_FILE_SIZE_MB}MB)", Toast.LENGTH_SHORT).show()
                            } else {
                                sendFile(info.ip, info.port, uri, displayName)
                            }
                        }
                    }
                }
            }
        } else {
            barcodeView.resume()
        }
    }

    private fun sendFile(ip: String, port: Int, fileUri: Uri, filename: String) {
        executor.execute {
            try {
                Socket(ip, port).use { socket ->
                    val outputStream = socket.getOutputStream()
                    val dataOutputStream = DataOutputStream(outputStream)

                    // Send metadata first
                    dataOutputStream.writeUTF(filename)
                    dataOutputStream.writeLong(contentResolver.openInputStream(fileUri)?.use {
                        it.copyTo(outputStream)
                        it.available().toLong()
                    } ?: 0)

                    runOnUiThread {
                        Toast.makeText(this, "File sent successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to send file: ${e.message}", Toast.LENGTH_SHORT).show()
                    barcodeView.resume()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isConnectedToHotspot) {
            barcodeView.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    data class HotspotInfo(
        val ssid: String,
        val password: String,
        val ip: String,
        val port: Int
    )

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 101
        private const val FILE_SELECT_REQUEST = 102
        private const val CONNECTION_CHECK_DELAY = 3000L
        private const val QR_CODE_PREFIX = "HOTSPOT:"
        private const val MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024 // 50MB
        private const val MAX_FILE_SIZE_MB = 50
    }
}