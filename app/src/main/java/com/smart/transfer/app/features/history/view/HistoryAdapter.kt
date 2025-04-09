package com.smart.transfer.app.com.smart.transfer.app.features.history.view;

import android.content.ActivityNotFoundException
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.smart.transfer.app.R;
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History;
import java.io.File;
import java.text.DecimalFormat;

class HistoryAdapter(
    private val context: Context,
    private var historyList: List<History>
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = historyList[position]
        val file = File(history.filePath)

        holder.fileName.text = file.name
        holder.fileSize.text = getFileSize(file.length())

        when {
            history.fileType.contains("image", true) ->
                Glide.with(context).load(file).into(holder.fileThumbnail)

            history.fileType.contains("video", true) ->
                Glide.with(context).load(file).thumbnail(0.1f).into(holder.fileThumbnail)

            history.fileType.contains("pdf", true) ||
                    history.fileType.contains("doc", true) ->
                holder.fileThumbnail.setImageResource(R.drawable.ic_transfer_doc)

            history.fileType.contains("mp3", true) ||
                    history.fileType.contains("wav", true) ->
                holder.fileThumbnail.setImageResource(R.drawable.ic_transfer_music)

            else -> holder.fileThumbnail.setImageResource(R.drawable.ic_file_placeholder)
        }


        holder.viewIcon.setOnClickListener {
            Log.e("open view", "setOnClickListener")
            val file = File(history.filePath)
            Log.d("FileOpen", "File path: ${file.absolutePath}")
            Log.d("FileOpen", "File exists: ${file.exists()}, readable: ${file.canRead()}")

            if (file.exists() && file.canRead()) {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )

                    // Get proper MIME type
                    val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(file.extension) ?: "*/*"
                    Log.d("FileOpen", "Detected MIME type: $mimeType")

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // Some files need this
                    }

                    // Grant temporary permissions to all apps that can handle the intent
                    val resolvedActivities = context.packageManager.queryIntentActivities(intent, 0)
                    resolvedActivities.forEach {
                        context.grantUriPermission(
                            it.activityInfo.packageName,
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }

                    context.startActivity(Intent.createChooser(intent, "Open file with"))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FileOpen", "Error opening file", e)
                }
            } else {
                Toast.makeText(context, "File not found or not accessible!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileThumbnail: ImageView = view.findViewById(R.id.fileThumbnail)
        val fileName: TextView = view.findViewById(R.id.fileName)
        val fileSize: TextView = view.findViewById(R.id.fileSize) // Added file size text view
        val viewIcon: ImageView = view.findViewById(R.id.viewIcon)
    }

    private fun getFileSize(size: Long): String {
        val df = DecimalFormat("#.##")
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> "${df.format(gb)} GB"
            mb >= 1 -> "${df.format(mb)} MB"
            kb >= 1 -> "${df.format(kb)} KB"
            else -> "$size B"
        }
    }
}
