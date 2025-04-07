package com.smart.transfer.app.features.localshare.ui.recevier

import com.smart.transfer.app.R


import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class ReceiverQrActivity : AppCompatActivity() {
    private val PORT = 8080
    private lateinit var statusTextView: TextView
    private lateinit var ipTextView: TextView
    private lateinit var imgBarCode: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var filesCountText: TextView

    private var serverSocket: ServerSocket? = null
    private var serverThread: Thread? = null
    private val isRunning = AtomicBoolean(false)
    private lateinit var progressDialog: AlertDialog
    private var totalFiles = 0
    private var receivedFiles = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_qr)

        initViews()
        setupProgressDialog()
        getLocalIpAddress()

        serverThread = Thread { startServer() }
        serverThread?.start()
    }

    private fun initViews() {
        statusTextView = findViewById(R.id.statusTextView)
        ipTextView = findViewById(R.id.ip)
        progressBar = findViewById(R.id.progressBar)
        imgBarCode = findViewById(R.id.imgBarCode)
        progressText = findViewById(R.id.progressText)
        filesCountText = findViewById(R.id.filesCountText)
    }

    private fun setupProgressDialog() {
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Receiving Files")
            .setMessage("Waiting for incoming files...")
            .setNegativeButton("Stop Server") { _, _ ->
                stopServer()
            }
            .setCancelable(false)
            .create()
    }

    private fun generateQRCode(content: String): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                500,
                500
            )
            BarcodeEncoder().createBitmap(bitMatrix)
        } catch (e: Exception) {
            Log.e("QRCode", "Generation failed", e)
            null
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            NetworkInterface.getNetworkInterfaces()?.let { interfaces ->
                for (networkInterface in interfaces) {
                    for (address in networkInterface.inetAddresses) {
                        if (!address.isLoopbackAddress && address is Inet4Address) {
                            val ipAddress = address.hostAddress ?: continue
                            runOnUiThread {
                                ipTextView.text = ipAddress
                                imgBarCode.setImageBitmap(generateQRCode(ipAddress))
                                filesCountText.text = "Ready to receive files"
                            }
                            return ipAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("Network", "Failed to get IP address", ex)
        }
        return null
    }

    private fun startServer() {
        isRunning.set(true)
        try {
            serverSocket = ServerSocket(PORT)
            runOnUiThread {
                statusTextView.text = "Waiting for connection..."
                progressDialog.show()
            }

            while (!Thread.currentThread().isInterrupted && isRunning.get()) {
                serverSocket?.accept()?.let { clientSocket ->
                    runOnUiThread {
                        statusTextView.text = "Client connected!"
                        progressDialog.setMessage("Receiving files...")
                    }
                    receiveFiles(clientSocket)
                }
            }
        } catch (e: IOException) {
            if (serverSocket?.isClosed == false) {
                Log.e("Server", "Error in server thread", e)
                runOnUiThread {
                    statusTextView.text = "Error: ${e.message}"
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun receiveFiles(socket: Socket) {
        try {
            DataInputStream(socket.getInputStream()).use { dataInputStream ->
                // Verify connection by reading magic number first
                val magicNumber = dataInputStream.readInt()
                if (magicNumber != 0x4D475443) { // MGTC magic number
                    throw IOException("Invalid protocol header")
                }

                // Read total files count with timeout
                socket.soTimeout = 5000 // 5 second timeout
                totalFiles = dataInputStream.readInt()
                receivedFiles = 0

                runOnUiThread {
                    progressBar.max = totalFiles
                    filesCountText.text = "Receiving $totalFiles files"
                    progressDialog.setMessage("0/$totalFiles files received")
                }

                for (i in 1..totalFiles) {
                    if (!isRunning.get()) break

                    // Read file metadata with timeout
                    socket.soTimeout = 30000 // 30 seconds per file
                    val fileName = try {
                        dataInputStream.readUTF()
                    } catch (e: EOFException) {
                        throw IOException("Connection closed while reading filename", e)
                    }

                    val fileSize = try {
                        dataInputStream.readLong()
                    } catch (e: EOFException) {
                        throw IOException("Connection closed while reading file size", e)
                    }

                    runOnUiThread {
                        progressDialog.setMessage("Receiving file $i/$totalFiles\n$fileName")
                    }

                    // Prepare output file
                    val outputFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    ).also { file ->
                        // Create parent directories if needed
                        file.parentFile?.mkdirs()
                    }

                    // Receive file content
                    FileOutputStream(outputFile).use { fileOutputStream ->
                        BufferedOutputStream(fileOutputStream).use { outputStream ->
                            val buffer = ByteArray(8192)
                            var remaining = fileSize

                            while (remaining > 0 && isRunning.get()) {
                                val chunkSize = minOf(buffer.size, remaining.toInt())
                                val bytesRead = try {
                                    dataInputStream.read(buffer, 0, chunkSize)
                                } catch (e: EOFException) {
                                    throw IOException("Connection closed during file transfer", e)
                                }

                                if (bytesRead == -1) {
                                    throw EOFException("Unexpected end of stream")
                                }

                                outputStream.write(buffer, 0, bytesRead)
                                remaining -= bytesRead

                                // Update progress
                                runOnUiThread {
                                    progressBar.progress = i - 1
                                    progressText.text = "${i - 1}/$totalFiles files"
                                }
                            }
                        }
                    }

                    receivedFiles++
                    runOnUiThread {
                        progressBar.progress = i
                        progressText.text = "$i/$totalFiles files"
                        progressDialog.setMessage("$i/$totalFiles files received")
                        statusTextView.text = "Received: $fileName"
                    }
                }
            }

            runOnUiThread {
                statusTextView.text = "Successfully received $receivedFiles/$totalFiles files"
                progressDialog.dismiss()
            }

        } catch (e: SocketTimeoutException) {
            Log.e("FileTransfer", "Transfer timed out", e)
            runOnUiThread {
                statusTextView.text = "Error: Transfer timed out"
                progressDialog.dismiss()
            }
        } catch (e: IOException) {
            Log.e("FileTransfer", "Transfer failed", e)
            runOnUiThread {
                statusTextView.text = "Error: ${e.message}"
                progressDialog.dismiss()
            }
        } finally {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.w("Socket", "Error closing socket", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
    }

    private fun stopServer() {
        isRunning.set(false)
        try {
            serverSocket?.close()
            serverThread?.interrupt()
            runOnUiThread {
                statusTextView.text = "Server stopped"
                progressDialog.dismiss()
            }
        } catch (e: IOException) {
            Log.e("Server", "Error stopping server", e)
        }
    }
}