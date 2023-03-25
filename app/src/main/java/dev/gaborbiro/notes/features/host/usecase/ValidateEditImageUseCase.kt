package dev.gaborbiro.notes.features.host.usecase

import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseUseCase

class ValidateEditImageUseCase(
    private val repository: RecordsRepository
) : BaseUseCase() {

    suspend fun execute(recordId: Long): EditImageValidationResult {
        val templateId = repository.getRecord(recordId)!!.template.id
        val records = repository.getRecords(templateId)
        return if (records.size < 2) {
            EditImageValidationResult.Valid
        } else {
            EditImageValidationResult.ConfirmMultipleEdit(records.size)
        }
    }
}

sealed class EditImageValidationResult {
    class ConfirmMultipleEdit(val count: Int) : EditImageValidationResult()
    object Valid : EditImageValidationResult()
}