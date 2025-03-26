package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model

data class VideoModel(
    val name: String,
    val path: String,
    val duration: Long,  // Duration in milliseconds
    val size: Long       // Size in bytes
)