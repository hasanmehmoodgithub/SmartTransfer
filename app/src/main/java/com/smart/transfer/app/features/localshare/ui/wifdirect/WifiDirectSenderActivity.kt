package com.smart.transfer.app.features.localshare.ui.wifdirect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager

import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast

import com.smart.transfer.app.R
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream


import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

class WiFiDirectSenderActivity : AppCompatActivity() {
    private val PORT = 8888
    private val FILE_PICK_CODE = 102
    private lateinit var tvStatus: TextView
    private lateinit var btnSelectFiles: Button
    private lateinit var btnSendFiles: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var lvSelectedFiles: ListView
    private val selectedFiles = mutableListOf<Uri>()
    private lateinit var receiverIp: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_direct_sender)

//        tvStatus = findViewById(R.id.tvStatus)
//        btnSelectFiles = findViewById(R.id.btnSelectFiles)
//        btnSendFiles = findViewById(R.id.btnSendFiles)
//        progressBar = findViewById(R.id.progressBar)
//        tvProgress = findViewById(R.id.tvProgress)
//        lvSelectedFiles = findViewById(R.id.lvSelectedFiles)

//        receiverIp = intent.getStringExtra("RECEIVER_IP") ?: "192.168.49.1"
//
//        btnSelectFiles.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "*/*"
//                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
//            }
//            startActivityForResult(intent, FILE_PICK_CODE)
//        }
//
//        btnSendFiles.setOnClickListener {
//            sendFiles()
//        }

      //  checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICK_CODE && resultCode == RESULT_OK) {
            selectedFiles.clear()
            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    selectedFiles.add(data.clipData!!.getItemAt(i).uri)
                }
            } else if (data?.data != null) {
                selectedFiles.add(data.data!!)
            }
            updateFileList()
            btnSendFiles.isEnabled = selectedFiles.isNotEmpty()
        }
    }

    private fun updateFileList() {
        val fileNames = selectedFiles.map { uri ->
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: uri.path?.substringAfterLast('/') ?: "Unknown"
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        lvSelectedFiles.adapter = adapter
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
        }
    }
}