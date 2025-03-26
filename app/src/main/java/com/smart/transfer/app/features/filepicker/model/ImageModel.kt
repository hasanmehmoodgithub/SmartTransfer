package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model




data class ImageModel(
    val name: String,   // Image file name
    val path: String,   // Image file path
    val size: Long,     // Image file size in bytes
    var isSelected: Boolean = false
)