package dev.gaborbiro.notes.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.view.Display
import android.view.Surface
import java.io.FileNotFoundException

class BitmapLoader(
    private val context: Context,
) {

    fun uriToBitmap(uri: Uri): Bitmap? {
        try {
            return ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            )
        } catch (e: FileNotFoundException) {
            return null
        }
    }
}

fun correctBitmapRotation(display: Display, bitmap: Bitmap): Bitmap {
    return when (display.rotation) {
        Surface.ROTATION_0 -> rotateImage(bitmap, 90f)
        Surface.ROTATION_90 -> bitmap
        Surface.ROTATION_180 -> bitmap
        Surface.ROTATION_270 -> rotateImage(bitmap, 180f)
        else -> bitmap
    }
}

fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
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