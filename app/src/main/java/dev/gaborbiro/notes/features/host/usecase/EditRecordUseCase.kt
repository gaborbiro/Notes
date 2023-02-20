package dev.gaborbiro.notes.features.host.usecase

import android.util.Log
import androidx.annotation.UiThread
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.features.common.BaseUseCase

class EditRecordUseCase(
    private val repository: RecordsRepository,
) : BaseUseCase() {

    @UiThread
    suspend fun execute(recordId: Long, title: String, description: String) {
        val record = repository.getRecord(recordId)!!
        val newRecord = ToSaveRecord(
            timestamp = record.timestamp,
            template = ToSaveTemplate(
                image = record.template.image,
                name = title,
                description = description,
            ),
        )
        repository.saveRecord(newRecord)
        repository.deleteRecord(recordId)
        val (templateDeleted, imageDeleted) =
            repository.deleteTemplateIfUnused(record.template.id)
        Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
    }
}