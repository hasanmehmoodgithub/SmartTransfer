package com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model

data class DownloadResponse(
    val success: Boolean,
    val id: String,
    val file_name: String,
    val file_type: String,
    val mime_type: String,
    val file_size: String,
    val uploaded_at: String,
    val download_link: String
)