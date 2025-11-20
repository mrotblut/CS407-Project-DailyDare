package com.cs407.dailydare.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread
import android.os.Handler
import android.os.Looper

object PhotoUploadManager {

    private const val IMGBB_API_KEY = "f1369d95bc780e32495ee82f4cd34bc4"

    fun uploadPhoto(context: Context, uri: Uri, callback: (String?) -> Unit) {
        thread {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                    return@thread
                }

                val base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                val urlString = "https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY"
                val postData = "image=${URLEncoder.encode(base64Image, "UTF-8")}"


                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.outputStream.use { it.write(postData.toByteArray()) }

                Log.w("uploadPhoto",connection.responseCode.toString())
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                if (json.getBoolean("success")) {
                    Handler(Looper.getMainLooper()).post {
                        callback(json.getJSONObject("data").getString("url"))
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        callback(null)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

}