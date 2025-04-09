package com.smart.transfer.app.features.localshare.ui.hotspot

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.databinding.ActivityReceiverHttpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL


class ReceiverHttpActivity : BaseActivity() {
    private var ipString: String=""
    private var hostString: String=""
    private lateinit var binding: ActivityReceiverHttpBinding
    private val fileList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiverHttpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupAppBar(binding.customToolbar.customToolbar, "Downloading File", showBackButton = true)


        binding.codePasteLayout.visibility = View.VISIBLE
        binding.downloadLayout.visibility = View.GONE
        binding.scanQr.setOnClickListener(View.OnClickListener {
            startQrScanner()

        })
        // Handle Done Button Click
        binding.doneBtn.setOnClickListener {

            val uniqueId = binding.etLink.text.toString().trim()
            if (uniqueId.isNotEmpty()) {
                val url = "http://$uniqueId:8080/"
                fetchFileList(url)



            } else {
                showErrorDialog("Please enter a valid link.")
            }
        }



//        binding.connectButton.setOnClickListener {
//            val serverIp = "192.168.100.14"
//
//            if (serverIp.isNotEmpty()) {
//                fetchFileList(serverIp)
//            } else {
//                Toast.makeText(this, "Please enter server IP", Toast.LENGTH_SHORT).show()
//            }
//        }
//        binding.dwload
//            .setOnClickListener {
//                downloadFile("192.168.100.14", fileList[1].toString())
//            }

    }


    private fun startQrScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR code")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                fetchFileList(result.contents.toString())

                Toast.makeText(this, "Scanned: ${result.contents}", Toast.LENGTH_LONG).show()
                // You can use the scanned data here
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    private fun extractIpFromUrl(url: String) {
         try {
            val uri = URI(url)
            uri.host // this will return the IP or domain
            ipString=uri.host.toString()
             hostString=url
        } catch (e: Exception) {

        }
    }
    private fun showErrorDialog(message: String) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()
                    binding.codePasteLayout.visibility = View.VISIBLE
                    binding.downloadLayout.visibility = View.GONE
                }
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
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss()
                    binding.codePasteLayout.visibility = View.VISIBLE
                    binding.downloadLayout.visibility = View.GONE
                    binding.loadingProgressBar.visibility = View.GONE
                }
                .create()
                .show()
        }
    }


    private fun fetchFileList(serverUrl: String) {


        extractIpFromUrl(serverUrl)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    binding.loadingProgressBar.visibility = View.VISIBLE
                    binding.downloadLayout.visibility=View.GONE
                    binding.codePasteLayout.visibility=View.GONE
                }

              //  val url = "http://$serverIp:8080/"
                val connection = URL(serverUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val html = connection.inputStream.bufferedReader().use { it.readText() }
                    val pattern = "<a href='/(.*?)'>".toRegex()
                    val files = pattern.findAll(html).map { it.groupValues[1] }.toList()

                    withContext(Dispatchers.Main) {
                        fileList.clear()
                        fileList.addAll(files)
                        Log.e(" fileList","$fileList")
                        binding.codePasteLayout.visibility=View.GONE
                        binding.downloadLayout.visibility=View.VISIBLE
                        binding.currenAndtotal.text="Total Files To be Downloaded is ${fileList.size}"
                        binding.loadingProgressBar.visibility = View.GONE
//                        runDownloadForListAsync()
                        downloadAllFilesSequentially();
                      //  binding.connectButton.setText("${fileList.size}")
                     //   binding.progressBar.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.downloadLayout.visibility=View.GONE
                        binding.codePasteLayout.visibility=View.VISIBLE
                        binding.currenAndtotal.text=""
                        binding.loadingProgressBar.visibility = View.GONE
                        Toast.makeText(this@ReceiverHttpActivity,
                            "Failed to Connect files: ${connection.responseCode}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.downloadLayout.visibility=View.GONE
                    binding.codePasteLayout.visibility=View.VISIBLE
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(this@ReceiverHttpActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    Log.e("fetchFileList","Error: ${e.message}")
                }
            }
        }
    }

    private fun downloadFile2(serverIp: String, fileName: String) {
        val destinationDir = getExternalFilesDir(null) ?: filesDir
        val destinationFile = File(destinationDir, fileName)

//        val progressDialog = ProgressDialog(this).apply {
//            setTitle("Downloading $fileName")
//            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
//            setCancelable(false)
//            show()
//        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "http://$serverIp:8080/$fileName"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val fileLength = connection.contentLength
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(destinationFile)

                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        withContext(Dispatchers.Main) {
                            val progress = (totalBytesRead * 100 / fileLength).toInt()
                            withContext(Dispatchers.Main) {
                                // progressDialog.dismiss()
                                binding.progressBar.progress=progress
                                binding.tvPercentage.text="$progress %"
                                binding.currenAndtotal.text="Total Files To be Downloaded is /${fileList.size}"
                             //binding.dwload.setText("$progress")
                            }

                        }
                    }

                    outputStream.close()
                    inputStream.close()

                    withContext(Dispatchers.Main) {
                       // progressDialog.dismiss()
                        Toast.makeText(this@ReceiverHttpActivity,
                            "File saved to ${destinationFile.path}",
                            Toast.LENGTH_LONG).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        //progressDialog.dismiss()
                        Toast.makeText(this@ReceiverHttpActivity,
                            "Download failed: ${connection.responseCode}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                 //   progressDialog.dismiss()
                    Toast.makeText(this@ReceiverHttpActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun downloadAllFilesSequentially() {
        CoroutineScope(Dispatchers.IO).launch {
            for ((index, fileName) in fileList.withIndex()) {
                downloadFile(ipString, fileName, index, fileList.size)
            }

            withContext(Dispatchers.Main) {
                showSuccessDialog("All files downloaded!");
                Toast.makeText(this@ReceiverHttpActivity, "All files downloaded!", Toast.LENGTH_LONG).show()
            }
        }
    }
    private suspend fun downloadFile(serverIp: String, fileName: String, index: Int, totalFiles: Int) {
        val destinationDir = getExternalFilesDir(null) ?: filesDir
        val destinationFile = File(destinationDir, fileName)

        try {
            val url = "http://$serverIp:8080/$fileName"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val fileLength = connection.contentLength
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(destinationFile)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    withContext(Dispatchers.Main) {
                        val progress = (totalBytesRead * 100 / fileLength).toInt()
                        binding.progressBar.progress = progress
                        binding.tvPercentage.text = "$progress %"
                        binding.currenAndtotal.text = "Downloading file ${index + 1} of $totalFiles"
                    }
                }

                outputStream.close()
                inputStream.close()
                try {
                    MediaScannerConnection.scanFile(
                        this@ReceiverHttpActivity,
                        arrayOf(destinationFile.absolutePath),
                        null,
                        null
                    )
                } catch (scanError: Exception) {
                    Log.e("MediaScanner", "Failed to scan file: ${scanError.message}")
                }
                withContext(Dispatchers.Main) {
//                    Toast.makeText(
//                        this@ReceiverHttpActivity,
//                        "Saved to ${destinationFile.path}",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ReceiverHttpActivity,
                        "Failed: ${connection.responseCode}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ReceiverHttpActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



}
