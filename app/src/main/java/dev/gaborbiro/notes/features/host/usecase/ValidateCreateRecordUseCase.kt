package dev.gaborbiro.notes.features.host.usecase

import android.net.Uri
import dev.gaborbiro.notes.features.common.BaseUseCase

class ValidateCreateRecordUseCase : BaseUseCase() {

    suspend fun execute(image: Uri?, title: String, description: String): CreateValidationResult {
        if (title.isBlank()) {
            return CreateValidationResult.Error("Cannot be empty")
        }
        return CreateValidationResult.Valid
    }
}

sealed class CreateValidationResult {
    data class Error(val message: String) : CreateValidationResult()
    object Valid : CreateValidationResult()
}