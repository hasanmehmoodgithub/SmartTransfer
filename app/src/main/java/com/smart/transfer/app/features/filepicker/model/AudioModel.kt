package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model

import android.net.Uri

data class AudioModel(
    val id: Long,
    val name: String,
    val duration: Long,
    val size: Long,
    val uri: Uri,
    val filePath: String // Store the real file path
)