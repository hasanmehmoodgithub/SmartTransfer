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
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter.VideoPickerAdapter
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.VideoModel
import com.smart.transfer.app.databinding.FragmentVideoPickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class VideoPickerFragment : Fragment() {

    private var _binding: FragmentVideoPickerBinding? = null
    private val binding get() = _binding!!

    private val videos = mutableListOf<VideoModel>()
    private lateinit var adapter: VideoPickerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVideoPickerBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        checkPermissionsAndLoadVideos()
        loadVideos()
        return view
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        adapter = VideoPickerAdapter(videos) { selectedList ->
            SelectedVideosManager.selectedVideos.clear()
            SelectedVideosManager.selectedVideos.addAll(selectedList)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun checkPermissionsAndLoadVideos() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            loadVideos()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        }
    }

    private fun loadVideos() {
        lifecycleScope.launch {
            getVideos().collect { videoList ->
                videos.clear()
                videos.addAll(videoList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun getVideos(): Flow<List<VideoModel>> = flow {
        val videoList = mutableListOf<VideoModel>()
        val projection = arrayOf(
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val cursor = requireContext().contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection, null, null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )
        cursor?.use {
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val pathIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val durationIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)


            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val path = it.getString(pathIndex)
                val duration = it.getLong(durationIndex)
                val size = it.getLong(sizeIndex)
                videoList.add(VideoModel(name, path, duration, size))
            }
        }
        emit(videoList)
    }.flowOn(Dispatchers.IO)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}

object SelectedVideosManager {
    val selectedVideos = mutableListOf<VideoModel>()
}
