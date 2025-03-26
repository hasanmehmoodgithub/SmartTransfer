package com.smart.transfer.app.features.filepicker.ui.fragments

import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter.ImagePickerAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.ImageModel
import com.smart.transfer.app.databinding.FragmentImagePickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ImagePickerFragment : Fragment() {

    private var _binding: FragmentImagePickerBinding? = null
    private val binding get() = _binding!!

    private val images = mutableListOf<ImageModel>()
    private lateinit var adapter: ImagePickerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImagePickerBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        checkPermissionsAndLoadImages()
        loadImages()

        return view
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = ImagePickerAdapter(images) { selectedList ->
            SelectedImagesManager.selectedImages.clear()
            SelectedImagesManager.selectedImages.addAll(selectedList)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun checkPermissionsAndLoadImages() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            loadImages()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    private fun loadImages() {
        lifecycleScope.launch {
            getImages().collect { imageList ->
                images.clear()
                images.addAll(imageList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getImages(): Flow<List<ImageModel>> = flow {
        val imageList = mutableListOf<ImageModel>()
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE
        )
        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val path = it.getString(pathIndex)
                val size = it.getLong(sizeIndex)
                imageList.add(ImageModel(name, path, size))
            }
        }
        emit(imageList)
    }.flowOn(Dispatchers.IO)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}

object SelectedImagesManager {
    val selectedImages = mutableListOf<ImageModel>()
}
