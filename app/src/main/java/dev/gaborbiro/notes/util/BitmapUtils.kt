package dev.gaborbiro.notes.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.DisplayMetrics
import android.view.Surface
import androidx.compose.ui.unit.Dp
import dev.gaborbiro.notes.App


fun correctBitmap(
    currentScreenRotation: Int,
    bitmap: Bitmap,
    correctRotation: Boolean,
    scaleDown: Boolean
): Bitmap {
    val rotateAngle = if (correctRotation) {
        when (currentScreenRotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 90f
            else -> 0f
        }
    } else {
        0f
    }
    return modifyImage(
        bitmap,
        rotateAngle,
        if (scaleDown) 640 else null
    )
}

fun modifyImage(source: Bitmap, rotateAngle: Float, maxWidthPx: Int?): Bitmap {
    val matrix = Matrix()
    if (rotateAngle != 0f) {
        matrix.postRotate(rotateAngle)
    }
    if (maxWidthPx != null) {
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

fun Dp.px(): Float {
    return App.appContext.dpToPixel(this.value)
}

fun Context.dpToPixel(dp: Float): Float {
    val metrics: DisplayMetrics = this.resources.displayMetrics
    return dp * (metrics.densityDpi / 160f)
}