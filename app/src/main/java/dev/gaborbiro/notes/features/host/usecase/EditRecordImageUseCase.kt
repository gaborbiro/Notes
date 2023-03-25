package dev.gaborbiro.notes.features.host.usecase

import android.net.Uri
import android.util.Log
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.features.common.BaseUseCase

class EditRecordImageUseCase(
    private val repository: RecordsRepository
) : BaseUseCase() {

    suspend fun execute(recordId: Long, uri: Uri?) {
        val record = repository.getRecord(recordId)!!
        val newRecord = ToSaveRecord(
            timestamp = record.timestamp,
            template = ToSaveTemplate(
                image = uri,
                name = record.template.name,
                description = record.template.description,
            ),
        )
        repository.saveRecord(newRecord)
        repository.deleteRecord(recordId)
        val (templateDeleted, imageDeleted) =
            repository.deleteTemplateIfUnused(record.template.id)
        Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
    }
}