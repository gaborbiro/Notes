package dev.gaborbiro.notes.features.host.usecase

import android.graphics.Bitmap
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseUseCase
import dev.gaborbiro.notes.store.bitmap.BitmapStore

class GetRecordImageUseCase(
    private val repository: RecordsRepository,
    private val bitmapStore: BitmapStore
) : BaseUseCase() {

    suspend fun execute(recordId: Long, thumbnail: Boolean): Bitmap? {
        return repository.getRecord(recordId)!!.template.image
            ?.let {
                bitmapStore.loadBitmap(it, thumbnail)
            }
    }
}
