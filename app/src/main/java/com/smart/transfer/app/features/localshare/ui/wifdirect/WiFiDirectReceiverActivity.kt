package com.smart.transfer.app.features.localshare.ui.wifdirect

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.smart.transfer.app.R
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.net.Socket


import android.content.pm.PackageManager

import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*


class WiFiDirectReceiverActivity : AppCompatActivity() {
    private val PORT = 8888
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var lvReceivedFiles: ListView
    private val receivedFiles = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_direct_recevier)

        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        lvReceivedFiles = findViewById(R.id.lvReceivedFiles)
        startReceiverServer()
       // checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 101)
        } else {

        }
    }

    private fun startReceiverServer() {
        Thread {
            try {
                val serverSocket = ServerSocket(PORT)
                runOnUiThread {
                    tvStatus.text = "Waiting for sender on port $PORT..."
                }

                while (true) {
                    val clientSocket = serverSocket.accept()
                    runOnUiThread {
                        tvStatus.text = "Sender connected!"
                        progressBar.visibility = android.view.View.VISIBLE
                    }

                    handleClientConnection(clientSocket)
                }
            } catch (e: IOException) {
                runOnUiThread {
                    tvStatus.text = "Server error: ${e.message}"
                }
            }
        }.start()
    }

    private fun handleClientConnection(clientSocket: Socket) {
        try {
            val input = DataInputStream(clientSocket.getInputStream())
            val fileCount = input.readInt()

            for (i in 0 until fileCount) {
                val fileName = input.readUTF()
                val fileSize = input.readLong()
                var bytesReceived = 0L

                val file = File(getExternalFilesDir(null), fileName)
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(1024)
                    var read: Int

                    while (bytesReceived < fileSize) {
                        read = input.read(buffer)
                        output.write(buffer, 0, read)
                        bytesReceived += read

                        runOnUiThread {
                            progressBar.progress = (bytesReceived * 100 / fileSize).toInt()
                            tvProgress.text = "Receiving $fileName: $bytesReceived/$fileSize bytes"
                        }
                    }
                }

                receivedFiles.add(file.absolutePath)
                runOnUiThread {
                    updateFileList()
                    Toast.makeText(this, "Received $fileName", Toast.LENGTH_SHORT).show()
                }
            }

            runOnUiThread {
                tvStatus.text = "All files received!"
                progressBar.visibility = android.view.View.GONE
            }
        } catch (e: Exception) {
            runOnUiThread {
                tvStatus.text = "Transfer error: ${e.message}"
            }
        } finally {
            clientSocket.close()
        }
    }

    private fun updateFileList() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, receivedFiles)
        lvReceivedFiles.adapter = adapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startReceiverServer()
        }
    }
}