package com.smart.transfer.app.features.remoltyshare

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api.ProgressRequestBody
import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api.RetrofitClient
import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model.UploadResponse
import com.smart.transfer.app.databinding.ActivityUploadingFilesBinding
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import kotlinx.coroutines.launch

import okhttp3.MultipartBody
import retrofit2.Call
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class UploadingFilesActivity : AppCompatActivity(), ProgressRequestBody.ProgressListener {

    private lateinit var binding: ActivityUploadingFilesBinding
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadingFilesBinding.inflate(layoutInflater)
        database = AppDatabase.getDatabase(this)

        setContentView(binding.root)

        binding.copyIpImg.setOnClickListener {
            copyToClipboard(binding.uniqueIdText.text.toString())
        }
        binding.icQr.setOnClickListener {
            showQrDialog(binding.uniqueIdText.text.toString());
        }

        processSelectedFiles()
        fadeInFadeOutProgressText()
    }

    private fun fadeInFadeOutProgressText() {
        val animation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 700
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }

        binding.tvUploading.startAnimation(animation)

    }
    override fun onProgressUpdate(progress: Int) {
        runOnUiThread {
            binding.progressBar.progress = progress
            binding.tvPercentage.text = "$progress%"
        }
    }

    private fun processSelectedFiles() {
        showLoadingDialog()
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        val zipFile = File(filesDir, "uploaded_files.zip")

        Thread {
            createZipFile(paths, zipFile)
            runOnUiThread {
                dismissLoadingDialog()
                uploadZipFile(zipFile)
            }
        }.start()
    }

    private fun createZipFile(filePaths: List<String>, zipFile: File) {
        try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOutputStream ->
                filePaths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val entry = ZipEntry(file.name)
                        zipOutputStream.putNextEntry(entry)
                        FileInputStream(file).use { input ->
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (input.read(buffer).also { length = it } > 0) {
                                zipOutputStream.write(buffer, 0, length)
                            }
                        }
                        zipOutputStream.closeEntry()
                    }
                }
            }
        } catch (e: IOException) {
            runOnUiThread { Toast.makeText(this, "Error creating zip file", Toast.LENGTH_SHORT).show() }
            e.printStackTrace()
        }
    }

    private fun uploadZipFile(file: File) {
        val progressRequestBody = ProgressRequestBody(file, this)
        val requestFile = MultipartBody.Part.createFormData("files", file.name, progressRequestBody)

        RetrofitClient.apiService.uploadFile(requestFile).enqueue(object : retrofit2.Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: retrofit2.Response<UploadResponse>) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    uploadResponse?.let {
                        if (it.success) {
                            showSuccessDialog(it.message, it.unique_id ?: "")
                        } else {
                            showErrorDialogWithBackBlock(it.error ?: "Upload failed")
                        }
                    }
                } else {
                    showErrorDialogWithBackBlock("Upload failed")
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                showErrorDialogWithBackBlock("Error: ${t.message}")
            }
        })
    }

    private fun showSuccessDialog(message: String, uniqueId: String) {
        runOnUiThread {
            showCongratsScreen(uniqueId)
            insertHistoryData()
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()

        }
    }
    private lateinit var database: AppDatabase

    private fun insertHistoryData() {
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }


        lifecycleScope.launch {
            paths.forEach { path ->
                val historyItem = History(
                    filePath = path,
                    fileType = getFileTypeFromPath(path), // Optional: get type based on file extension
                    tag = "remotely",
                    from = "send",
                    timestamp = System.currentTimeMillis()
                )
                database.historyDao().insertHistory(historyItem)
                Log.e("historyList", "Insert Success: ${historyItem.filePath} saved to Room DB")
            }
        }
    }
    private fun getFileTypeFromPath(path: String): String {
        return when {
            path.endsWith(".mp3", true) || path.endsWith(".wav", true) -> "music"
            path.endsWith(".mp4", true) || path.endsWith(".mkv", true) -> "video"
            path.endsWith(".jpg", true) || path.endsWith(".jpeg", true) || path.endsWith(".png", true) -> "image"
            path.endsWith(".pdf", true) || path.endsWith(".doc", true) || path.endsWith(".docx", true) ||
                    path.endsWith(".xls", true) || path.endsWith(".xlsx", true) || path.endsWith(".txt", true) -> "document"
            else -> "unknown"
        }
    }
    private fun showCongratsScreen(uniqueId: String) {
        binding.uploadingLayout.visibility = View.GONE
        binding.congratslayout.visibility = View.VISIBLE
        binding.uniqueIdText.text = uniqueId
    }

    private fun showErrorDialog(message: String) {
        runOnUiThread {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showErrorDialogWithBackBlock(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    return@setOnKeyListener true // Consume the back key
                }
                false
            }
        }

        dialog.show()
    }

    private fun showLoadingDialog() {
        loadingDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            findViewById<TextView>(R.id.tvMessage).text = "Getting file ready for upload..."
            show()
        }
    }

    private fun dismissLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    private fun showQrDialog(data: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_qr, null)
        val qrImageView = dialogView.findViewById<ImageView>(R.id.qrImageView)
        qrImageView.setImageBitmap(generateQrCode(data))

        AlertDialog.Builder(this)
            .setTitle("Scan this QR")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun generateQrCode(data: String): Bitmap {
        val size = 512
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
