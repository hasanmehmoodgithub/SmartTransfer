package com.smart.transfer.app.features.localshare.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.smart.transfer.app.R
import com.smart.transfer.app.features.androidtoios.ui.AndroidToIosActivity
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import com.smart.transfer.app.features.localshare.ui.hotspot.ReceiverHotSpotActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.SenderHotSpotActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.SenderHttpActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.WebViewActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.WebViewSender
import com.smart.transfer.app.features.localshare.ui.recevier.ReceiverQrActivity
import com.smart.transfer.app.features.localshare.ui.sender.SenderQrActivity
import com.smart.transfer.app.features.localshare.ui.wifdirect.WiFiDirectActivity
import com.smart.transfer.app.features.localshare.ui.wifdirect.WiFiDirectActivity2
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class LocalShareTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_share_test)
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        Log.e("FileActivity", "Paths: $paths")

        // Example: Loop through each path

    }



    fun onClickReceiveWifiDirect(view: View) {
        startActivity(Intent(this, WiFiDirectActivity::class.java))
    }
    fun onClickSendWifiDirect(view: View) {
        startActivity(Intent(this, WiFiDirectActivity2::class.java))
    }
    fun onClickSendHotSpot(view: View) {
        startActivity(Intent(this, SenderHotSpotActivity::class.java))

    }
    fun onClickReceiveHotSpot(view: View) {
        startActivity(Intent(this, ReceiverHotSpotActivity::class.java))
    }
    fun onClickSendQR(view: View) {
        startActivity(Intent(this, SenderQrActivity::class.java))
    }
    fun onClickReceiveQR(view: View) {
        startActivity(Intent(this, ReceiverQrActivity::class.java))
    }
    fun onClickSendWebQR(view: View) {
       // startActivity(Intent(this, WebViewSender::class.java))
        startActivity(Intent(this, SenderHttpActivity::class.java))


    }
    fun onClickWebQR(view: View) {
        startActivity(Intent(this, WebViewActivity::class.java))

    }
}