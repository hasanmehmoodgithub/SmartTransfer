package com.smart.transfer.app.com.smart.transfer.app.features.filepicker.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.filepicker.model.FilePickerCategory

class FilePickerCategoryAdapter(
    private val context: Context,
    private val categories: List<FilePickerCategory>,
    private var selectedIndex: Int = 0,
    private val onCategoryClick: (Int) -> Unit
) : RecyclerView.Adapter<FilePickerCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val cardView: CardView = view.findViewById(R.id.remotelyShare)
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val title: TextView = view.findViewById(R.id.title)

        fun bind(category: FilePickerCategory, isSelected: Boolean) {
            icon.setImageResource(category.icon)
            title.text = category.name

            // Set background color for selection
            val selectedColor = ContextCompat.getColor(context, R.color.app_blue) // Define in colors.xml
            val defaultColor = ContextCompat.getColor(context, R.color.white)

            cardView.setCardBackgroundColor(if (isSelected) selectedColor else defaultColor)

            // Update text color
            val textColor = if (isSelected) Color.WHITE else Color.BLACK
            title.setTextColor(textColor)

            // Apply color filter to icon
            val iconColor = if (isSelected) Color.WHITE else Color.GRAY
            icon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)

            // Handle category click
            cardView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onCategoryClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_file_picker_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedIndex)
    }

    override fun getItemCount(): Int = categories.size

    // Update selected item efficiently
    fun updateSelection(newIndex: Int) {
        if (newIndex == selectedIndex) return // Avoid unnecessary updates
        val oldIndex = selectedIndex
        selectedIndex = newIndex
        notifyItemChanged(oldIndex)
        notifyItemChanged(newIndex)
    }
}
