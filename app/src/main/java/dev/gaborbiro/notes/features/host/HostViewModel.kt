package dev.gaborbiro.notes.features.host

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.UiThread
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.features.host.usecase.CacheImageUseCase
import dev.gaborbiro.notes.features.host.usecase.CreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.CreateValidationResult
import dev.gaborbiro.notes.features.host.usecase.EditImageValidationResult
import dev.gaborbiro.notes.features.host.usecase.EditRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateUseCase
import dev.gaborbiro.notes.features.host.usecase.EditValidationResult
import dev.gaborbiro.notes.features.host.usecase.GetRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.PersistNewPhotoUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateCreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditRecordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HostViewModel(
    private val repository: RecordsRepository,
    private val createRecordUseCase: CreateRecordUseCase,
    private val editRecordUseCase: EditRecordUseCase,
    private val editTemplateUseCase: EditTemplateUseCase,
    private val validateEditRecordUseCase: ValidateEditRecordUseCase,
    private val validateCreateRecordUseCase: ValidateCreateRecordUseCase,
    private val persistNewPhotoUseCase: PersistNewPhotoUseCase,
    private val cacheImageUseCase: CacheImageUseCase,
    private val validateEditImageUseCase: ValidateEditImageUseCase,
    private val editRecordImageUseCase: EditRecordImageUseCase,
    private val editTemplateImageUseCase: EditTemplateImageUseCase,
    private val getRecordImageUseCase: GetRecordImageUseCase,
) : BaseViewModel() {

    companion object {
        enum class EditTarget {
            RECORD, TEMPLATE
        }
    }

    private val _uiState: MutableStateFlow<HostUIState> = MutableStateFlow(HostUIState())
    val uiState: StateFlow<HostUIState> = _uiState.asStateFlow()

    @UiThread
    fun onStartWithCamera() {
        _uiState.update {
            it.copy(
                showCamera = true,
            )
        }
    }

    @UiThread
    fun onStartWithImagePicker() {
        _uiState.update {
            it.copy(
                imagePicker = ImagePickerState.Create,
            )
        }
    }

    @UiThread
    fun onStartWithJustText() {
        _uiState.update {
            it.copy(
                dialog = DialogState.InputDialogState.Create(),
            )
        }
    }

    @UiThread
    fun onStartWithRedoImage(recordId: Long) {
        _uiState.update {
            it.copy(
                imagePicker = ImagePickerState.EditImage(recordId),
            )
        }
    }

    fun onStartWithShowImage(recordId: Long) {
        runSafely {
            val image = getRecordImageUseCase.execute(recordId)!!
            _uiState.update {
                it.copy(
                    dialog = DialogState.ShowImageDialog(image)
                )
            }
        }
    }

//    @UiThread
//    fun deleteRecord(recordId: Long) {
//        runSafely {
//            val record = repository.deleteRecord(recordId)
//            val (templateDeleted, imageDeleted) = repository.deleteTemplateIfUnused(record.template.id)
//            Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
//            refreshWidgetAndClose()
//        }
//    }

    @UiThread
    fun onStartWithEdit(recordId: Long) {
        runSafely {
            val record = repository.getRecord(recordId)!!
            _uiState.update {
                it.copy(
                    dialog = DialogState.InputDialogState.Edit(
                        recordId = recordId,
                        image = record.template.image,
                        title = record.template.name,
                        description = record.template.description,
                        validationError = null,
                    ),
                )
            }
        }
    }

    @UiThread
    fun onPhotoTaken(currentScreenRotation: Int, bitmap: Bitmap?) {
        runSafely {
            val uri: Uri? = persistNewPhotoUseCase.execute(currentScreenRotation, bitmap)

            _uiState.update {
                it.copy(
                    showCamera = false,
                    dialog = DialogState.InputDialogState.CreateWithImage(image = uri),
                )
            }
        }
    }

    @UiThread
    fun onImagePicked(currentScreenRotation: Int, uri: Uri?) {
        runSafely {
            val persistedUri = cacheImageUseCase.execute(currentScreenRotation, uri)
            val imagePicker = _uiState.value.imagePicker!!
            when (imagePicker) {
                ImagePickerState.Create -> {
                    _uiState.update {
                        it.copy(
                            imagePicker = null,
                            dialog = DialogState.InputDialogState.CreateWithImage(image = persistedUri),
                        )
                    }
                }

                is ImagePickerState.EditImage -> {
                    val result = validateEditImageUseCase.execute(imagePicker.recordId)
                    when (result) {
                        is EditImageValidationResult.ConfirmMultipleEdit -> {
                            _uiState.update {
                                it.copy(
                                    imagePicker = null,
                                    dialog = DialogState.EditImageTargetConfirmationDialog(
                                        recordId = imagePicker.recordId,
                                        count = result.count,
                                        image = persistedUri,
                                    ),
                                )
                            }
                        }

                        EditImageValidationResult.Valid -> {
                            refreshWidgetAndClose()
                            editRecordImageUseCase.execute(imagePicker.recordId, persistedUri)
                        }
                    }
                }
            }
        }
    }

    @UiThread
    fun onDialogDismissed() {
        _uiState.update {
            it.copy(
                closeScreen = true,
            )
        }
    }

    @UiThread
    fun onRecordDetailsUserTyping(title: String, description: String) {
        _uiState.update {
            val dialogState: DialogState.InputDialogState = it.requireDialog()
            it.copy(
                dialog = dialogState.withValidationError(validationError = null),
            )
        }
    }

    @UiThread
    fun onRecordDetailsSubmitRequested(title: String, description: String) {
        val dialogState: DialogState.InputDialogState = _uiState.value.requireDialog()
        runSafely {
            when (dialogState) {
                is DialogState.InputDialogState.Create, is DialogState.InputDialogState.CreateWithImage -> {
                    handleCreateRecordDetailsSubmitted(dialogState, title, description)
                }

                is DialogState.InputDialogState.Edit -> {
                    handleEditRecordDialogSubmitted(dialogState, title, description)
                }
            }
        }
    }

    private suspend fun handleCreateRecordDetailsSubmitted(
        dialogState: DialogState.InputDialogState,
        title: String,
        description: String
    ) {
        val image = (dialogState as? DialogState.InputDialogState.CreateWithImage)?.image
        val result = validateCreateRecordUseCase.execute(image, title, description)

        when (result) {
            is CreateValidationResult.Error -> {
                applyValidationError(result.message)
            }

            is CreateValidationResult.Valid -> {
                refreshWidgetAndClose()
                createRecordUseCase.execute(image, title, description)
            }
        }
    }

    private suspend fun handleEditRecordDialogSubmitted(
        dialogState: DialogState.InputDialogState.Edit,
        title: String,
        description: String
    ) {
        val result = validateEditRecordUseCase.execute(
            dialogState.recordId,
            title,
            description
        )
        when (result) {
            is EditValidationResult.ConfirmMultipleEdit -> {
                _uiState.update {
                    it.copy(
                        dialog = DialogState.EditTargetConfirmationDialog(
                            recordId = dialogState.recordId,
                            count = result.count,
                            newTitle = title,
                            newDescription = description,
                        ),
                    )
                }
            }

            is EditValidationResult.Valid -> {
                editRecordUseCase.execute(
                    recordId = dialogState.recordId,
                    title = title,
                    description = description,
                )
                refreshWidgetAndClose()
            }

            is EditValidationResult.Error -> {
                applyValidationError(result.message)
            }
        }
    }

    private fun applyValidationError(message: String?) {
        _uiState.update {
            val dialogState: DialogState.InputDialogState = it.requireDialog()
            it.copy(
                dialog = dialogState.withValidationError(validationError = message),
            )
        }
    }

    @UiThread
    fun onEditImageTargetConfirmed(target: EditTarget) {
        refreshWidgetAndClose()
        val dialogState: DialogState.EditImageTargetConfirmationDialog =
            _uiState.value.requireDialog()
        runSafely {
            when (target) {
                EditTarget.RECORD -> {
                    editRecordImageUseCase.execute(dialogState.recordId, dialogState.image)
                }

                EditTarget.TEMPLATE -> {
                    editTemplateImageUseCase.execute(dialogState.recordId, dialogState.image)
                }
            }
        }
    }

    @UiThread
    fun onEditTargetConfirmed(target: EditTarget) {
        refreshWidgetAndClose()
        val dialogState: DialogState.EditTargetConfirmationDialog = _uiState.value.requireDialog()
        dialogState.let {
            val recordId = dialogState.recordId
            val title = dialogState.newTitle
            val description = dialogState.newDescription
            runSafely {
                when (target) {
                    EditTarget.RECORD -> {
                        editRecordUseCase.execute(
                            recordId = recordId,
                            title = title,
                            description = description,
                        )
                    }

                    EditTarget.TEMPLATE -> {
                        editTemplateUseCase.execute(
                            recordId = recordId,
                            title = title,
                            description = description,
                        )
                    }
                }
            }
        }
    }

    private fun refreshWidgetAndClose() {
        _uiState.update {
            it.copy(
                refreshWidget = true,
                closeScreen = true,
                imagePicker = null,
            )
        }
    }

    private inline fun <reified T : DialogState> HostUIState.requireDialog(): T {
        if (this.dialog?.let { it is T } != true) throw IllegalArgumentException(
            "Expected ${T::class.java.simpleName}. Found ${this.dialog?.javaClass?.simpleName}"
        )
        return this.dialog as T
    }
}