package com.smart.transfer.app.features.remoltyshare.ui.view


import android.content.Intent
import android.net.Uri
import android.os.Bundle

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.ui.viewmodel.UploadViewModel
import com.smart.transfer.app.databinding.ActivityUploadTestBinding

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadTestBinding


    private var selectedFiles: MutableList<Uri> = mutableListOf()
    private var uploadedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadTestBinding
            .inflate(layoutInflater)
        setContentView(binding.root)

    }
}