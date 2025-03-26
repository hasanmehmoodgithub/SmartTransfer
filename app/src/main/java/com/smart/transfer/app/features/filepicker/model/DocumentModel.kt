package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model

import android.net.Uri

data class DocumentModel(
    val id: Long,
    val name: String,
    val size: Long,
    val uri: Uri
)