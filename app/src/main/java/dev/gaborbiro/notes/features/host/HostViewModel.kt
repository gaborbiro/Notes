package dev.gaborbiro.notes.features.host

import android.net.Uri
import androidx.annotation.UiThread
import dev.gaborbiro.notes.data.chatgpt.ChatGPTRepository
import dev.gaborbiro.notes.data.chatgpt.model.DomainError
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.features.host.usecase.CreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.CreateValidationResult
import dev.gaborbiro.notes.features.host.usecase.EditImageValidationResult
import dev.gaborbiro.notes.features.host.usecase.EditRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateUseCase
import dev.gaborbiro.notes.features.host.usecase.EditValidationResult
import dev.gaborbiro.notes.features.host.usecase.FoodPicSummaryUseCase
import dev.gaborbiro.notes.features.host.usecase.GetRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.SaveImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateCreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditRecordUseCase
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HostViewModel(
    private val bitmapStore: BitmapStore,
    private val recordsRepository: RecordsRepository,
    private val chatGPTRepository: ChatGPTRepository,
    private val createRecordUseCase: CreateRecordUseCase,
    private val editRecordUseCase: EditRecordUseCase,
    private val editTemplateUseCase: EditTemplateUseCase,
    private val validateEditRecordUseCase: ValidateEditRecordUseCase,
    private val validateCreateRecordUseCase: ValidateCreateRecordUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val validateEditImageUseCase: ValidateEditImageUseCase,
    private val editRecordImageUseCase: EditRecordImageUseCase,
    private val editTemplateImageUseCase: EditTemplateImageUseCase,
    private val getRecordImageUseCase: GetRecordImageUseCase,
    private val foodPicSummaryUseCase: FoodPicSummaryUseCase,
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
                dialog = DialogState.InputDialog.Create(),
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
            val image = getRecordImageUseCase.execute(recordId, thumbnail = false)!!
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
            val record = recordsRepository.getRecord(recordId)!!
            _uiState.update {
                it.copy(
                    dialog = DialogState.InputDialog.Edit(
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
    fun onPhotoTaken(filename: String) {
        runSafely {
            _uiState.update {
                it.copy(
                    showCamera = false,
                    dialog = DialogState.InputDialog.CreateWithImage(
                        image = filename,
                        titleSuggestionProgressIndicator = true,
                    ),
                )
            }
            try {
                val summary = foodPicSummaryUseCase.execute(filename)
                summary?.let {
                    _uiState.update { currentState ->
                        if (currentState.dialog is DialogState.InputDialog.CreateWithImage) {
                            currentState.copy(dialog = currentState.dialog.copy(titleSuggestion = it))
                        } else {
                            currentState
                        }
                    }
                }
            } catch (e: DomainError) {
                e.printStackTrace()
            } finally {
                _uiState.update { currentState ->
                    if (currentState.dialog is DialogState.InputDialog.CreateWithImage) {
                        currentState.copy(dialog = currentState.dialog.copy(titleSuggestionProgressIndicator = false))
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    @UiThread
    fun onImagePicked(uri: Uri?) {
        runSafely {
            val imagePicker = _uiState.value.imagePicker!!
            _uiState.update {
                it.copy(
                    imagePicker = null,
                    dialog = DialogState.InputDialog.CreateWithImage(
                        image = null,
                        titleSuggestionProgressIndicator = true,
                    ),
                )
            }
            val persistedFilename = uri?.let { saveImageUseCase.execute(it) }
            when (imagePicker) {
                ImagePickerState.Create -> {
                    _uiState.update {
                        it.copy(
                            imagePicker = null,
                            dialog = DialogState.InputDialog.CreateWithImage(
                                image = persistedFilename,
                                titleSuggestionProgressIndicator = true,
                            ),
                        )
                    }
                    persistedFilename?.let {
                        try {
                            val summary = foodPicSummaryUseCase.execute(persistedFilename)
                            summary?.let {
                                _uiState.update { currentState ->
                                    if (currentState.dialog is DialogState.InputDialog.CreateWithImage) {
                                        currentState.copy(dialog = currentState.dialog.copy(titleSuggestion = it))
                                    } else {
                                        currentState
                                    }
                                }
                            }
                        } catch (e: DomainError) {
                            e.printStackTrace()
                        } finally {
                            _uiState.update { currentState ->
                                if (currentState.dialog is DialogState.InputDialog.CreateWithImage) {
                                    currentState.copy(dialog = currentState.dialog.copy(titleSuggestionProgressIndicator = false))
                                } else {
                                    currentState
                                }
                            }
                        }
                    }
                }

                is ImagePickerState.EditImage -> {
                    val result = validateEditImageUseCase.execute(imagePicker.recordId)
                    when (result) {
                        is EditImageValidationResult.AskConfirmation -> {
                            _uiState.update {
                                it.copy(
                                    imagePicker = null,
                                    dialog = DialogState.EditImageTargetConfirmationDialog(
                                        recordId = imagePicker.recordId,
                                        count = result.count,
                                        image = persistedFilename,
                                    ),
                                )
                            }
                        }

                        EditImageValidationResult.Valid -> {
                            refreshWidgetAndClose()
                            editRecordImageUseCase.execute(imagePicker.recordId, persistedFilename)
                        }
                    }
                }
            }
        }
    }

    @UiThread
    fun onDialogDismissed() {
        runSafely {
            _uiState.update {
                (it.dialog as? DialogState.InputDialog.CreateWithImage)
                    ?.image?.let {
                        bitmapStore.delete(it)
                    }
                it.copy(
                    closeScreen = true,
                )
            }
        }
    }

    @UiThread
    fun onRecordDetailsUserTyping(title: String, description: String) {
        _uiState.update {
            val dialogState: DialogState.InputDialog = it.requireDialog()
            it.copy(
                dialog = dialogState.withValidationError(validationError = null),
            )
        }
    }

    @UiThread
    fun onRecordDetailsSubmitRequested(title: String, description: String) {
        val dialogState: DialogState.InputDialog = _uiState.value.requireDialog()
        runSafely {
            when (dialogState) {
                is DialogState.InputDialog.Create, is DialogState.InputDialog.CreateWithImage -> {
                    handleCreateRecordDetailsSubmitted(dialogState, title, description)
                }

                is DialogState.InputDialog.Edit -> {
                    handleEditRecordDialogSubmitted(dialogState, title, description)
                }
            }
        }
    }

    private suspend fun handleCreateRecordDetailsSubmitted(
        dialogState: DialogState.InputDialog,
        title: String,
        description: String,
    ) {
        val image = (dialogState as? DialogState.InputDialog.CreateWithImage)?.image
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
        dialogState: DialogState.InputDialog.Edit,
        title: String,
        description: String,
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
            val dialogState: DialogState.InputDialog = it.requireDialog()
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
