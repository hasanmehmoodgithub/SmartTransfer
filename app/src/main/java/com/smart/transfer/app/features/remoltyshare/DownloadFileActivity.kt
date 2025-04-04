package com.smart.transfer.app.features.remoltyshare

import android.Manifest
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.*
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api.RetrofitClient
import com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.model.DownloadResponse
import com.smart.transfer.app.databinding.ActivityDownloadFileBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class DownloadFileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadFileBinding
    private lateinit var downloadManager: DownloadManager
    private var downloadId: Long = -1L

    // Request Code for Storage Permission
    private val STORAGE_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityDownloadFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.codePasteLayout.visibility = View.VISIBLE
        binding.downloadLayout.visibility = View.GONE

        // Handle Done Button Click
        binding.doneBtn.setOnClickListener {

            val uniqueId = binding.etLink.text.toString().trim()
            if (uniqueId.isNotEmpty()) {
                getZipFileLinkFromCode("qA3czNzsMZ")
                if (checkStoragePermission()) {
                  getZipFileLinkFromCode(uniqueId)

                } else {
                    requestStoragePermission()
                }
            } else {
                showErrorDialog("Please enter a valid link.")
            }
        }
    }

    // Check if storage permission is granted
    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request storage permission
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val uniqueId = binding.etLink.text.toString().trim()
                if (uniqueId.isNotEmpty()) {
                    getZipFileLinkFromCode(uniqueId)
                }
            } else {
                showErrorDialog("Storage permission denied.")
            }
        }
    }

    // Download file using Retrofit
    private fun getZipFileLinkFromCode(uniqueId: String) {
        binding.codePasteLayout.visibility = View.GONE
        binding.downloadLayout.visibility = View.VISIBLE
        binding.tvUploading.text = "Downloading..."
        binding.progressBar.progress = 0
        binding.tvPercentage.text = "0%"


        RetrofitClient.apiService.getFileDetails(uniqueId).enqueue(object : retrofit2.Callback<DownloadResponse> {
            override fun onResponse(call: Call<DownloadResponse>, response: retrofit2.Response<DownloadResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {

                        showSuccessDialog();
                       // downloadZipFileFromLink(body.download_link)

                        downloadZipFileFromLink("https://firebasestorage.googleapis.com/v0/b/testapp-e5995.appspot.com/o/test.zip?alt=media&token=6589ecdd-d405-4cea-8305-bd4186b3ce27",uniqueId)

                    } else {
                        showErrorDialog("File download failed: Empty response.")
                    }
                } else {
                    showErrorDialog("Failed to download file. Server error.")
                }
            }

            override fun onFailure(call: Call<DownloadResponse>, t: Throwable) {
                showErrorDialog("Download failed: ${t.message}")
            }
        })
    }
    private fun downloadZipFileFromLink(fileUrl: String, uniqueId: String) {
        binding.codePasteLayout.visibility = View.GONE
        binding.downloadLayout.visibility = View.VISIBLE
        binding.tvUploading.text = "Downloading..."
        binding.progressBar.progress = 0
        binding.tvPercentage.text = "0%"

        val client = OkHttpClient()
        val request = Request.Builder().url(fileUrl).build()

        client.newCall(request).enqueue(object :okhttp3. Callback {
            override fun onResponse(call:okhttp3. Call, response:okhttp3. Response) {
                response.body?.let { body ->
                    saveFileAndUnzip(body,uniqueId)
                } ?: runOnUiThread { showErrorDialog("Download failed: Empty response.") }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread { showErrorDialog("Download failed: ${e.message}") }
            }
        })
    }
    private fun showSuccessDialog() {
        runOnUiThread {
            showCongratsScreen()
            Snackbar.make(binding.root, "Downloaded", Snackbar.LENGTH_LONG).show()
        }
    }
    private fun showCongratsScreen() {
        binding.tvWaitMessage.text = ""
        binding.tvUploading.text ="Download Completed"
    }
    // Save the file locally and unzip it
    private fun saveFileAndUnzip(responseBody: ResponseBody, uniqueId: String) {
        Thread {
            try {
                val zipFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "${uniqueId}_file.zip"
                )

                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(zipFile)
                val buffer = ByteArray(1024)
                var totalBytesRead: Long = 0
                val totalSize = responseBody.contentLength()
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    outputStream.write(buffer, 0, bytesRead)

                    // Update UI Progress
                    val progress = (totalBytesRead * 100 / totalSize).toInt()
                    runOnUiThread {
                        binding.progressBar.progress = progress
                        binding.tvPercentage.text = "$progress%"
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // Unzip the file
                unzipFile(zipFile)
            } catch (e: Exception) {
                runOnUiThread {
                    showErrorDialog("Error saving file: ${e.message}")
                }
            }
        }.start()
    }

    // Unzip the downloaded file
    private fun unzipFile(zipFile: File) {
        Thread {
            try {
                val outputDir = File(zipFile.parent, zipFile.nameWithoutExtension)
                if (!outputDir.exists()) outputDir.mkdirs()

                val zipInputStream = ZipInputStream(FileInputStream(zipFile))
                var entry: ZipEntry?

                while (zipInputStream.nextEntry.also { entry = it } != null) {
                    val outputFile = File(outputDir, entry!!.name)

                    // Ensure parent directories exist
                    outputFile.parentFile?.mkdirs()

                    val outputStream = FileOutputStream(outputFile)
                    val buffer = ByteArray(1024)
                    var length: Int

                    while (zipInputStream.read(buffer).also { length = it } > 0) {
                        outputStream.write(buffer, 0, length)
                    }

                    outputStream.close()
                    zipInputStream.closeEntry()

                    // Sync extracted file to the gallery (media scanner)
                    MediaScannerConnection.scanFile(
                        this@DownloadFileActivity,
                        arrayOf(outputFile.absolutePath),
                        null
                    ) { path, uri ->
                        println("Scanned $path -> URI: $uri")  // Debugging log
                    }
                }

                zipInputStream.close()

                runOnUiThread {
                    showSuccessDialog("File downloaded and extracted successfully!")
                    binding.tvUploading.text = "Download Complete!"
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showErrorDialog("Error extracting file: ${e.message}")
                }
            }
        }.start()
    }


    // Show error dialog
    private fun showErrorDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }

    // Show success dialog
    private fun showSuccessDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .create()
                .show()
        }
    }
}

