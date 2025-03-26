package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smart.transfer.app.R

class ImagePagingAdapter(
    private val onImageSelected: (String) -> Unit
) : PagingDataAdapter<String, ImagePagingAdapter.ImageViewHolder>(DIFF_CALLBACK) {

    private var selectedImage: String? = null

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
            override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        }
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)

        fun bind(imagePath: String) {
            Glide.with(itemView.context).load(imagePath).into(imageView)

            itemView.setOnClickListener {
                selectedImage = imagePath
                notifyDataSetChanged()
                onImageSelected(imagePath)
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_picker, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }
}
