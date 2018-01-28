package com.sauray.boursorama.utils

import android.os.Environment
import android.util.Log
import com.sauray.boursorama.Boursorama
import java.io.File
import java.io.FileOutputStream

/**
 * Created by Antoine Sauray on 28/01/2018.
 * Used to keep the requests for post work
 */

fun saveString(name: String, message: String) {
    val filename = name .toLowerCase()+ ".txt"
    Log.d("saveString", "filename: "+filename)
    val sd = Environment.getExternalStorageDirectory()
    val dest = File(sd, filename)
    Log.d("saveString", "dest: " + dest.absolutePath)
    try {
        val out = FileOutputStream(dest)
        out.write(message.toByteArray())
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}