package com.smart.transfer.app.features.remoltyshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity

class RemotelyShareActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remotely_share)
        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Remote sharing", showBackButton = true)

    }

    fun onClickDownload(view: View) {
        startActivity(Intent(this, DownloadFileActivity::class.java))
    }
    fun onClickUpload(view: View) {
        startActivity(Intent(this, UploadingFilesActivity::class.java))
    }
}