package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.VideoModel
import com.smart.transfer.app.databinding.ItemVideoPickerBinding

class VideoPickerAdapter(
    private val videos: List<VideoModel>,
    private val onSelectionChanged: (List<VideoModel>) -> Unit
) : RecyclerView.Adapter<VideoPickerAdapter.VideoViewHolder>() {

    private val selectedVideos = mutableListOf<VideoModel>()

    inner class VideoViewHolder(private val binding: ItemVideoPickerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(video: VideoModel) {
            Glide.with(binding.thumbnail.context)
                .load(video.path)
                .into(binding.thumbnail)

            binding.durationText.text = formatDuration(video.duration)

            // Update checkbox state
            binding.checkBoxSelect.isChecked = selectedVideos.contains(video)

            binding.checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedVideos.add(video)
                } else {
                    selectedVideos.remove(video)
                }
                onSelectionChanged(selectedVideos)
            }

            binding.root.setOnClickListener {
                binding.checkBoxSelect.isChecked = !binding.checkBoxSelect.isChecked
            }
        }

        private fun formatDuration(durationMillis: Long): String {
            val seconds = durationMillis / 1000 % 60
            val minutes = durationMillis / (1000 * 60) % 60
            val hours = durationMillis / (1000 * 60 * 60)

            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount() = videos.size
}
