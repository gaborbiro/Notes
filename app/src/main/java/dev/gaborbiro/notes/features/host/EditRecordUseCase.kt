package dev.gaborbiro.notes.features.host

import android.util.Log
import androidx.annotation.UiThread
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.features.common.UseCase

class EditRecordUseCase(
    private val repository: RecordsRepository,
) : UseCase() {

    /**
     * @return null if there was only one matching record or [force] is set to true. The number of matching records otherwise.
     */
    @UiThread
    suspend fun execute(recordId: Long, title: String, description: String, force: Boolean): Int? {
        val record = repository.getRecord(recordId)
        val records = repository.getRecords(record!!.template.id)
        return if (force || records.size < 2) {
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
            null
        } else {
            records.size
        }

    }
}