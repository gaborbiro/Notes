package dev.gaborbiro.notes.features.host.usecase

import android.graphics.Bitmap
import android.net.Uri
import dev.gaborbiro.notes.features.common.BaseUseCase
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.util.correctBitmap

class CacheImageUseCase(private val bitmapStore: BitmapStore) : BaseUseCase() {

    suspend fun execute(currentScreenRotation: Int, uri: Uri?): Uri? {
        return uri?.let { copyImage(currentScreenRotation, uri) }
    }

    private suspend fun copyImage(currentScreenRotation: Int, uri: Uri): Uri? {
        return bitmapStore.loadBitmap(uri)?.let {
            persistImage(currentScreenRotation, it)
        }
    }

    private suspend fun persistImage(
        currentScreenRotation: Int,
        bitmap: Bitmap,
    ): Uri {
        val correctedBitmap = correctBitmap(
            currentScreenRotation = currentScreenRotation,
            bitmap = bitmap,
            correctRotation = false,
            correctWidth = true
        )
        return bitmapStore.writeBitmap(correctedBitmap)
    }
}