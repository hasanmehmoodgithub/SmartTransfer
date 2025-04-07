package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.ImageModel
import com.smart.transfer.app.databinding.ItemImagePickerBinding

class ImagePickerAdapter(
    private val images: List<ImageModel>,
    private val onSelectionChanged: (List<ImageModel>) -> Unit
) : RecyclerView.Adapter<ImagePickerAdapter.ImageViewHolder>() {

    private val selectedImages = mutableListOf<ImageModel>()

    inner class ImageViewHolder(private val binding: ItemImagePickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(image: ImageModel) {
            Glide.with(binding.imageView.context)
                .load(image.path)
                .into(binding.imageView)

            // Avoid triggering listener when programmatically changing checkbox
            binding.checkBoxSelect.setOnCheckedChangeListener(null)

            // Set the checkbox state
            binding.checkBoxSelect.isChecked = selectedImages.contains(image)

            // Listener for manual selection
            binding.checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!selectedImages.contains(image)) {
                        selectedImages.add(image)
                    }
                } else {
                    selectedImages.remove(image)
                }
                onSelectionChanged(selectedImages)
            }

            // Toggle selection on item click
            binding.root.setOnClickListener {
                binding.checkBoxSelect.isChecked = !binding.checkBoxSelect.isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImagePickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    private fun selectAll() {
        selectedImages.clear()
        selectedImages.addAll(images)
        notifyDataSetChanged()
        onSelectionChanged(selectedImages)
    }

    private fun clearAll() {
        selectedImages.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedImages)
    }

    fun toggleSelection() {
        if (selectedImages.size < images.size) {
            selectAll()
        } else {
            clearAll()
        }
    }


    fun getSelectedImages(): List<ImageModel> = selectedImages
}
