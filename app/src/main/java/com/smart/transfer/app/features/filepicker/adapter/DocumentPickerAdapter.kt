package com.smart.transfer.app.features.filepicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smart.transfer.app.com.smart.transfer.app.core.formatFileSizeUtil
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.DocumentModel
import com.smart.transfer.app.databinding.ItemDocumentPickerBinding
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedDocumentsManager

class DocumentPickerAdapter(
    private var documents: List<DocumentModel>,
    private val onSelectionChanged: (List<DocumentModel>) -> Unit
) : RecyclerView.Adapter<DocumentPickerAdapter.DocumentViewHolder>() {

    private val selectedDocuments = mutableSetOf<DocumentModel>()

    fun submitList(newList: List<DocumentModel>) {
        documents = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding =
            ItemDocumentPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(documents[position])
    }

    override fun getItemCount() = documents.size

    inner class DocumentViewHolder(private val binding: ItemDocumentPickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(document: DocumentModel) {
            binding.txtDocumentName.text = document.name
            binding.txtDocumentSize.text = formatFileSizeUtil(document.size)

            // Prevent unwanted callbacks while updating UI
            binding.checkBox.setOnCheckedChangeListener(null)

            // Maintain selection state when scrolling
            binding.checkBox.isChecked = selectedDocuments.contains(document)

            // Handle selection changes
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDocuments.add(document)
                } else {
                    selectedDocuments.remove(document)
                }

                // Update global selection manager
                SelectedDocumentsManager.selectedDocuments.clear()
                SelectedDocumentsManager.selectedDocuments.addAll(selectedDocuments)

                // Notify selection change
                onSelectionChanged(selectedDocuments.toList())
            }
        }
    }

    // Select all documents
    private fun selectAll() {
        selectedDocuments.clear()
        selectedDocuments.addAll(documents)
        notifyDataSetChanged()
        onSelectionChanged(selectedDocuments.toList())
    }

    // Clear all selected documents
    private fun clearAll() {
        selectedDocuments.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedDocuments.toList())
    }

    // Toggle selection: Select all if not all selected, otherwise clear all
    fun toggleSelection() {
        if (selectedDocuments.size < documents.size) {
            selectAll()
        } else {
            clearAll()
        }
    }
}
