package dev.gaborbiro.notes.features.host

import android.net.Uri

data class HostUIState(
    val showCamera: Boolean = false,
    val showImagePicker: Boolean = false,
    val refreshWidgetAndCloseScreen: Boolean = false,
    val imageUri: Uri? = null,
    val templateIdForRedo: Long? = null,
    val inputDialogState: InputDialogState = InputDialogState(),
)

data class InputDialogState(
    val visible: Boolean = false,
    val validationError: String? = null,
)

sealed class Intent {
    object OpenCamera : Intent()
    object CameraOpened : Intent()
}