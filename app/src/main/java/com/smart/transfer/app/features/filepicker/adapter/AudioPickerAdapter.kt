package com.smart.transfer.app.features.filepicker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smart.transfer.app.com.smart.transfer.app.core.formatFileSizeUtil
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.AudioModel
import com.smart.transfer.app.databinding.ItemAudioPickerBinding
import com.smart.transfer.app.features.filepicker.ui.fragments.SelectedAudiosManager

class AudioPickerAdapter(
    private val audios: List<AudioModel>,
    private val onSelectionChanged: (List<AudioModel>) -> Unit
) : RecyclerView.Adapter<AudioPickerAdapter.AudioViewHolder>() {

    private val audioList = mutableListOf<AudioModel>()

    fun submitList(newList: List<AudioModel>) {
        audioList.clear()
        audioList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioList[position])
    }

    override fun getItemCount() = audioList.size

    inner class AudioViewHolder(private val binding: ItemAudioPickerBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(audio: AudioModel) {
            binding.txtAudioName.text = audio.name
            binding.txtAudioDuration.text = formatDuration(audio.duration)
            binding.txtAudioSize.text = formatFileSizeUtil(audio.size)


            // Prevent unwanted triggers when recycling views
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = SelectedAudiosManager.isSelected(audio)

            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                SelectedAudiosManager.toggleSelection(audio, isChecked)
                onSelectionChanged(SelectedAudiosManager.selectedAudios.toList())
            }
        }

        private fun formatDuration(duration: Long): String {
            val minutes = duration / 60000
            val seconds = (duration % 60000) / 1000
            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}
