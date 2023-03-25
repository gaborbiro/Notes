package dev.gaborbiro.notes.store.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.LruCache
import dev.gaborbiro.notes.store.file.DocumentWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class BitmapStore(
    private val context: Context,
) {
    private var memoryCache: LruCache<String, Bitmap>

    private val documentWriter = DocumentWriter(context)

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 6
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    fun loadBitmap(uri: Uri, maxSizePx: Int? = null): Bitmap? {
        val imageKey = uri.toString()
        var bitmap = memoryCache[imageKey]

        if (bitmap == null) {
            bitmap = if (maxSizePx != null) {
                val listener = ImageDecoder.OnHeaderDecodedListener { decoder, info, _ ->
                    var height = info.size.height
                    var width = info.size.width
                    var update = false
                    if (height > width && width > maxSizePx) {
                        height = ((height.toFloat() / width) * maxSizePx).toInt()
                        width = maxSizePx
                        update = true
                    }
                    if (width > height && height > maxSizePx) {
                        width = ((width.toFloat() / height) * maxSizePx).toInt()
                        height = maxSizePx
                        update = true
                    }
                    if (width == height && width > maxSizePx) {
                        width = maxSizePx
                        height = maxSizePx
                        update = true
                    }
                    if (update) {
                        decoder.setTargetSize(width, height)
                    }
                }
                try {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri),
                        listener
                    )
                } catch (e: FileNotFoundException) {
                    null
                }
            } else {
                try {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            context.contentResolver,
                            uri
                        )
                    )
                } catch (e: FileNotFoundException) {
                    null
                }
            }


            bitmap?.let {
                memoryCache.put(imageKey, bitmap)
            }
            bitmap ?: run {
                memoryCache.remove(imageKey)
            }
        }
        return bitmap
    }

    suspend fun writeBitmap(bitmap: Bitmap): Uri {
        return ByteArrayOutputStream().let { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val inStream = ByteArrayInputStream(stream.toByteArray())
            documentWriter.write(inStream, "${System.currentTimeMillis()}.png")
        }
    }
}

//private val imageSizePx = context.resources.displayMetrics.density.let { scale ->
//    (60 * scale + .5f)
//}
//private val dummyBitmap: Bitmap = Bitmap
//    .createBitmap(imageSizePx.toInt(), imageSizePx.toInt(), Bitmap.Config.ARGB_8888)
//    .also {
//        it.eraseColor(android.graphics.Color.CYAN)
//    }