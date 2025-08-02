package dev.gaborbiro.notes.store.bitmap

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.util.LruCache
import dev.gaborbiro.notes.ImageFileFormat
import dev.gaborbiro.notes.store.file.FileStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class BitmapStore(
    private val fileStore: FileStore,
) {
    private var cache: LruCache<String, Bitmap>
    private var thumbnailCache: LruCache<String, Bitmap>

    companion object {
        const val THUMBNAIL_SUFFIX = "-thumb"
    }

    private val maxThumbnailSizePx = 128 // maximum height and length of thumbnail variant of images

    init {
        val maxMemoryBytes = (Runtime.getRuntime().maxMemory()).toInt()
        val cacheSize = maxMemoryBytes / 6
        cache = object : LruCache<String, Bitmap>(/* maxSize = */ cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount
            }
        }
        thumbnailCache = object : LruCache<String, Bitmap>(/* maxSize = */ cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount
            }
        }
    }

    fun read(filename: String, thumbnail: Boolean): Bitmap? {
        val path = fileStore.resolveFilePath(filename)
        val file = File(path)
        val thumbnailFilename = insertSuffixToFilename(filename, THUMBNAIL_SUFFIX)
        var bitmap = if (thumbnail) thumbnailCache[thumbnailFilename] else cache[filename]

        if (bitmap == null) {
            bitmap = if (thumbnail) {
                val thumbnailPath = fileStore.resolveFilePath(thumbnailFilename)
                val thumbnailFile = File(thumbnailPath)
                if (thumbnailFile.exists()) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(thumbnailFile))
                } else {
                    val decodedListener = ImageDecoder.OnHeaderDecodedListener { decoder, info, _ ->
                        var height = info.size.height
                        var width = info.size.width
                        var update = false
                        if (height > width && width > maxThumbnailSizePx) {
                            height = ((height.toFloat() / width) * maxThumbnailSizePx).toInt()
                            width = maxThumbnailSizePx
                            update = true
                        }
                        if (width > height && height > maxThumbnailSizePx) {
                            width = ((width.toFloat() / height) * maxThumbnailSizePx).toInt()
                            height = maxThumbnailSizePx
                            update = true
                        }
                        if (width == height && width > maxThumbnailSizePx) {
                            width = maxThumbnailSizePx
                            height = maxThumbnailSizePx
                            update = true
                        }
                        if (update) {
                            decoder.setTargetSize(width, height)
                        }
                    }
                    if (file.exists()) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(file), decodedListener)
                            .also {
                                write(thumbnailFilename, it)
                            }
                    } else {
                        null
                    }
                }
            } else {
                if (file.exists()) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(file))
                } else {
                    null
                }
            }

            bitmap
                ?.let {
                    if (thumbnail) {
                        thumbnailCache.put(thumbnailFilename, bitmap)
                    } else {
                        cache.put(filename, bitmap)
                    }
                }
                ?: run {
                    if (thumbnail) {
                        thumbnailCache.remove(thumbnailFilename)
                    } else {
                        cache.remove(filename)
                    }
                }
        }
        return bitmap
    }

    fun write(filename: String, bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            fileStore.write(filename) { outputStream ->
                bitmap.compress(
                    /* format = */ ImageFileFormat,
                    /* quality = */ 0,  // quality is ignored with PNG
                    /* stream = */ outputStream
                )
            }
        }
        filename
    }

    /**
     * Inserts [suffix] before the extension in [filename], or appends it if there's no extension.
     *
     * Rules:
     *  - "photo.png" + "-thumb" -> "photo-thumb.png"
     *  - "archive.tar.gz" + "-small" -> "archive.tar-small.gz"
     *  - "readme" + "-v2" -> "readme-v2"
     *  - ".gitignore" + "-old" -> ".gitignore-old"  (leading dot not treated as extension)
     *  - "2025-07-28T20:50:19.901788.png" + "-thumb" -> "2025-07-28T20:50:19.901788-thumb.png"
     */
    fun insertSuffixToFilename(filename: String, suffix: String): String {
        if (filename.isEmpty()) return filename
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot > 0) { // dot not at start
            val name = filename.substring(0, lastDot)
            val ext = filename.substring(lastDot) // includes '.'
            "$name$suffix$ext"
        } else {
            "$filename$suffix"
        }
    }
}
