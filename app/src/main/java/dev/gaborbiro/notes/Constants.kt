package dev.gaborbiro.notes

import android.graphics.Bitmap
import java.time.LocalDateTime

const val ImageFilenameExt = "png"
val ImageFileFormat = Bitmap.CompressFormat.PNG

val imageFilename: () -> String = {
    "${LocalDateTime.now()}.$ImageFilenameExt"
}
