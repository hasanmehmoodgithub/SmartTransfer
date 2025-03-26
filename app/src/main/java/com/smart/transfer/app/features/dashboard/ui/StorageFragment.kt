package com.smart.transfer.app.features.dashboard.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.smart.transfer.app.databinding.FragmentStorageBinding

class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!! // Safe property for binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.btnRefresh.setOnClickListener {
//            checkPermissionsAndLoadData()
//        }
        loadStorageData()
        checkPermissionsAndLoadData()
    }

    private fun checkPermissionsAndLoadData() {
        if (hasStoragePermission()) {
            loadStorageData()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
    }


    private fun loadStorageData() {
        lifecycleScope.launch {
            getStorageInfo()
                .flowOn(Dispatchers.IO)
                .collect { (totalStorage, usedStorage, availableStorage, categories) ->

                    val usedPercentage = if (totalStorage > 0) {
                        (usedStorage.toFloat() / totalStorage * 100).toInt()
                    } else {
                        0 // Prevent division by zero
                    }

                    binding.apply {
                        tvTotalStorage.text = formatSize(totalStorage)
                        tvUsedStorage.text = formatSize(usedStorage)
                        tvAvailableStorage.text = formatSize(availableStorage)
                        usedStorageProgress.text = "$usedPercentage %"

                        tvPhotos.text = "${categories["Photos"]?.first ?: 0} items - ${formatSize(categories["Photos"]?.second ?: 0)}"
                        tvVideos.text = "${categories["Videos"]?.first ?: 0} items - ${formatSize(categories["Videos"]?.second ?: 0)}"
                        tvMusic.text = "${categories["Music"]?.first ?: 0} items - ${formatSize(categories["Music"]?.second ?: 0)}"
                        tvDocuments.text = "${categories["Documents"]?.first ?: 0} items - ${formatSize(categories["Documents"]?.second ?: 0)}"

                        // Extract category sizes safely
                        val totalSize = totalStorage.toFloat()
                        val imageSize = categories["Photos"]?.second?.toFloat() ?: 0f
                        val videoSize = categories["Videos"]?.second?.toFloat() ?: 0f
                        val docSize = categories["Documents"]?.second?.toFloat() ?: 0f
                        val audioSize = categories["Music"]?.second?.toFloat() ?: 0f
                        val available = availableStorage.toFloat()

                        // Calculate system size (remaining storage after accounting for all other segments)
                        val systemSize = totalSize - (imageSize + videoSize + docSize + audioSize + available)

                        // Format system size properly
                        tvSystem.text = formatSize(systemSize.toLong())

                        binding.progressPieView.setStorageSizes(
                            totalSize,
                            imageSize,
                            videoSize,
                            docSize,
                            audioSize,
                            available,
                            systemSize
                        )
                    }
                }
        }
    }


    private fun getStorageInfo(): Flow<StorageInfo> {
        return flow {
            val storageDir = Environment.getExternalStorageDirectory()
            val stat = android.os.StatFs(storageDir.path)
            val totalStorage = stat.blockCountLong * stat.blockSizeLong
            val availableStorage = stat.availableBlocksLong * stat.blockSizeLong
            val usedStorage = totalStorage - availableStorage

            val categories = mapOf(
                "Photos" to getMediaSizeAndCount(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                "Videos" to getMediaSizeAndCount(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
                "Music" to getMediaSizeAndCount(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI),
                "Documents" to getMediaSizeAndCount(MediaStore.Files.getContentUri("external"))
            )

            emit(StorageInfo(totalStorage, usedStorage, availableStorage, categories))
        }
    }

    private fun getMediaSizeAndCount(contentUri: android.net.Uri): Pair<Int, Long> {
        var totalSize: Long = 0
        var count = 0
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)

        requireContext().contentResolver.query(contentUri, projection, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                totalSize += cursor.getLong(sizeIndex)
                count++
            }
        }
        return Pair(count, totalSize)
    }

    private fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> "%.2f GB".format(gb)
            mb >= 1 -> "%.2f MB".format(mb)
            kb >= 1 -> "%.2f KB".format(kb)
            else -> "$size B"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadStorageData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 101
    }
}

data class StorageInfo(
    val totalStorage: Long,
    val usedStorage: Long,
    val availableStorage: Long,
    val categories: Map<String, Pair<Int, Long>>
)