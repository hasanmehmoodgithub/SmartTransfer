package com.smart.transfer.app.features.androidtoios.ui

import androidx.appcompat.app.AppCompatActivity


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import android.graphics.drawable.BitmapDrawable
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.kotlintest.mobileToPc.MultipleFileServer
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.databinding.ActivityAndroidToIosBinding
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
        binding.icQr.setOnClickListener {
            showQrDialog(binding.ipText.text.toString());
        }
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        val fileList = paths.map { File(it) }

        startFileServerList(fileList)

    }

//    fun onClickStartServer(view: View) {
//        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
//        val fileList = paths.map { File(it) }
//
//        startFileServerList(fileList)
//    }
//
//    fun onClickStopServer(view: View) {
//        try {
//            fileServerMulti?.stop()
//            setupStopServerUi()
//        } catch (e: Exception) {
//            setupStartServerUi()
//        }
//    }

    fun onClickCopyIpText(view: View) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ip Text", binding.ipText.text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Link Copied to clipboard", Toast.LENGTH_SHORT).show()
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
//        binding.starButton.visibility = View.GONE
//        binding.stopButton.visibility = View.VISIBLE
        binding.downloadText.visibility = View.VISIBLE

        binding.ipLinkLayout.visibility = View.VISIBLE

        binding.icQr.setImageBitmap(generateQrCode(binding.ipText.text.toString()))
    }

    private fun setupStopServerUi() {
        isServerRunning = false
//        binding.starButton.visibility = View.VISIBLE
//        binding.stopButton.visibility = View.GONE
        binding.downloadText.visibility = View.GONE

        binding.ipLinkLayout.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        fileServerMulti?.stop()
        setupStopServerUi()
    }
    private fun showQrDialog(data: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr, null)
        val qrImageView = dialogView.findViewById<ImageView>(R.id.qrImageView)
        qrImageView.setImageBitmap(generateQrCode(data))

        AlertDialog.Builder(this)
            .setTitle("Scan this QR")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
    private fun generateQrCode(data: String): Bitmap {
        val size = 512
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }




}
