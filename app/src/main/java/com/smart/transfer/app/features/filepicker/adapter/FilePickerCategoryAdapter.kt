package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.FilePickerCategory


class FilePickerCategoryAdapter(
    private val categories: List<FilePickerCategory>,
    private var selectedIndex: Int = 0,
    private val onCategoryClick: (Int) -> Unit
) : RecyclerView.Adapter<FilePickerCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.remotelyShare)
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)

        fun bind(category: FilePickerCategory, isSelected: Boolean) {
            icon.setImageResource(category.icon)
            title.text = category.name

            cardView.setCardBackgroundColor(
                if (isSelected) Color.parseColor("#1C80F7") else Color.WHITE
            )

            title.setTextColor(if (isSelected) Color.WHITE else Color.BLACK)

            cardView.setOnClickListener {
                onCategoryClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_picker_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedIndex)
    }

    override fun getItemCount() = categories.size

    fun updateSelection(newIndex: Int) {
        val oldIndex = selectedIndex
        selectedIndex = newIndex
        notifyItemChanged(oldIndex)
        notifyItemChanged(newIndex)
    }
}