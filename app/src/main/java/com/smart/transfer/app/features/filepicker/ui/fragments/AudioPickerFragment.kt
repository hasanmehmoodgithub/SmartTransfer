package com.smart.transfer.app.features.filepicker.ui.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference.SharedPrefManager
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.AudioModel
import com.smart.transfer.app.databinding.FragmentAudioPickerBinding
import com.smart.transfer.app.features.filepicker.adapter.AudioPickerAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class AudioPickerFragment : Fragment() {
    private val sharedPrefManager by lazy { SharedPrefManager.getInstance(requireContext()) }

    private var _binding: FragmentAudioPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var audioAdapter: AudioPickerAdapter
    private var allSelected = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        audioAdapter = AudioPickerAdapter(emptyList()) { _ -> }
        binding.recyclerView.adapter = audioAdapter
        loadAudioFiles()
        val isSelectAllButtonStatus = sharedPrefManager.isSelectAllCheckBoxStatus()
        if(!isSelectAllButtonStatus)
        {
            binding.checkBoxSelect.visibility=View.GONE
            binding.btnToggleSelect.visibility=View.GONE
        }
        binding.checkBoxSelect.setOnClickListener {
            audioAdapter.toggleSelection()
            allSelected = !allSelected
            binding.btnToggleSelect.text = if (allSelected) "Clear All" else "Select All"
        }

        if (checkPermissions()) {
           // loadAudioFiles()
        } else {
            requestPermissions()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1001)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1001)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAudioFiles()
        } else {
          //  Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAudioFiles() {
        lifecycleScope.launch {
            getAudioFiles(requireContext()).collectLatest { audios ->
                audioAdapter.submitList(audios)
            }
        }
    }

    private fun getAudioFiles(context: Context): Flow<List<AudioModel>> = flow {
        val audioList = mutableListOf<AudioModel>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA, // Add this to get the file path
            MediaStore.Audio.Media.MIME_TYPE
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA) // Real file path
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val filePath = cursor.getString(pathColumn) ?: "" // Get real file path
                val mimeType = cursor.getString(mimeTypeColumn) ?: ""

                if (mimeType in listOf(
                        "audio/mpeg", "audio/wav", "audio/x-wav",
                        "audio/aac", "audio/m4a", "audio/mp4"
                    ) && !mimeType.contains("opus")
                ) {
                    val audioUri = Uri.withAppendedPath(uri, id.toString())
                    audioList.add(AudioModel(id, name, duration, size, audioUri, filePath))
                }
            }
        }
        emit(audioList)
    }

}
object SelectedAudiosManager {
    val selectedAudios = mutableSetOf<AudioModel>()

    fun toggleSelection(audio: AudioModel, isSelected: Boolean) {
        if (isSelected) {
            selectedAudios.add(audio)
        } else {
            selectedAudios.remove(audio)
        }
    }

    fun isSelected(audio: AudioModel): Boolean {
        return selectedAudios.contains(audio)
    }
}