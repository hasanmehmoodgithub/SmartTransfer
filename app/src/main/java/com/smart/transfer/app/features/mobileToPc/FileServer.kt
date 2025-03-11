package com.example.kotlintest.mobileToPc

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class FileServer(port: Int, private val file: File) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession?): Response {
        Log.e("NanoHTTPD", "Server started")

        val uri = session?.uri ?: "/"

        return if (uri == "/") {
            // Serve the UI
            val fileType = getFileType(file)
            val fileUrl = "/file"

            val htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>File Server</title>
                    <style>
                        body { 
                            font-family: Arial, sans-serif; 
                            text-align: center; 
                            margin: 50px; 
                            background: #f5f5f5; 
                        }
                        .container {
                            background: white; 
                            padding: 20px; 
                            border-radius: 10px; 
                            box-shadow: 0px 0px 15px rgba(0,0,0,0.2);
                            max-width: 500px;
                            margin: auto;
                        }
                        h1 { color: #007bff; }
                        .file-preview {
                            width: 100%; 
                            max-height: 300px; 
                            border-radius: 10px; 
                            margin-top: 10px;
                        }
                        .download-btn {
                            display: inline-block;
                            background: #28a745;
                            color: white;
                            padding: 12px 20px;
                            text-decoration: none;
                            font-size: 18px;
                            border-radius: 5px;
                            margin-top: 15px;
                            transition: 0.3s;
                        }
                        .download-btn:hover { background: #218838; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>File Server Made By Hasan</h1>
                        <p>Download or Preview Your File</p>
                        
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
                </body>
                </html>
            """.trimIndent()

            newFixedLengthResponse(Response.Status.OK, "text/html", htmlContent)
        } else if (uri == "/file") {
            return if (file.exists() && file.isFile) {
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
                Log.e("NanoHTTPD Error", "File not found: $file")
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File Not Found")
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






class MultipleFileServer(port: Int, private val files: List<File>) : NanoHTTPD(port) {
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
                    <h3>${file.name}</h3>
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
                    <title>File Server</title>
                    <style>
                        body { 
                            font-family: Arial, sans-serif; 
                            text-align: center; 
                            margin: 50px; 
                            background: #f5f5f5; 
                        }
                        .container {
                            background: white; 
                            padding: 20px; 
                            border-radius: 10px; 
                            box-shadow: 0px 0px 15px rgba(0,0,0,0.2);
                            max-width: 600px;
                            margin: auto;
                        }
                        h1 { color: #007bff; }
                        .file-item {
                            background: #fff;
                            padding: 15px;
                            margin: 10px 0;
                            border-radius: 10px;
                            box-shadow: 0px 0px 10px rgba(0,0,0,0.1);
                        }
                        .file-preview {
                            width: 100%; 
                            max-height: 200px; 
                            border-radius: 10px; 
                            margin-top: 10px;
                        }
                        .download-btn {
                            display: inline-block;
                            background: #28a745;
                            color: white;
                            padding: 10px 15px;
                            text-decoration: none;
                            font-size: 16px;
                            border-radius: 5px;
                            margin-top: 10px;
                            transition: 0.3s;
                        }
                        .download-btn:hover { background: #218838; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>File Server Made By Hasan</h1>
                        <p>Download or Preview Your Files</p>
                        $fileListHtml
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
