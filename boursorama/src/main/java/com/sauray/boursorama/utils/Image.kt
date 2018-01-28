package com.sauray.boursorama.utils

import android.graphics.Bitmap

/**
 * Created by Antoine Sauray on 27/01/2018.
 */

/**
 * The features are simply a grey scale version (Average)
 */
fun getFeatures(colorCropped : Bitmap): DoubleArray {
    val width = colorCropped.width
    val height = colorCropped.height
    val features = DoubleArray(width*height)
    for(y in 0 until height) {
        for(x in 0 until width) {
            val argb = colorCropped.getPixel(x, y)
            val red = (0xFF and (argb shr 16))
            val green = (0xFF and (argb shr 8))
            val blue = (0xFF and (argb shr 0))
            features[y*width + x] = (red + green + blue)/3.0
        }
    }
    return features
}