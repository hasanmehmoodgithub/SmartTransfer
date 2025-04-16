package com.smart.transfer.app.com.smart.transfer.app.features.mobileToPc.ui

import android.net.wifi.WifiManager
import android.os.Bundle

import android.text.format.Formatter

import android.widget.Toast

import com.example.kotlintest.mobileToPc.MultipleFileServer
import com.smart.transfer.app.databinding.ActivityMobileToPcBinding
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager


import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MobileToPcActivity : BaseActivity() {

    private var fileServerMulti: MultipleFileServer? = null
    private val port = 8080
    private var isServerRunning = false

    private lateinit var binding: ActivityMobileToPcBinding  // View Binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobileToPcBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)

        setupAppBar(toolbar, getString(R.string.mobile_to_pc), showBackButton = true)

        // Start animation
//        binding.lottieAnimationView.playAnimation()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupStopServerUi()



    }
    fun onClickStartServer(view: View) {
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        Log.e("paths","$paths")

// Convert paths to File objects
        val fileList = paths.map { File(it) }

        startFileServerList(fileList)
    }
    fun onClickStopServer(view: View) {
        try {

            fileServerMulti?.stop()
            setupStopServerUi()

        } catch (e:Exception){
            setupStartServerUi()
        }


    }
    fun onClickCopyIpText(view: View) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ip Text", binding.ipText.text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Link Copied to clipboard", Toast.LENGTH_SHORT).show()
        ;

    }


    private fun startFileServerList(files: List<File>) {
        try {
            val iconDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher
            )

            val bitmap = if (iconDrawable is BitmapDrawable) {
                iconDrawable.bitmap
            } else {
                // Handle VectorDrawable
                val width = iconDrawable?.intrinsicWidth
                val height = iconDrawable?.intrinsicHeight
                val bitmap = width?.let { height?.let { it1 ->
                    Bitmap.createBitmap(it,
                        it1, Bitmap.Config.ARGB_8888)
                } }
                val canvas = bitmap?.let { Canvas(it) }
                if (width != null) {
                    height?.let { iconDrawable.setBounds(0, 0, width, it) }
                }
                canvas?.let { iconDrawable.draw(it) }
                bitmap
            }

            val iconFile = File(this.cacheDir, "app.png")
            try {
                val outputStream = FileOutputStream(iconFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            fileServerMulti?.stop()
            fileServerMulti = MultipleFileServer(port, files,iconFile)
            fileServerMulti?.start()

            val ipAddress = getLocalIpAddress()

            binding.ipText.text="http://$ipAddress:$port/"
            setupStartServerUi()
        }
        catch (e:Exception){
            setupStopServerUi()
            isServerRunning=false
        }

    }

    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        return Formatter.formatIpAddress(ip)
    }

    private fun setupStartServerUi(){
        isServerRunning=true
        binding.starButton.visibility=View.GONE
        binding.stopButton.visibility=View.VISIBLE
        binding.downloadText.visibility=View.VISIBLE
        binding.msgText.text="Paste the link in your browser and ensure that your mobile and PC are connected to the same WiFi network"

        binding.ipLinkLayout.visibility=View.VISIBLE

    }
    private fun setupStopServerUi(){
        isServerRunning=false
        binding.starButton.visibility=View.VISIBLE
        binding.stopButton.visibility=View.GONE
        binding.downloadText.visibility=View.GONE
        binding.msgText.text="Before Starting the Server Ensure your mobile and PC are on the same WiFi"
        binding.ipLinkLayout.visibility=View.GONE

    }


    override fun onDestroy() {
        super.onDestroy()

        fileServerMulti?.stop()
        setupStopServerUi()
    }




}
