package dev.gaborbiro.notes.features.host

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.correctBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class HostViewModel(
    private val repository: RecordsRepository,
    private val documentWriter: DocumentWriter,
    private val bitmapLoader: BitmapLoader,
    private val createRecordUseCase: CreateRecordUseCase,
    private val editRecordUseCase: EditRecordUseCase,
) : BaseViewModel() {

    companion object {
        enum class EditTarget {
            RECORD, TEMPLATE
        }
    }

    private val _uiState = MutableStateFlow(HostUIState())
    val uiState: StateFlow<HostUIState> = _uiState.asStateFlow()

    var toast: String? by mutableStateOf(null)

    @UiThread
    fun initWithCamera() {
        _uiState.update {
            it.copy(showCamera = true)
        }
    }

    @UiThread
    fun initWithImagePicker() {
        _uiState.update {
            it.copy(showImagePicker = true)
        }
    }

    @UiThread
    fun initWithJustText() {
        _uiState.update {
            it.copy(
                dialog = DialogState.InputDialogState.Create(),
            )
        }
    }

    @UiThread
    fun redoImage(templateId: Long) {
        _uiState.update {
            it.copy(
                showImagePicker = true,
                templateIdForImageRedo = templateId,
            )
        }
    }

//    @UiThread
//    fun deleteRecord(recordId: Long) {
//        runSafely {
//            val record = repository.deleteRecord(recordId)
//            val (templateDeleted, imageDeleted) = repository.deleteTemplateIfUnused(record.template.id)
//            Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
//            _uiState.update {
//                it.copy(refreshWidget = true, closeScreen = true)
//            }
//        }
//    }

    @UiThread
    fun startWithEdit(recordId: Long) {
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
            val uri: Uri? =
                bitmap?.let { persistImage(currentScreenRotation, it, correctRotation = true) }
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
            val cachedUri = uri?.let { copyImage(currentScreenRotation, uri) }
            _uiState.value.templateIdForImageRedo?.let {
                repository.updateTemplate(it, cachedUri)
                _uiState.update {
                    it.copy(
                        refreshWidget = true,
                        closeScreen = true,
                        showImagePicker = false,
                        templateIdForImageRedo = null,
                    )
                }
            } ?: run {
                _uiState.update {
                    it.copy(
                        showImagePicker = false,
                        dialog = DialogState.InputDialogState.CreateWithImage(image = cachedUri),
                    )
                }
            }
        }
    }

    @UiThread
    fun onDialogDismissed() {
        _uiState.update {
            it.copy(
                closeScreen = true,
                dialog = null,
            )
        }
    }

    private suspend fun copyImage(currentScreenRotation: Int, uri: Uri): Uri? {
        return bitmapLoader.loadBitmap(uri)?.let {
            persistImage(currentScreenRotation, it, false)
        }
    }

    private suspend fun persistImage(
        currentScreenRotation: Int,
        bitmap: Bitmap,
        correctRotation: Boolean
    ): Uri {
        val correctedBitmap =
            correctBitmap(currentScreenRotation, bitmap, correctRotation, correctWidth = true)
        val uri = ByteArrayOutputStream().let { stream ->
            correctedBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val inStream = ByteArrayInputStream(stream.toByteArray())
            documentWriter.write(inStream, "${System.currentTimeMillis()}.png")
        }
        return uri
    }

    @UiThread
    fun onRecordDetailsChanged(title: String, description: String) {
        val image: Uri? = (_uiState.value.dialog as? DialogState.InputDialogState.Edit)?.image
        runSafely {
            _uiState.update {
                val dialogState: DialogState.InputDialogState? = it.requireDialog()
                it.copy(
                    dialog = dialogState?.withValidationError(validationError = null),
                )
            }
        }
    }

    @UiThread
    fun onRecordDetailsSubmitted(title: String, description: String) {
        runSafely {
            var validationError: String? = null
            if (title.isBlank()) {
                validationError = "Cannot be empty"
            }

            if (validationError != null) {
                _uiState.update {
                    val dialogState: DialogState.InputDialogState? = it.requireDialog()
                    it.copy(
                        dialog = dialogState?.withValidationError(validationError = validationError),
                    )
                }
            } else {
                val dialogState: DialogState.InputDialogState? = _uiState.value.requireDialog()
                when (dialogState) {
                    is DialogState.InputDialogState.Create -> {
                        createRecord(
                            image = null,
                            title = title,
                            description = description
                        )
                    }

                    is DialogState.InputDialogState.CreateWithImage -> {
                        createRecord(
                            image = dialogState.image,
                            title = title,
                            description = description
                        )
                    }

                    is DialogState.InputDialogState.Edit -> {
                        editRecord(
                            recordId = dialogState.recordId,
                            newTitle = title,
                            newDescription = description
                        )
                    }

                    null -> {
                        throw IllegalArgumentException(
                            "Expected InputDialogState. Found null"
                        )
                    }
                }
            }
        }
    }

    private suspend fun createRecord(image: Uri?, title: String, description: String) {
        createRecordUseCase.execute(image, title, description)
        _uiState.update {
            it.copy(
                refreshWidget = true, closeScreen = true,
            )
        }
    }

    @UiThread
    private suspend fun editRecord(recordId: Long, newTitle: String, newDescription: String) {
        val count = editRecordUseCase.execute(
            recordId = recordId,
            title = newTitle,
            description = newDescription,
            force = false,
        )
        if (count == null) {
            _uiState.update {
                it.copy(
                    refreshWidget = true, closeScreen = true,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    dialog = DialogState.EditTargetConfirmationDialog(
                        recordId = recordId,
                        count = count,
                        newTitle = newTitle,
                        newDescription = newDescription,
                    ),
                )
            }
        }
    }

    @UiThread
    fun onEditTargetConfirmed(target: EditTarget) {
        val dialogState: DialogState.EditTargetConfirmationDialog? = _uiState.value.requireDialog()
        dialogState
            ?.let {
                val recordId = dialogState.recordId
                val title = dialogState.newTitle
                val description = dialogState.newDescription
                runOnBackgroundThread {
                    when (target) {
                        EditTarget.RECORD -> {
                            editRecordUseCase.execute(
                                recordId = recordId,
                                title = title,
                                description = description,
                                force = true,
                            )
                        }

                        EditTarget.TEMPLATE -> {
                            doEditTemplate(
                                recordId = recordId,
                                title = title,
                                description = description,
                            )
                        }
                    }
                    _uiState.update {
                        it.copy(
                            refreshWidget = true, closeScreen = true,
                        )
                    }
                }
            }
    }

    @WorkerThread
    private suspend fun doEditTemplate(recordId: Long, title: String, description: String) {
        val record = repository.getRecord(recordId)!!
        repository.updateTemplate(
            templateId = record.template.id,
            image = null,
            title = title,
            description = description
        )
    }

    private inline fun <reified T : DialogState> HostUIState.requireDialog(): T? {
        if (this.dialog?.let { it is T } != true) throw IllegalArgumentException(
            "Expected InputDialogState. Found ${this.dialog}"
        )
        return this.dialog as T?
    }
}