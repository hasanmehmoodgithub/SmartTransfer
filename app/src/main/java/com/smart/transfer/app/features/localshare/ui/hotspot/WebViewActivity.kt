package com.smart.transfer.app.features.localshare.ui.hotspot
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.webkit.WebView
import com.smart.transfer.app.R
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.ServerSocket

import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

import java.net.InetAddress


class WebViewActivity : AppCompatActivity() {

    private lateinit var viewFileButton: Button
    private lateinit var downloadAllButton: Button
    private lateinit var imageView: ImageView
    private lateinit var webView: WebView
    private var fileServerPort = 8080  // Example port

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // Initialize views
        viewFileButton = findViewById(R.id.viewFileButton)
        downloadAllButton = findViewById(R.id.downloadAllButton)
        imageView = findViewById(R.id.imageView)
        webView = findViewById(R.id.webView)

        // Get the list of files to host
        val files = getFilesToHost()
        if (files.isEmpty()) {
            Toast.makeText(this, "No files to host", Toast.LENGTH_SHORT).show()
            return
        }

        val fileServer = FileServer(files, fileServerPort)

        // Start the file server in a separate background thread
        Thread {
            fileServer.startServer()
        }.start()

        // Show the QR code with IP and Port
        val ipAddress = getLocalIpAddress()
        val qrCodeData = "http://$ipAddress:$fileServerPort"
        generateQRCode(qrCodeData)

        // Handle file viewing
        viewFileButton.setOnClickListener {
            val firstFile = files.firstOrNull()
            if (firstFile != null) {
                showFile(firstFile)
            } else {
                Toast.makeText(this, "No files to view", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle downloading all files
        downloadAllButton.setOnClickListener {
            downloadAllFiles(files)
        }
    }

    // Get local IP address
    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return InetAddress.getByAddress(
            byteArrayOf(
                (ip and 0xFF).toByte(),
                (ip shr 8 and 0xFF).toByte(),
                (ip shr 16 and 0xFF).toByte(),
                (ip shr 24 and 0xFF).toByte()
            )
        ).hostAddress
    }

    // Generate QR Code with IP and Port
    private fun generateQRCode(data: String) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)

            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }

            val qrImageView: ImageView = findViewById(R.id.qrImageView)  // Assume you have an ImageView for the QR code
            qrImageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun getFilesToHost(): List<String> {
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        return paths
    }

    private fun showFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val fileUri = Uri.fromFile(file)
            if (filePath.endsWith(".jpg") || filePath.endsWith(".png")) {
                imageView.setImageURI(fileUri)
                imageView.visibility = View.VISIBLE
                webView.visibility = View.GONE
            } else if (filePath.endsWith(".pdf")) {
                webView.loadUrl("http://localhost:$fileServerPort/${file.name}")
                webView.visibility = View.VISIBLE
                imageView.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadAllFiles(files: List<String>) {
        for (filePath in files) {
            val file = File(filePath)
            if (file.exists()) {
                val downloadPath = "/storage/emulated/0/Download/${file.name}"
                try {
                    copyFile(file, File(downloadPath))
                    Toast.makeText(this, "File ${file.name} downloaded", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Toast.makeText(this, "Error downloading file: ${file.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyFile(sourceFile: File, destFile: File) {
        try {
            if (!destFile.exists()) {
                destFile.createNewFile()
            }
            val inputStream = sourceFile.inputStream()
            val outputStream = FileOutputStream(destFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            Toast.makeText(this, "File copy failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // Inner class to handle the file server
    inner class FileServer(private val files: List<String>, private val port: Int) {

        fun startServer() {
            try {
                val serverSocket = ServerSocket(port)
                println("Server started on port $port")

                while (true) {
                    val clientSocket = serverSocket.accept()
                    val inputStream = clientSocket.getInputStream()
                    val outputStream = clientSocket.getOutputStream()

                    val buffer = ByteArray(1024)
                    inputStream.read(buffer)
                    val request = String(buffer)

                    val requestedFileName = request.substringAfter("GET /").substringBefore(" HTTP")
                    val requestedFile = files.find { it.contains(requestedFileName) }

                    if (requestedFile != null) {
                        val file = File(requestedFile)
                        val fileBytes = file.readBytes()
                        val response = "HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\n\r\n"
                        outputStream.write(response.toByteArray())
                        outputStream.write(fileBytes)
                    } else {
                        val response = "HTTP/1.1 404 Not Found\r\n\r\n"
                        outputStream.write(response.toByteArray())
                    }
                    clientSocket.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
