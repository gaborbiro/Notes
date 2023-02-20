package dev.gaborbiro.notes.features.host.usecase

import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseUseCase

class EditTemplateUseCase(
    private val repository: RecordsRepository
) : BaseUseCase() {

    suspend fun execute(recordId: Long, title: String, description: String) {
        val record = repository.getRecord(recordId)!!
        repository.updateTemplate(
            templateId = record.template.id,
            image = null,
            title = title,
            description = description
        )
    }
}