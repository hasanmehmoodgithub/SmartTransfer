package com.smart.transfer.app.features.dashboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.BaseActivity
import com.smart.transfer.app.com.smart.transfer.app.core.appenums.ChooseFileNextScreenType
import com.smart.transfer.app.com.smart.transfer.app.core.formatFileSizeUtil
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.VideoModel
import com.smart.transfer.app.com.smart.transfer.app.features.mobileToPc.ui.MobileToPcActivity
import com.smart.transfer.app.databinding.ActivityTransferItemBinding
import com.smart.transfer.app.features.androidtoios.ui.AndroidToIosActivity
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedAudiosManager
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedDocumentsManager
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedImagesManager
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedVideosManager
import com.smart.transfer.app.features.localshare.ui.HandlePermissionActivity
import com.smart.transfer.app.features.localshare.ui.LocalShareTestActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.QrSenderReceiverActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.SenderHotSpotActivity
import com.smart.transfer.app.features.localshare.ui.hotspot.SenderHttpActivity
import com.smart.transfer.app.features.localshare.ui.sender.SenderQrActivity
import com.smart.transfer.app.features.localshare.ui.wifdirect.WiFiDirectActivity
import com.smart.transfer.app.features.localshare.ui.wifdirect.WiFiDirectActivity2
import com.smart.transfer.app.features.localshare.ui.wifdirect.WiFiDirectSenderActivity
import com.smart.transfer.app.features.remoltyshare.RemotelyShareActivity
import com.smart.transfer.app.features.remoltyshare.UploadingFilesActivity
import java.io.File

class TransferItemActivity : BaseActivity() {

    private lateinit var binding: ActivityTransferItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val chooseFileNextScreenType = intent.getSerializableExtra("ChooseFileNextScreenType") as? ChooseFileNextScreenType

        val toolbar = findViewById<LinearLayout>(R.id.custom_toolbar)
        setupAppBar(toolbar, "Transfer Items", showBackButton = true)

        updateFileList()

        binding.btnTransfer.setOnClickListener {
            val allSelectedFiles = mutableListOf<Map<String, Any>>()
            var totalSize: Long = 0

            // Collect Images
            SelectedImagesManager.selectedImages.forEach { image ->
                allSelectedFiles.add(
                    mapOf(
                        "name" to image.name,
                        "path" to image.path,
                        "type" to "Photos",
                        "size" to image.size
                    )
                )
                totalSize += image.size
            }

            // Collect Videos
            SelectedVideosManager.selectedVideos.forEach { video ->
                allSelectedFiles.add(
                    mapOf(
                        "name" to video.name,
                        "path" to video.path,
                        "type" to "Videos",
                        "size" to video.size
                    )
                )
                totalSize += video.size
            }

            // Collect Documents
            SelectedDocumentsManager.selectedDocuments.forEach { doc ->
                allSelectedFiles.add(
                    mapOf(
                        "name" to doc.name,
                        "path" to (doc.uri.path ?: "Unknown Path"),
                        "type" to "Documents",
                        "size" to doc.size
                    )
                )
                totalSize += doc.size
            }

            // Collect Audio
            SelectedAudiosManager.selectedAudios.forEach { audio ->
                allSelectedFiles.add(
                    mapOf(
                        "name" to audio.name,
                        "path" to (audio.filePath ?: "Unknown Path"),
                        "type" to "Music",
                        "size" to audio.size
                    )
                )
                totalSize += audio.size
            }

            // Log all selected files
            allSelectedFiles.forEach { file ->
                Log.d(
                    "Selected File",
                    "Name: ${file["name"]}, Path: ${file["path"]}, Type: ${file["type"]}, Size: ${formatFileSizeUtil(file["size"] as Long)}"
                )
            }

            Log.d("Total Count", "Total Files Selected: ${allSelectedFiles.size}")
            Log.d("Total Size", "Total Size of All Files: ${formatFileSizeUtil(totalSize)}")

            AllSelectedFilesManager.allSelectedFiles.clear()
            AllSelectedFilesManager.allSelectedFiles.addAll(allSelectedFiles)

            updateFileList()
            if (allSelectedFiles.isEmpty()) {
                Toast.makeText(this, "Please select files first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            when (chooseFileNextScreenType) {
                ChooseFileNextScreenType.MobileToPc -> {
                    // Handle MobileToPc case
                    startActivity(Intent(this, MobileToPcActivity::class.java))

                }
                ChooseFileNextScreenType.AndroidToIos -> {
                    // Handle AndroidToIos case
                    startActivity(Intent(this, AndroidToIosActivity::class.java))
                }
                ChooseFileNextScreenType.LocalShare -> {
                    // Handle AndroidToIos case
                   // startActivity(Intent(this, QrSenderReceiverActivity::class.java).putExtra("mode", "sender"))
//                  startActivity(Intent(this, SenderHotSpotActivity::class.java))
                   // startActivity(Intent(this, SenderHttpActivity::class.java))
                    startActivity(Intent(this, SenderHttpActivity::class.java))

                  //  startActivity(Intent(this, LocalShareTestActivity::class.java))

               //     startActivity(Intent(this, SenderQrActivity::class.java))
                }
                ChooseFileNextScreenType.Remote -> {
                    // Handle AndroidToIos case
                    startActivity(Intent(this, UploadingFilesActivity::class.java))
                }
                else -> {
                    // Handle other cases (if any)
                }
            }
        }
        binding.addMoreBtn.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    private fun updateFileList() {
        val categorizedFiles = mutableMapOf(
            "Photos" to mutableListOf<String>(),
            "Videos" to mutableListOf<String>(),
            "Music" to mutableListOf<String>(),
            "Documents" to mutableListOf<String>()
        )

        var totalSize = 0L

        // Add all selected files dynamically
        SelectedImagesManager.selectedImages.forEach { image ->
            categorizedFiles["Photos"]?.add(image.path)
            totalSize += image.size
        }

        SelectedVideosManager.selectedVideos.forEach { video ->
            categorizedFiles["Videos"]?.add(video.path)
            totalSize += video.size
        }

        SelectedDocumentsManager.selectedDocuments.forEach { doc ->
            categorizedFiles["Documents"]?.add(doc.uri.path ?: "Unknown Path")
            totalSize += doc.size
        }

        SelectedAudiosManager.selectedAudios.forEach { audio ->
            categorizedFiles["Music"]?.add(audio.uri.path ?: "Unknown Path")
            totalSize += audio.size
        }

        val categories = listOf("Photos", "Videos", "Music", "Documents")
        val icons = listOf(R.drawable.ic_transfer_photo, R.drawable.ic_transfer_video, R.drawable.ic_transfer_music, R.drawable.ic_transfer_doc)

        categories.forEachIndexed { index, category ->
            val count = categorizedFiles[category]?.size ?: 0
            val itemView = binding.gridLayout.getChildAt(index)
            val txtCategoryName = itemView.findViewById<TextView>(R.id.txtCategoryName)
            val txtItemCount = itemView.findViewById<TextView>(R.id.txtItemCount)
            val txtBadgeCount = itemView.findViewById<TextView>(R.id.txtBadgeCount)
            val imgIcon = itemView.findViewById<ImageView>(R.id.imgCategoryIcon)

            txtCategoryName.text = category
            txtItemCount.text = "$count items"
            imgIcon.setImageResource(icons[index])

            txtBadgeCount.text = count.toString()
            txtBadgeCount.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        binding.txtTotalItemsValue.text = "${categorizedFiles.values.sumOf { it.size }}"
        binding.txtTotalSizeValue.text = formatFileSizeUtil(totalSize)
    }


}
object AllSelectedFilesManager {
    val allSelectedFiles = mutableListOf<Map<String, Any>>()

}