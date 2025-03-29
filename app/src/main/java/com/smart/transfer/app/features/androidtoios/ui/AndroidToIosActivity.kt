package com.smart.transfer.app.features.androidtoios.ui

import androidx.appcompat.app.AppCompatActivity


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.kotlintest.mobileToPc.MultipleFileServer
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.databinding.ActivityAndroidToIosBinding
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.File

class AndroidToIosActivity : BaseActivity() {

    private var fileServerMulti: MultipleFileServer? = null
    private val port = 8080
    private var isServerRunning = false

    private lateinit var binding: ActivityAndroidToIosBinding  // View Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAndroidToIosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Android to iOS", showBackButton = true)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupStopServerUi()
    }

    fun onClickStartServer(view: View) {
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        val fileList = paths.map { File(it) }

        startFileServerList(fileList)
    }

    fun onClickStopServer(view: View) {
        try {
            fileServerMulti?.stop()
            setupStopServerUi()
        } catch (e: Exception) {
            setupStartServerUi()
        }
    }

    fun onClickCopyIpText(view: View) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ip Text", binding.ipText.text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Link Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun startFileServerList(files: List<File>) {
        try {
            fileServerMulti?.stop()
            fileServerMulti = MultipleFileServer(port, files)
            fileServerMulti?.start()

            val ipAddress = getLocalIpAddress()

            binding.ipText.text = "http://$ipAddress:$port/"
            setupStartServerUi()
        } catch (e: Exception) {
            setupStopServerUi()
            isServerRunning = false
        }
    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        return Formatter.formatIpAddress(ip)
    }

    private fun setupStartServerUi() {
        isServerRunning = true
        binding.starButton.visibility = View.GONE
        binding.stopButton.visibility = View.VISIBLE
        binding.downloadText.visibility = View.VISIBLE
        binding.msgText.text = "Paste the link in your browser and ensure that your Android and iOS devices are connected to the same WiFi network"

        binding.ipLinkLayout.visibility = View.VISIBLE
    }

    private fun setupStopServerUi() {
        isServerRunning = false
        binding.starButton.visibility = View.VISIBLE
        binding.stopButton.visibility = View.GONE
        binding.downloadText.visibility = View.GONE
        binding.msgText.text = "Before Starting the Server, Ensure your Android and iOS devices are on the same WiFi"
        binding.ipLinkLayout.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        fileServerMulti?.stop()
        setupStopServerUi()
    }
}
