package com.smart.transfer.app.features.remoltyshare

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.ChooseFileNextScreenType
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.ui.ChooseFileActivity
import com.smart.transfer.app.databinding.ActivityRemotelyShareBinding
import com.smart.transfer.app.features.localshare.ui.HandlePermissionActivity

class RemotelyShareActivity : BaseActivity() {

    private lateinit var binding: ActivityRemotelyShareBinding  // Declare binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityRemotelyShareBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar using binding

        setupAppBar(binding.customToolbar.customToolbar, getString(R.string.remote_share), showBackButton = true)

        binding.downloadCard.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, DownloadFileActivity::class.java))
        })
        binding.uploadCard.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ChooseFileActivity::class.java)
            intent.putExtra("ChooseFileNextScreenType", ChooseFileNextScreenType.Remote) // Convert to String
            startActivity(intent)


        })
    }
}
