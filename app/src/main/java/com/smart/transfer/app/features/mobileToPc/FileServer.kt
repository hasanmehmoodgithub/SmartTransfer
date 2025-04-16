package com.example.kotlintest.mobileToPc

import android.util.Log
import com.smart.transfer.app.R
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MultipleFileServer(port: Int, private val files: List<File>, private val icon: File) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession?): Response {
        Log.e("NanoHTTPD", "Server started")

        val uri = session?.uri ?: "/"

        return if (uri == "/") {
            // Serve the file list UI
            val fileListHtml = files.joinToString("") { file ->
                val fileType = getFileType(file)
                val fileUrl = "/file/${file.name}"

                """
                <div class="file-item">
                    <h3 title="${file.name}">${file.name}</h3>
                    ${
                    when (fileType) {
                        "image" -> "<img src='$fileUrl' class='file-preview' alt='Preview' />"
                        "video" -> "<video src='$fileUrl' class='file-preview' controls></video>"
                        else -> "<p>No preview available</p>"
                    }
                }
                    <br>
                    <a href="$fileUrl" class="download-btn" download>Download ${file.name}</a>
                </div>
                """
            }

            val htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Smart Share</title>
                    <link rel="icon" type="image/png" href="/icon/app.png">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f5f5f5;
                            margin: 0;
                            padding: 20px;
                        }
                        .header {
                            display: flex;
                            align-items: center;
                            justify-content: flex-start;
                            margin-bottom: 20px;
                        }
                        .header img {
                            margin-right: 8px;
                        }
                        .container {
                            max-width: 1200px;
                            margin: auto;
                            background: white;
                            padding: 30px;
                            border-radius: 10px;
                            box-shadow: 0 0 15px rgba(0,0,0,0.1);
                        }
                        h1 {
                            color: #007bff;
                            margin-bottom: 10px;
                        }
                        p {
                            color: #666;
                            margin-bottom: 30px;
                        }
                        .file-list {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
                            gap: 20px;
                        }
                        .file-item {
                            background: #fafafa;
                            padding: 15px;
                            border-radius: 10px;
                            box-shadow: 0 0 10px rgba(0,0,0,0.05);
                            text-align: center;
                        }
                        .file-item h3 {
                            font-size: 16px;
                            margin: 10px 0;
                            white-space: nowrap;
                            overflow: hidden;
                            text-overflow: ellipsis;
                        }
                        .file-preview {
                            width: 100%;
                            max-height: 180px;
                            object-fit: cover;
                            border-radius: 8px;
                            margin-top: 10px;
                        }
                       .download-btn {
    display: inline-block;
    background: #28a745;
    color: white;
    padding: 8px 12px;
    font-size: 14px;
    text-decoration: none;
    border-radius: 5px;
    margin-top: 10px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    
                        .download-btn:hover {
                            background: #218838;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <img src="/icon/app.png" width="50" height="50" alt="App Icon" />
                            <h1>Smart Share</h1>
                        </div>
                        <p>Download or Preview Your Files</p>
                        <div class="file-list">
                            $fileListHtml
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()

            newFixedLengthResponse(Response.Status.OK, "text/html", htmlContent)
        } else if (uri.startsWith("/file/")) {
            val fileName = uri.removePrefix("/file/")
            val file = files.find { it.name == fileName }

            return if (file != null && file.exists()) {
                try {
                    val fileInputStream = FileInputStream(file)
                    val mimeType = getMimeType(file)

                    Log.e("NanoHTTPD Serving", file.absolutePath)
                    newChunkedResponse(Response.Status.OK, mimeType, fileInputStream)
                } catch (e: IOException) {
                    Log.e("NanoHTTPD Error", "Failed to read file", e)
                    newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file")
                }
            } else {
                Log.e("NanoHTTPD Error", "File not found: $fileName")
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File Not Found")
            }
        } else if (uri == "/icon/app.png") {
            return if (icon.exists()) {
                try {
                    val iconStream = FileInputStream(icon)
                    newChunkedResponse(Response.Status.OK, "image/png", iconStream)
                } catch (e: IOException) {
                    newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading icon")
                }
            } else {
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Icon Not Found")
            }
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    private fun getFileType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png" -> "image"
            "mp4" -> "video"
            else -> "other"
        }
    }
}
