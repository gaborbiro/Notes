package dev.gaborbiro.notes.features.host.usecase

import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseUseCase

class ValidateEditRecordUseCase(
    private val repository: RecordsRepository
) : BaseUseCase() {

    suspend fun execute(recordId: Long, title: String, description: String): EditValidationResult {
        if (title.isBlank()) {
            return EditValidationResult.Error("Title cannot be empty")
        }
        val record = repository.getRecord(recordId)!!
        record.template.image?.let {
            if (repository.getTemplates(it, title).isNotEmpty()) {
                return EditValidationResult.Error("Record with this image and title already exists")
            }
        }
        val records = repository.getRecords(record.template.id)
        return if (records.size < 2) {
            EditValidationResult.Valid
        } else {
            EditValidationResult.ConfirmMultipleEdit(records.size)
        }
    }

}

sealed class EditValidationResult {
    class ConfirmMultipleEdit(val count: Int) : EditValidationResult()
    object Valid : EditValidationResult()
    data class Error(val message: String) : EditValidationResult()
}