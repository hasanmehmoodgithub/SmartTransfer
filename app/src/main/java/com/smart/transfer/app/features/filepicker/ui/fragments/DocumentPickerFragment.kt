package com.smart.transfer.app.features.filepicker.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smart.transfer.app.com.smart.transfer.app.core.sharedpreference.SharedPrefManager
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.DocumentModel
import com.smart.transfer.app.features.filepicker.adapter.DocumentPickerAdapter


import com.smart.transfer.app.databinding.FragmentDocumentPickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DocumentPickerFragment : Fragment() {
    private val sharedPrefManager by lazy { SharedPrefManager.getInstance(requireContext()) }

    private var _binding: FragmentDocumentPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var documentAdapter: DocumentPickerAdapter
    private val documents = mutableListOf<DocumentModel>()
    private var allSelected = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDocumentPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        documentAdapter = DocumentPickerAdapter(documents) { selectedList ->
            SelectedDocumentsManager.selectedDocuments.clear()
            SelectedDocumentsManager.selectedDocuments.addAll(selectedList)
            Log.e("SelectedDocumentsManager", "Selected Documents: $selectedList")
        }
        binding.recyclerView.adapter = documentAdapter

        loadDocuments()

        binding.btnDone.setOnClickListener {
            // Handle selected documents (e.g., pass to another screen, upload, etc.)
            Log.d("SelectedDocs", "Final Selected Documents: ${SelectedDocumentsManager.selectedDocuments}")
        }
        val isSelectAllButtonStatus = sharedPrefManager.isSelectAllCheckBoxStatus()
        if(!isSelectAllButtonStatus)
        {
            binding.checkBoxSelect.visibility=View.GONE
            binding.btnToggleSelect.visibility=View.GONE
        }
        binding.checkBoxSelect.setOnClickListener {
            documentAdapter.toggleSelection()
            allSelected = !allSelected
            binding.btnToggleSelect.text = if (allSelected) "Clear All" else "Select All"
        }
    }

    private fun loadDocuments() {
        lifecycleScope.launch {
            getDocuments(requireContext()).collectLatest { documentsList ->
                documents.clear()
                documents.addAll(documentsList)
                documentAdapter.submitList(documents)
            }
        }
    }
    private fun getDocuments(context: Context): Flow<List<DocumentModel>> = flow {
        val documents = mutableListOf<DocumentModel>()
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?)"
        val selectionArgs = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val size = cursor.getLong(sizeColumn)
                val documentUri = Uri.withAppendedPath(uri, id.toString())

                // Get actual file path
                val filePath = getRealPathFromURI(context, documentUri) ?: getFileFromUri(context, documentUri).absolutePath

                Log.d("DocumentPath", "File Path: $filePath") // Debugging purpose

                documents.add(DocumentModel(id, name, size, Uri.parse(filePath)))
            }
        }

        emit(documents)
    }

//    private fun getDocuments(context: Context): Flow<List<DocumentModel>> = flow {
//        val documents = mutableListOf<DocumentModel>()
//        val uri: Uri = MediaStore.Files.getContentUri("external")
//        val projection = arrayOf(
//            MediaStore.Files.FileColumns._ID,
//            MediaStore.Files.FileColumns.DISPLAY_NAME,
//            MediaStore.Files.FileColumns.SIZE
//        )
//        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?)"
//        val selectionArgs = arrayOf(
//            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
//        )
//        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
//
//        context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
//            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
//            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
//
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val name = cursor.getString(nameColumn)
//                val size = cursor.getLong(sizeColumn)
//                val documentUri = Uri.withAppendedPath(uri, id.toString())
//                documents.add(DocumentModel(id, name, size, documentUri))
//            }
//        }
//
//        emit(documents)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
object SelectedDocumentsManager {
    val selectedDocuments = mutableListOf<DocumentModel>()
}
fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            return cursor.getString(columnIndex)
        }
    }
    return null
}
fun getFileFromUri(context: Context, uri: Uri): File {
    val file = File(context.cacheDir, "tempFile_${System.currentTimeMillis()}.tmp")
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return file
}