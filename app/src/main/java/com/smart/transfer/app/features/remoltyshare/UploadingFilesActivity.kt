package com.smart.transfer.app.features.remoltyshare
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smart.transfer.app.databinding.ActivityUploadingFilesBinding
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.File
import java.io.FileInputStream
import java.io.IOException

import java.util.zip.ZipInputStream

import java.io.*


import android.app.Dialog

import android.widget.ProgressBar
import android.widget.TextView
import com.smart.transfer.app.R
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class UploadingFilesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadingFilesBinding
    private lateinit var loadingDialog: Dialog // Dialog to show loading message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadingFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start upload animation and process selected files
        startUploadingAnimation()
        processSelectedFiles()
    }

    private fun startUploadingAnimation() {
        // Show uploading layout
        binding.uploadingLayout.visibility = View.VISIBLE
        binding.congratslayout.visibility = View.GONE

        val animator = android.animation.ValueAnimator.ofInt(0, 100)
        animator.duration = 3000 // 3 seconds
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            binding.progressBar.progress = progress
            binding.tvPercentage.text = "$progress%"

            // Show Congrats layout when progress reaches 100
            if (progress == 100) {
                showCongratsScreen()
            }
        }
        animator.start()
    }

    private fun showCongratsScreen() {
        binding.uploadingLayout.visibility = View.GONE
        binding.congratslayout.visibility = View.VISIBLE
    }

    // This function retrieves the file paths and processes them
    private fun processSelectedFiles() {
        // Create a loading dialog
        showLoadingDialog()

        // Retrieve the list of file paths from AllSelectedFilesManager
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }

        // Create a zip file to store the selected files
        val zipFile = File(filesDir, "uploaded_files.zip")

        // Create the zip file in a separate thread
        Thread {
            createZipFile(paths, zipFile)
            runOnUiThread {
                dismissLoadingDialog() // Dismiss loading dialog when zipping is done
            }
        }.start()

        // After creating the zip, you can upload the zip file
        uploadFile(zipFile)
    }

    // Method to create a zip file
    private fun createZipFile(filePaths: List<String>, zipFile: File) {
        try {
            val zipOutputStream = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))

            // Loop through each file path and add them to the zip
            filePaths.forEach { path ->
                val file = File(path)
                if (file.exists()) {
                    val entry = ZipEntry(file.name)
                    zipOutputStream.putNextEntry(entry)

                    // Read the file and write it to the zip output stream
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

            zipOutputStream.finish()
            zipOutputStream.close()

            println("Zip file created successfully: ${zipFile.absolutePath}")
          //  Toast.makeText(this, "Zip file created: ${zipFile.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error creating zip file")
            Toast.makeText(this, "Error creating zip file", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to upload file
    private fun uploadFile(file: File) {
        // Implement your file upload logic here
        println("Uploading zip file: ${file.name}")
        // Example: Call your upload API here
      //  Toast.makeText(this, "Uploading file: ${file.name}", Toast.LENGTH_SHORT).show()
    }

    // Method to show the loading dialog
    private fun showLoadingDialog() {
        // Create a new dialog
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false) // Prevent cancellation by tapping outside

        val progressBar = loadingDialog.findViewById<ProgressBar>(R.id.progressBar)
        val messageText = loadingDialog.findViewById<TextView>(R.id.tvMessage)

        // Set the message on the dialog
        messageText.text = "Getting file ready for upload..."

        // Show the dialog
        loadingDialog.show()
    }

    // Method to dismiss the loading dialog
    private fun dismissLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    fun onClickCopyIpText(view: View) {}
}
