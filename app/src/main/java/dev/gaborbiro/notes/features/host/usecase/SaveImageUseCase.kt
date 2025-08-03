package dev.gaborbiro.notes.features.host.usecase

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import dev.gaborbiro.notes.features.common.BaseUseCase
import dev.gaborbiro.notes.imageFilename
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SaveImageUseCase(
    private val appContext: Context,
    private val bitmapStore: BitmapStore,
) : BaseUseCase() {

    suspend fun execute(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val bitmap = ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    appContext.contentResolver,
                    uri
                )
            )
            val filename = imageFilename()
            bitmapStore.write(filename, bitmap)
            filename
        }
    }
}
