package dev.gaborbiro.notes.features.host

import android.net.Uri
import androidx.annotation.UiThread
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.features.common.UseCase
import java.time.LocalDateTime

class CreateRecordUseCase(
    private val repository: RecordsRepository,
) : UseCase() {

    @UiThread
    suspend fun execute(
        image: Uri?,
        title: String,
        description: String
    ) {
        val record = ToSaveRecord(
            timestamp = LocalDateTime.now(),
            template = ToSaveTemplate(
                image = image,
                name = title,
                description = description,
            ),
        )
        repository.saveRecord(record)
    }
}