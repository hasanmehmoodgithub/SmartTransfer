package com.smart.transfer.app

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class StorageActivity : AppCompatActivity() {

    private lateinit var totalStorageText: TextView
    private lateinit var usedStorageText: TextView
    private lateinit var availableStorageText: TextView
    private lateinit var photosStorageText: TextView
    private lateinit var videosStorageText: TextView
    private lateinit var musicStorageText: TextView
    private lateinit var documentsStorageText: TextView
    private lateinit var downloadsStorageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        totalStorageText = findViewById(R.id.totalStorage)
        usedStorageText = findViewById(R.id.usedStorage)
        availableStorageText = findViewById(R.id.availableStorage)
        photosStorageText = findViewById(R.id.photosStorage)
        videosStorageText = findViewById(R.id.videosStorage)
        musicStorageText = findViewById(R.id.musicStorage)
        documentsStorageText = findViewById(R.id.documentsStorage)
        downloadsStorageText = findViewById(R.id.downloadsStorage)

        // Fetch and display storage details
        displayStorageDetails()
    }

    private fun displayStorageDetails() {
        val (usedStorage, availableStorage, totalStorage) = getStorageDetails()

        totalStorageText.text = "Total Storage: ${formatSize(totalStorage)}"
        usedStorageText.text = "Used Storage: ${formatSize(usedStorage)}"
        availableStorageText.text = "Available Storage: ${formatSize(availableStorage)}"

        // Fetch individual category storage sizes
        photosStorageText.text = "Photos Storage: ${getFolderSize(Environment.DIRECTORY_PICTURES)}"
        videosStorageText.text = "Videos Storage: ${getFolderSize(Environment.DIRECTORY_MOVIES)}"
        musicStorageText.text = "Music Storage: ${getFolderSize(Environment.DIRECTORY_MUSIC)}"
        documentsStorageText.text = "Documents Storage: ${getFolderSize(Environment.DIRECTORY_DOCUMENTS)}"
        downloadsStorageText.text = "Downloads Storage: ${getFolderSize(Environment.DIRECTORY_DOWNLOADS)}"
    }

    private fun getStorageDetails(): Triple<Long, Long, Long> {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)

        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedBytes = totalBytes - freeBytes

        return Triple(usedBytes, freeBytes, totalBytes)
    }

    private fun getFolderSize(directory: String): String {
        val file = Environment.getExternalStoragePublicDirectory(directory)
        return if (file.exists()) formatSize(getSizeRecursive(file)) else "0 KB"
    }

    private fun getSizeRecursive(file: File): Long {
        if (file.isFile) return file.length()
        return file.listFiles()?.sumOf { getSizeRecursive(it) } ?: 0L
    }

    private fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb > 1 -> String.format("%.2f GB", gb)
            mb > 1 -> String.format("%.2f MB", mb)
            kb > 1 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }
}