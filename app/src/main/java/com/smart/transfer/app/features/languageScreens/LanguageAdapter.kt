package com.smart.transfer.app.com.smart.transfer.app.features.languageScreens

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smart.transfer.app.R
import com.smart.transfer.app.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val languages: List<Language>,
    private val onLanguageSelected: (Int) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var selectedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        holder.bind(language, position)
    }

    override fun getItemCount() = languages.size

    inner class LanguageViewHolder(private val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(language: Language, position: Int) {
            binding.tvLanguage.text = language.name

            // Update radio button color based on selection
            val colorRes = if (position == selectedPosition) R.color.app_blue else R.color.grey
            binding.radioImg.setColorFilter(ContextCompat.getColor(binding.root.context, colorRes))

            // Handle item selection
            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position

                // Notify item changes only if they exist
                previousPosition?.let { notifyItemChanged(it) }
                notifyItemChanged(position)

                onLanguageSelected(position)
            }
        }
    }
}
