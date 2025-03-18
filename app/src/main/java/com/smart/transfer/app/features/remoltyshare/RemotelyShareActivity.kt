package com.smart.transfer.app.features.remoltyshare


import android.view.View
import android.widget.LinearLayout
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts


class RemotelyShareActivity : BaseActivity() {


    // Register a launcher for requesting permissions (recommended for modern Android development)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted
                proceedAfterPermissionGranted()
            } else {
                // Permission denied
                handlePermissionDenied()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remotely_share)
        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Remote sharing", showBackButton = true)

        checkStoragePermission()


    }

    private fun checkStoragePermission() {
        when {
            // Check if permission is already granted
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                proceedAfterPermissionGranted()
            }
            // For Android 10 and above, handle Scoped Storage
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                handleScopedStorage()
            }
            // For Android 6.0 (API 23) and above, request runtime permission
            else -> {
                requestStoragePermission()
            }
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explain why the permission is needed
            showPermissionRationale()
        } else {
            // Request the permission
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun handleScopedStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) and above requires MANAGE_EXTERNAL_STORAGE for broad access
            if (Environment.isExternalStorageManager()) {
                proceedAfterPermissionGranted()
            } else {
                // Request MANAGE_EXTERNAL_STORAGE permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            // Android 10 (API 29) uses Scoped Storage, but WRITE_EXTERNAL_STORAGE is still required
            requestStoragePermission()
        }
    }

    private fun proceedAfterPermissionGranted() {
        // Perform your storage-related operations here
        Log.d("StoragePermission", "Permission granted, proceed with storage operations")
    }

    private fun handlePermissionDenied() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Explain why the permission is needed
            showPermissionRationale()
        } else {
            // Permission denied permanently, guide the user to app settings
            showPermissionSettingsDialog()
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to save and access files.")
            .setPositiveButton("OK") { _, _ ->
                // Request permission again
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to proceed. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun onClickDownload(view: View) {
        startActivity(Intent(this, DownloadFileActivity::class.java))
    }
    fun onClickUpload(view: View) {
        startActivity(Intent(this, UploadingFilesActivity::class.java))
    }
}