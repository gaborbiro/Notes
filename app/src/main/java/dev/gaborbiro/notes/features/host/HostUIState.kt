package dev.gaborbiro.notes.features.host

import android.net.Uri

data class HostUIState(
    val showCamera: Boolean = false,
    val showImagePicker: Boolean = false,
    val refreshWidget: Boolean = false,
    val closeScreen: Boolean = false,
    val templateIdForImageRedo: Long? = null,
    val dialog: DialogState? = null,
)

sealed class DialogState {
    data class EditTargetConfirmationDialog(
        val recordId: Long,
        val count: Int,
        val newTitle: String,
        val newDescription: String,
    ) : DialogState()

    sealed class InputDialogState(
        open val validationError: String? = null,
    ) : DialogState() {
        data class Create(
            override val validationError: String? = null,
        ) : InputDialogState(validationError) {
            override fun withValidationError(validationError: String?) =
                copy(validationError = validationError)
        }

        data class CreateWithImage(
            val image: Uri?,
            override val validationError: String? = null,
        ) : InputDialogState(validationError) {
            override fun withValidationError(validationError: String?) =
                copy(validationError = validationError)
        }

        data class Edit(
            val recordId: Long,
            val image: Uri?,
            val title: String,
            val description: String,
            override val validationError: String? = null,
        ) : InputDialogState(validationError) {
            override fun withValidationError(validationError: String?) =
                copy(validationError = validationError)
        }

        abstract fun withValidationError(validationError: String?): InputDialogState
    }
}
