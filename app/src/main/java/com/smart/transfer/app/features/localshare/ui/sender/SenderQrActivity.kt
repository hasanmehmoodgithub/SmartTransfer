package com.smart.transfer.app.features.localshare.ui.sender


import com.smart.transfer.app.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import java.io.DataOutputStream
import java.io.File
import java.io.InputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class SenderQrActivity : AppCompatActivity() {
    private lateinit var ipEditText: EditText
    private lateinit var statusTextView: TextView
    private lateinit var qrBtn: Button
    private val PORT = 8080
    private var selectedFiles = mutableListOf<Uri>()
    private val isSending = AtomicBoolean(false)
    private val isCancelled = AtomicBoolean(false)
    private lateinit var progressDialog: Dialog

    private lateinit var qrCodeLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sender_qr)

        ipEditText = findViewById(R.id.ipEditText)
        statusTextView = findViewById(R.id.statusTextView)
        qrBtn=findViewById(R.id.qrBtn)
        val sendFileButton = findViewById<Button>(R.id.sendFileButton)
        setSelectedFilesFromPaths()
        // Initialize progress dialog
        progressDialog = AlertDialog.Builder(this)
            .setTitle("Sending Files")
            .setMessage("Preparing to send files...")
            .setNegativeButton("Cancel") { dialog, _ ->
                isCancelled.set(true)
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        sendFileButton.setOnClickListener {
            val ip = ipEditText.text.toString()
            if (ip.isNotEmpty() && selectedFiles.isNotEmpty()) {
                isSending.set(true)
                isCancelled.set(false)
                Thread { sendFiles(ip) }.start()
            } else {
                statusTextView.text = "Select files and enter an IP"
            }
        }
        qrBtn.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan a QR Code")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(true)
            qrCodeLauncher.launch(integrator.createScanIntent())
        }
        qrCodeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (intentResult.contents != null) {
                ipEditText.setText(intentResult.contents)
                Toast.makeText(this, "Scanned: ${intentResult.contents}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "QR Scan Cancelled", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to set the selected files from a list of file paths (strings)
    private fun setSelectedFilesFromPaths() {
        val filePaths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        selectedFiles.clear()
        filePaths.forEach {
            val fileUri = Uri.fromFile(File(it))
            selectedFiles.add(fileUri)
        }
        statusTextView.text = "Selected ${selectedFiles.size} files"
    }


    private fun sendFiles(serverIp: String) {
        runOnUiThread {
            // Show progress dialog when sending files starts
            progressDialog.show()
          //  progressDialog.setMessage("Sending 0/${selectedFiles.size}")
        }

        var successCount = 0
        var failedCount = 0

        for ((index, fileUri) in selectedFiles.withIndex()) {
            if (isCancelled.get()) break

            runOnUiThread {
         //       progressDialog.setMessage("Sending ${index + 1}/${selectedFiles.size}")
            }

            try {
                Socket(serverIp, PORT).use { socket ->
                    DataOutputStream(socket.getOutputStream()).use { dataOutputStream ->
                        contentResolver.openInputStream(fileUri)?.use { inputStream ->
                            val fileName = getFileName(fileUri)
                            dataOutputStream.writeUTF(fileName)

                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                if (isCancelled.get()) break
                                dataOutputStream.write(buffer, 0, bytesRead)
                            }

                            if (!isCancelled.get()) {
                                successCount++
                                runOnUiThread {
                                    statusTextView.text = "Sent: $fileName (${index + 1}/${selectedFiles.size})"
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                failedCount++
                runOnUiThread {
                    Log.e("failedCount","$e ${e.printStackTrace()} Failed to send: ${getFileName(fileUri)}")
                    statusTextView.text = "Failed to send: ${getFileName(fileUri)}"
                }
            }
        }

        isSending.set(false)
        runOnUiThread {
            progressDialog.dismiss()
            val summary = when {
                isCancelled.get() -> "Transfer cancelled. Sent $successCount of ${selectedFiles.size} files."
                failedCount > 0 -> "Completed with $failedCount failures. Sent $successCount of ${selectedFiles.size} files."
                else -> "Successfully sent all ${selectedFiles.size} files."
            }
            statusTextView.text = summary
            Toast.makeText(this, summary, Toast.LENGTH_LONG).show()
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "unknown_file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        }
        return name
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isSending.get()) {
            isCancelled.set(true)
        }
    }
}
