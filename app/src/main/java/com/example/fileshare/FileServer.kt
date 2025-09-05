package com.example.fileshare

import fi.iki.elonen.NanoHTTPD
import android.content.Context
import java.io.File
import java.io.FileInputStream

class FileServer(private val context: Context, port: Int = 8080) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        try {
            val uri = session.uri
            if (session.method == Method.GET) {
                if (uri == "/" || uri == "/index.html") {
                    val html = loadAssetText("index.html")
                    return newFixedLengthResponse(Response.Status.OK, "text/html", html)
                }
                if (uri == "/list") {
                    val dir = context.getExternalFilesDir(null)
                    val names = dir?.listFiles()?.map { it.name } ?: emptyList()
                    val json = org.json.JSONArray(names).toString()
                    return newFixedLengthResponse(Response.Status.OK, "application/json", json)
                }
                if (uri.startsWith("/files/")) {
                    val name = uri.removePrefix("/files/")
                    val file = File(context.getExternalFilesDir(null), name)
                    if (file.exists()) {
                        val fis = FileInputStream(file)
                        return newChunkedResponse(Response.Status.OK, "application/octet-stream", fis)
                    } else {
                        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found")
                    }
                }
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not found")
            }

            if (session.method == Method.POST && uri == "/upload") {
                val filesMap = HashMap<String, String>()
                session.parseBody(filesMap)
                val tmpPath = filesMap["file"]
                val params = session.parameters
                val originalNames = params["file"]
                val name = if (originalNames != null && originalNames.isNotEmpty()) originalNames[0] else "upload.bin"

                if (tmpPath != null) {
                    val tmpFile = File(tmpPath)
                    val dest = File(context.getExternalFilesDir(null), sanitizeFilename(name))
                    tmpFile.copyTo(dest, overwrite = true)
                    return newFixedLengthResponse(Response.Status.OK, "text/plain", "OK")
                } else {
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Upload failed")
                }
            }

            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Method Not Allowed")
        } catch (e: Exception) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error: ${e.message}")
        }
    }

    private fun loadAssetText(name: String): String {
        context.assets.open(name).bufferedReader().use { return it.readText() }
    }

    private fun sanitizeFilename(s: String): String {
        return s.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}
