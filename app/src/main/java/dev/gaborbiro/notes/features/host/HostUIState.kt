package dev.gaborbiro.notes.features.host

import android.graphics.Bitmap

data class HostUIState(
    val showCamera: Boolean = false,
    val imagePicker: ImagePickerState? = null,
    val refreshWidget: Boolean = false,
    val closeScreen: Boolean = false,
    val dialog: DialogState? = null,
)

sealed class DialogState {
    data class EditTargetConfirmationDialog(
        val recordId: Long,
        val count: Int,
        val newTitle: String,
        val newDescription: String,
    ) : DialogState()

    data class EditImageTargetConfirmationDialog(
        val recordId: Long,
        val count: Int,
        val image: String?,
    ) : DialogState()

    data class ShowImageDialog(val bitmap: Bitmap) : DialogState()

    sealed class InputDialog(
        open val validationError: String? = null,
    ) : DialogState() {

        data class Create(
            override val validationError: String? = null,
        ) : InputDialog(validationError) {
            override fun withValidationError(validationError: String?) =
                copy(validationError = validationError)
        }

        data class CreateWithImage(
            val image: String?,
            val titleSuggestion: String? = null,
            val titleSuggestionProgressIndicator: Boolean = false,
            override val validationError: String? = null,
        ) : InputDialog(validationError) {
            override fun withValidationError(validationError: String?) =
                copy(validationError = validationError)
        }

        data class Edit(
            val recordId: Long,
            val image: String?,
            val title: String,
            val description: String,
            override val validationError: String? = null,
        ) : InputDialog(validationError) {
            override fun withValidationError(validationError: String?) =
                copy(validationError = validationError)
        }

        abstract fun withValidationError(validationError: String?): InputDialog
    }
}

sealed class ImagePickerState {
    class EditImage(val recordId: Long) : ImagePickerState()
    object Create : ImagePickerState()
}
