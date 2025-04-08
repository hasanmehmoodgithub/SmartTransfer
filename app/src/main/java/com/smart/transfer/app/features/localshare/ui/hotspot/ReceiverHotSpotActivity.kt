package com.smart.transfer.app.features.localshare.ui.hotspot

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.smart.transfer.app.R
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class ReceiverHotSpotActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var qrCodeImageView: ImageView
    private lateinit var statusTextView: TextView
    private val executor = Executors.newSingleThreadExecutor()
    private var hotspotReservation: WifiManager.LocalOnlyHotspotReservation? = null
    private val hotspotPort = 8989
    private var serverSocket: ServerSocket? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_hot_spot)

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        statusTextView = findViewById(R.id.statusTextView)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        checkPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )

        val permissionsToRequest = requiredPermissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            setupHotspot()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupHotspot()
            } else {
                Toast.makeText(this, "Permissions are required for hotspot functionality", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupHotspot() {
        statusTextView.text = "Setting up hotspot..."

        // Check if hotspot is already enabled
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                startHotspot()
            }, 1000)
        } else {
            startHotspot()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startHotspot() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
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
            wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                    hotspotReservation = reservation

                    try {
                        val wifiConfiguration = reservation.wifiConfiguration
                        val ssid = wifiConfiguration?.SSID?.removeSurrounding("\"") ?: ""
                        val password = wifiConfiguration?.preSharedKey?.removeSurrounding("\"") ?: ""

                        if (ssid.isEmpty() || password.isEmpty()) {
                            throw Exception("Invalid hotspot configuration")
                        }

                        val ipAddress = getLocalIpAddress()
                        val qrContent = "HOTSPOT:$ssid:$password:$ipAddress:$hotspotPort"

                        runOnUiThread {
                            statusTextView.text = "Hotspot is ready\nSSID: $ssid\nPassword: $password"
                            generateQrCode(qrContent)
                            startFileServer()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            statusTextView.text = "Hotspot configuration error"
                            Toast.makeText(
                                this@ReceiverHotSpotActivity,
                                "Hotspot error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        reservation.close()
                    }
                }

                // ... rest of the callback methods remain the same ...
            }, Handler(Looper.getMainLooper()))
        } catch (e: Exception) {
            runOnUiThread {
                statusTextView.text = "Failed to start hotspot"
                Toast.makeText(
                    this@ReceiverHotSpotActivity,
                    "Cannot start hotspot: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun getHotspotError(reason: Int): String {
        return when (reason) {
            WifiManager.LocalOnlyHotspotCallback.ERROR_NO_CHANNEL -> "No channel available"
            WifiManager.LocalOnlyHotspotCallback.ERROR_GENERIC -> "Generic error"
            WifiManager.LocalOnlyHotspotCallback.ERROR_INCOMPATIBLE_MODE -> "Incompatible mode"
            WifiManager.LocalOnlyHotspotCallback.ERROR_TETHERING_DISALLOWED -> "Tethering disallowed"
            else -> "Unknown error"
        }
    }

    private fun getLocalIpAddress(): String {
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun generateQrCode(data: String) {
        executor.execute {
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }

                runOnUiThread {
                    qrCodeImageView.setImageBitmap(bmp)
                }
            } catch (e: WriterException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@ReceiverHotSpotActivity,
                        "Failed to generate QR code", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startFileServer() {
        executor.execute {
            try {
                serverSocket = ServerSocket(hotspotPort)
                runOnUiThread {
                    statusTextView.text = "Waiting for sender to connect..."
                }

                while (true) {
                    val clientSocket = serverSocket?.accept() ?: break
                    handleClientConnection(clientSocket)
                }
            } catch (e: IOException) {
                Log.e("ReceiverHotSpot", "Server error", e)
                runOnUiThread {
                    Toast.makeText(this@ReceiverHotSpotActivity,
                        "Server error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleClientConnection(clientSocket: Socket) {
        try {
            val inputStream = clientSocket.getInputStream()
            val dataInputStream = DataInputStream(inputStream)

            // Read metadata
            val filename = dataInputStream.readUTF()
            val fileSize = dataInputStream.readLong()

            // Create file in Downloads directory
            val downloadsDir = getExternalFilesDir(null) ?: filesDir
            val file = File(downloadsDir, filename)

            // Write file content
            FileOutputStream(file).use { fileOut ->
                var bytesRead: Int
                val buffer = ByteArray(8192)
                var totalBytesRead = 0L

                while (totalBytesRead < fileSize) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) break
                    fileOut.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                }
            }

            runOnUiThread {
                statusTextView.text = "File received: $filename"
                Toast.makeText(this@ReceiverHotSpotActivity,
                    "File saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }

            clientSocket.close()
        } catch (e: Exception) {
            Log.e("ReceiverHotSpot", "File transfer error", e)
            runOnUiThread {
                Toast.makeText(this@ReceiverHotSpotActivity,
                    "File transfer failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
        serverSocket?.close()
        hotspotReservation?.close()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}