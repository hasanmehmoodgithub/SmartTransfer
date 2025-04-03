package com.smart.transfer.app.com.smart.transfer.app.features.remoltyshare.data.remote.api

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class ProgressRequestBody(
    private val file: File,
    private val progressListener: ProgressListener
) : RequestBody() {

    interface ProgressListener {
        fun onProgressUpdate(progress: Int)
    }

    override fun contentType(): MediaType? {

        return "application/zip".toMediaTypeOrNull() // Change to the MIME type of your file
    }


    // Your function or class where you're using MediaType

    override fun contentLength(): Long {
        return file.length()
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val buffer = Buffer()
        val source = file.source()
        val totalBytes = file.length()
        var uploadedBytes: Long = 0

        source.use {
            var read: Long
            while (source.read(buffer, 8192).also { read = it } != -1L) {
                sink.write(buffer, read)
                uploadedBytes += read
                val progress = ((uploadedBytes.toDouble() / totalBytes) * 100).toInt()
                progressListener.onProgressUpdate(progress)
            }
        }
    }
}
