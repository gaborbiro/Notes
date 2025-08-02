package dev.gaborbiro.notes.features.host.usecase

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import dev.gaborbiro.notes.features.common.BaseUseCase
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import java.time.LocalDateTime

class SaveImageUseCase(
    private val appContext: Context,
    private val bitmapStore: BitmapStore,
) : BaseUseCase() {

    suspend fun execute(uri: Uri): String {
        val bitmap = ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(
                appContext.contentResolver,
                uri
            )
        )
        val filename = "${LocalDateTime.now()}.png"
        bitmapStore.writeBitmap(bitmap, filename)
        return filename
    }
}
