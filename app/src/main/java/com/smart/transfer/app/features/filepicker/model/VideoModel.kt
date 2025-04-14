package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model


data class VideoModel(
    val name: String,
    val path: String,
    val duration: Long,
    val size: Long,
    var isSelected: Boolean = false // Maintains selection state
)
