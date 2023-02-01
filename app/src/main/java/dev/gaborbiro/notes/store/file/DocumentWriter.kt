package dev.gaborbiro.notes.store.file

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DocumentWriter(
    private val appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun write(inStream: InputStream, name: String): Uri =
        withContext(ioDispatcher) {
            val uri = getOrCreateFile(name)
            val output = appContext.contentResolver.openOutputStream(uri)
                ?: throw IOException("ContentProvider recently crashed. Try again later.")
            doWrite(output, inStream)
            uri
        }

    private fun doWrite(outStream: OutputStream, inStream: InputStream) {
        inStream.use { input ->
            outStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getOrCreateFile(fileName: String): Uri {
        return getOrCreateFile(subPath = null, fileName = fileName)
    }

    fun getOrCreateFile(subPath: String?, fileName: String): Uri {
        val authority = "${appContext.packageName}.provider"
        val dir = appContext.cacheDir
        val separator = File.separator
        val filePath = "public$separator${subPath?.let { "$it$separator" } ?: ""}$fileName"
        return androidx.core.content.FileProvider.getUriForFile(
            appContext,
            authority,
            File(dir, filePath).also {
                if (it.isDirectory) it.delete()
                // because we ourselves are composing the path above, it can be assumed that parentFile will never be null
                if (it.parentFile?.exists() != true) it.parentFile?.mkdirs()
            }
        )
    }
}