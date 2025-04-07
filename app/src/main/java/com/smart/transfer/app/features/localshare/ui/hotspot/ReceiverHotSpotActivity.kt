package com.smart.transfer.app.features.localshare.ui.hotspot



import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
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
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

class ReceiverHotSpotActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var qrCodeImageView: ImageView
    private val hotspotPort = 8989

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_hot_spot)

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        setupHotspot()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupHotspot() {
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
                super.onStarted(reservation)
                // Hotspot started successfully
                val wifiConfiguration = reservation.wifiConfiguration
                val ssid = wifiConfiguration?.SSID
                val password = wifiConfiguration?.preSharedKey

                // Generate QR code with these details
                if (ssid != null) {
                    if (password != null) {
                        generateQrCode(ssid, password)
                    }
                }
            }

            override fun onFailed(reason: Int) {
                // Handle hotspot failure
               // showError("Hotspot failed to start: $reason")
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun getLocalIpAddress(): String {
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun generateQrCode(data: String,pass: String) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }

            qrCodeImageView.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun startFileServer(port: Int) {
        Thread {
            try {
                val serverSocket = ServerSocket(port)
                while (true) {
                    val clientSocket = serverSocket.accept()
                    handleClientConnection(clientSocket)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleClientConnection(clientSocket: Socket) {
        try {
            val inputStream = clientSocket.getInputStream()
            val outputStream = clientSocket.getOutputStream()

            // Simple protocol: first receive filename, then file content
            val filename = DataInputStream(inputStream).readUTF()
            val file = File(getExternalFilesDir(null), filename)

            FileOutputStream(file).use { fileOut ->
                inputStream.copyTo(fileOut)
            }

            runOnUiThread {
                Toast.makeText(this, "File received: $filename", Toast.LENGTH_SHORT).show()
            }

            clientSocket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


