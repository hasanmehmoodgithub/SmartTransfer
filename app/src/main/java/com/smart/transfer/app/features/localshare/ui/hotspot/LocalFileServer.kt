import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException



class LocalFileServer(port: Int, private val files: List<File>) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession?): Response {
        val uri = session?.uri ?: "/"

        return when {
            uri == "/" -> serveHtml()
            uri == "/files-json" -> serveJson()
            uri.startsWith("/file/") -> serveFile(uri.removePrefix("/file/"))
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    private fun serveHtml(): Response {
        val fileListHtml = files.joinToString("") { file ->
            val fileUrl = "/file/${file.name}"
            """
            <li>
                <strong>${file.name}</strong> —
                <a href="$fileUrl" download>Download</a>
            </li>
            """.trimIndent()
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>File Share</title>
            </head>
            <body>
                <h1>Available Files (${files.size})</h1>
                <ul>
                    $fileListHtml
                </ul>
                <p>Or <a href="/files-json">get JSON list</a></p>
            </body>
            </html>
        """.trimIndent()

        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun serveJson(): Response {
        val jsonArray = JSONArray()
        files.forEach { file ->
            val fileObj = JSONObject()
            fileObj.put("name", file.name)
            fileObj.put("size", file.length())
            fileObj.put("url", "/file/${file.name}")
            jsonArray.put(fileObj)
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray.toString())
    }

    private fun serveFile(fileName: String): Response {
        val file = files.find { it.name == fileName }

        return if (file != null && file.exists()) {
            try {
                val mimeType = getMimeType(file)
                val inputStream = FileInputStream(file)
                newChunkedResponse(Response.Status.OK, mimeType, inputStream)
            } catch (e: IOException) {
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file")
            }
        } else {
            newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "mp3" -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
}
