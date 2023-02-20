package dev.gaborbiro.notes.features.host.usecase

import android.graphics.Bitmap
import android.net.Uri
import dev.gaborbiro.notes.features.common.BaseUseCase
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.util.correctBitmap

class PersistNewPhotoUseCase(private val bitmapStore: BitmapStore) : BaseUseCase() {

    suspend fun execute(currentScreenRotation: Int, image: Bitmap?): Uri? {
        return image?.let { persistImage(currentScreenRotation, it) }
    }

    private suspend fun persistImage(
        currentScreenRotation: Int,
        bitmap: Bitmap,
    ): Uri {
        val correctedBitmap = correctBitmap(
            currentScreenRotation = currentScreenRotation,
            bitmap = bitmap,
            correctRotation = true,
            correctWidth = true
        )
        return bitmapStore.writeBitmap(correctedBitmap)
    }
}