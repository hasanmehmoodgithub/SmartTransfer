package com.smart.transfer.app.features.localshare.ui.hotspot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.database.AppDatabase
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import com.smart.transfer.app.databinding.ActivitySenderHttpBinding
import com.smart.transfer.app.features.dashboard.ui.AllSelectedFilesManager
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException



class SenderHttpActivity : BaseActivity() {
    private lateinit var binding: ActivitySenderHttpBinding
    private var fileServer: LocalFileServer? = null
    private var isServerRunning = false
    private val port = 8080






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySenderHttpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Local Share", showBackButton = true)
        database = AppDatabase.getDatabase(this)
        // Start animation
//        binding.lottieAnimationView.playAnimation()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupStopServerUi()



        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
        val fileList = paths.map { File(it) }

        startFileServerList(fileList)
    }
    fun onClickCopyIpText(view: View) {
        val clipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Ip Text", binding.ipText.text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Link Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

//    fun onClickStartServer(view: View) {
//        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }
//        Log.e("paths","$paths")
//
//// Convert paths to File objects
//        val fileList = paths.map { File(it) }
//
//        startFileServerList(fileList)
//    }
//    fun onClickStopServer(view: View) {
//        try {
//
//            fileServer?.stop()
//            setupStopServerUi()
//
//        } catch (e:Exception){
//            setupStartServerUi()
//        }
//
//
//    }

    private fun setupStartServerUi(){
        isServerRunning=true
//        binding.starButton.visibility=View.GONE
//        binding.stopButton.visibility=View.VISIBLE
        binding.qrImage.visibility=View.VISIBLE
        binding.ipText.visibility=View.VISIBLE

    }
    private fun setupStopServerUi(){
        isServerRunning=false
//        binding.starButton.visibility=View.VISIBLE
//        binding.stopButton.visibility=View.GONE
        binding.qrImage.visibility=View.GONE
        binding.ipText.visibility=View.GONE

    }

    private fun startFileServerList(fileList: List<File>) {
        try {
            val iconDrawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher
            )

            val bitmap = if (iconDrawable is BitmapDrawable) {
                iconDrawable.bitmap
            } else {
                // Handle VectorDrawable
                val width = iconDrawable?.intrinsicWidth
                val height = iconDrawable?.intrinsicHeight
                val bitmap = width?.let { height?.let { it1 ->
                    Bitmap.createBitmap(it,
                        it1, Bitmap.Config.ARGB_8888)
                } }
                val canvas = bitmap?.let { Canvas(it) }
                if (width != null) {
                    height?.let { iconDrawable.setBounds(0, 0, width, it) }
                }
                canvas?.let { iconDrawable.draw(it) }
                bitmap
            }

            val iconFile = File(this.cacheDir, "app.png")
            try {
                val outputStream = FileOutputStream(iconFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            fileServer?.stop()
            fileServer = LocalFileServer(port,fileList)
            try {
                fileServer?.start()
                val ipAddress = getLocalIpAddress()
                Log.e("startSharingFiles","Sharing ${fileList.size} files at http://$ipAddress:8080")
                Toast.makeText(this,"Sharing Total Files ${fileList.size}",Toast.LENGTH_SHORT).show();
                //   showToast("Sharing ${fileList.size} files at http://$ipAddress:8080")

                // Optional: Show a notification with the server address
                insertHistoryData()

            } catch (e: IOException) {
                Log.e("startSharingFiles","Could not start server: ${e.message}")
                //  showToast("Could not start server: ${e.message}")
                Toast.makeText(this,"Could not start server: ${e.message}",Toast.LENGTH_SHORT).show();
            }

            val ipAddress = getLocalIpAddress()


            val qrCodeBitmap =   generateQRCode("http://$ipAddress:$port/")
            binding.qrImage.setImageBitmap(qrCodeBitmap)
            binding.ipText.text = ipAddress.toString()
            setupStartServerUi()
        }
        catch (e:Exception){
            setupStopServerUi()
            isServerRunning=false
        }

    }



    private fun getLocalIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ip = wifiInfo.ipAddress
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }
    private fun generateQRCode(content: String): Bitmap {
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 500, 500)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }

        return bitmap
    }

    override fun onDestroy() {
        super.onDestroy()

        fileServer?.stop()
        setupStopServerUi()
    }

    private lateinit var database: AppDatabase

    private fun insertHistoryData() {
        val paths = AllSelectedFilesManager.allSelectedFiles.mapNotNull { it["path"] as? String }


        lifecycleScope.launch {

            paths.forEach { path ->
                val historyItem = History(
                    filePath = path,
                    fileType = getFileTypeFromPath(path), // Optional: get type based on file extension
                    tag = "local",
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
}
class LocalFileServer(private val port: Int, private val filesToShare: List<File>) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        // Serve file listing at root
        if (uri == "/") {
            return listSharedFiles()
        }

        // Serve specific file
        val fileName = uri.removePrefix("/")
        val requestedFile = filesToShare.find { it.name == fileName }

        if (requestedFile != null && requestedFile.exists() && requestedFile.canRead()) {
            return serveFile(requestedFile)
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found")
    }

    private fun listSharedFiles(): Response {
        val html = buildString {
            append("<html><body><h1>Shared Files</h1><ul>")
            filesToShare.forEach { file ->
                append("<li><a href='/${file.name}'>${file.name}</a> (${file.length()} bytes)</li>")
            }
            append("</ul></body></html>")
        }
        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, html)
    }

    private fun serveFile(file: File): Response {
        val mimeType = getMimeType(file)
        val fileStream = FileInputStream(file)
        return newFixedLengthResponse(Response.Status.OK, mimeType, fileStream, file.length())
    }
    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }



}