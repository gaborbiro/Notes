package dev.gaborbiro.notes.store.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import dev.gaborbiro.notes.store.file.DocumentWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class BitmapStore(
    private val context: Context,
) {

    private val documentWriter = DocumentWriter(context)

    fun loadBitmap(uri: Uri): Bitmap? {
        return try {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            )
        } catch (e: FileNotFoundException) {
            null
        }
    }

    suspend fun writeBitmap(bitmap: Bitmap): Uri {
        return ByteArrayOutputStream().let { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val inStream = ByteArrayInputStream(stream.toByteArray())
            documentWriter.write(inStream, "${System.currentTimeMillis()}.png")
        }
    }
}
