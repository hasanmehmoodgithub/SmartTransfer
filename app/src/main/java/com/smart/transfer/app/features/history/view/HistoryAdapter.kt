package com.smart.transfer.app.com.smart.transfer.app.features.history.view


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smart.transfer.app.R
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History
import java.io.File

class HistoryAdapter(
    private val context: Context,
    private var historyList: List<History> // Change 'val' to 'var' for mutability
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]

        holder.fileName.text = File(history.filePath).name

        // ✅ Set Thumbnail Based on File Type
        when {
            history.fileType.contains("image", true) ->
                Glide.with(context).load(File(history.filePath)).into(holder.fileThumbnail)

            history.fileType.contains("video", true) ->
                Glide.with(context).load(File(history.filePath)).thumbnail(0.1f).into(holder.fileThumbnail)

            history.fileType.contains("pdf", true) ||
                    history.fileType.contains("doc", true) ->
                holder.fileThumbnail.setImageResource(R.drawable.ic_transfer_doc)

            history.fileType.contains("mp3", true) ||
                    history.fileType.contains("wav", true) ->
                holder.fileThumbnail.setImageResource(R.drawable.ic_transfer_music)

            else -> holder.fileThumbnail.setImageResource(R.drawable.ic_folder)
        }

        // ✅ Open File on Click
        holder.viewIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(File(history.filePath)), history.fileType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileThumbnail: ImageView = view.findViewById(R.id.fileThumbnail)
        val fileName: TextView = view.findViewById(R.id.fileName)
        val viewIcon: ImageView = view.findViewById(R.id.viewIcon)
    }


}
