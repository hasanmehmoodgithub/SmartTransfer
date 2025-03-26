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

            // Update checkbox state
            binding.checkBoxSelect.isChecked = selectedImages.contains(image)

            binding.checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedImages.add(image)
                } else {
                    selectedImages.remove(image)
                }
                onSelectionChanged(selectedImages)
            }

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
}
