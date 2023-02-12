package dev.gaborbiro.notes.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.view.Surface
import java.io.FileNotFoundException

class BitmapLoader(
    private val context: Context,
) {

    fun loadBitmap(uri: Uri): Bitmap? {
        try {
            return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            )
        } catch (e: FileNotFoundException) {
            return null
        }
    }
}

fun correctBitmap(
    currentScreenRotation: Int,
    bitmap: Bitmap,
    correctRotation: Boolean,
    correctWidth: Boolean
): Bitmap {
    val rotateAngle = if (correctRotation) {
        when (currentScreenRotation) {
            Surface.ROTATION_0 -> 90f
            Surface.ROTATION_90 -> 0f
            Surface.ROTATION_180 -> 0f
            Surface.ROTATION_270 -> 180f
            else -> 0f
        }
    } else {
        0f
    }
    return modifyImage(
        bitmap,
        rotateAngle,
        if (correctWidth) 360 else 0
    )
}

fun modifyImage(source: Bitmap, rotateAngle: Float, maxWidthPx: Int): Bitmap {
    val matrix = Matrix()
    if (rotateAngle != 0f) {
        matrix.postRotate(rotateAngle)
    }
    if (maxWidthPx != 0) {
        val scale = maxWidthPx / source.width.toFloat()
        if (scale < 1f) {
            matrix.postScale(scale, scale)
        }
    }
    return Bitmap.createBitmap(
        source,
        0,
        0,
        source.width,
        source.height,
        matrix,
        true
    )
}