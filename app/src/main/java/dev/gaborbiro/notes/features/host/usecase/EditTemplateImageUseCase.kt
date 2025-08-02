package dev.gaborbiro.notes.features.host.usecase

import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseUseCase

class EditTemplateImageUseCase(
    private val repository: RecordsRepository,
) : BaseUseCase() {

    suspend fun execute(recordId: Long, uri: String?) {
        val templateId = repository.getRecord(recordId)!!.template.id
        repository.updateTemplate(templateId, uri)
    }
}
