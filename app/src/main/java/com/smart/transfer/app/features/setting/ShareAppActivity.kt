package com.smart.transfer.app.features.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smart.transfer.app.R


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.LinearLayout

import android.widget.Toast


import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.databinding.ActivityShareAppBinding

class ShareAppActivity : BaseActivity() {

    private lateinit var binding: ActivityShareAppBinding
    private val appLink = "https://play.google.com/store/apps/details?id=com.smartdatatransfer.easytransfer.filetransfer.sendanydata.smartswitchmobile.copydata"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityShareAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Share App", showBackButton = true)

        // Generate QR Code
        val barcodeEncoder = BarcodeEncoder()
        val bitmap: Bitmap = barcodeEncoder.encodeBitmap(appLink, BarcodeFormat.QR_CODE, 400, 400)
        binding.qrCodeImage.setImageBitmap(bitmap)

        // Set app link text
        binding.tvAppLink.text = appLink

        // Copy link to clipboard
        binding.btnCopy.setOnClickListener { copyToClipboard(appLink) }
        binding.copyCard.setOnClickListener { copyToClipboard(appLink) }

        // Share link
        binding.shareCard.setOnClickListener { shareAppLink(appLink) }
        binding.doneBtn.setOnClickListener { finish() }

    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("App Link", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Link Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun shareAppLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link)
        startActivity(Intent.createChooser(intent, "Share via"))
    }
}
