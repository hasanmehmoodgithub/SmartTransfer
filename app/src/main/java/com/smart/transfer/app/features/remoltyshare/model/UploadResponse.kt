package com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val error: String?,
    val unique_id: String?
)
