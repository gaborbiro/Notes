package dev.gaborbiro.notes.features.host.usecase

import android.graphics.Bitmap
import android.net.Uri
import dev.gaborbiro.notes.features.common.BaseUseCase
import dev.gaborbiro.notes.store.bitmap.BitmapStore

class CacheImageUseCase(private val bitmapStore: BitmapStore) : BaseUseCase() {

    suspend fun execute(uri: Uri?): Uri? {
        return uri?.let { copyImage(uri) }
    }

    private suspend fun copyImage(uri: Uri): Uri? {
        return bitmapStore.loadBitmap(uri)?.let {
            persistImage(it)
        }
    }

    private suspend fun persistImage(
        bitmap: Bitmap,
    ): Uri {
//        val correctedBitmap = correctBitmap(
//            currentScreenRotation = currentScreenRotation,
//            bitmap = bitmap,
//            correctRotation = false,
//        )
        return bitmapStore.writeBitmap(bitmap)
    }
}