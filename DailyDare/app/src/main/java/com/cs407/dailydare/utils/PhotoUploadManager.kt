package com.cs407.dailydare.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object PhotoUploadManager {

    private const val IMGBB_API_KEY = "f1369d95bc780e32495ee82f4cd34bc4"

    fun uploadPhoto(context: Context, uri: Uri, callback: (String?) -> Unit) {
        thread {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    callback(null)
                    return@thread
                }

                val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val urlString = "https://api.imgbb.com/1/upload"
                val postData = "key=$IMGBB_API_KEY&image=$base64Image"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                connection.outputStream.use { it.write(postData.toByteArray()) }

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                if (json.getBoolean("success")) {
                    callback(json.getJSONObject("data").getString("url"))
                } else {
                    callback(null)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }
    }
}