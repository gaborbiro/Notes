package dev.gaborbiro.notes.features.host

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.TransactionProvider
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.correctBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class HostViewModel(
    private val repository: RecordsRepository,
    private val documentWriter: DocumentWriter,
    private val bitmapLoader: BitmapLoader,
    private val transactionProvider: TransactionProvider,
) : ViewModel() {

    companion object {
        enum class EditTarget {
            RECORD, TEMPLATE
        }
    }

    private val _uiState = MutableStateFlow(HostUIState())
    val uiState: StateFlow<HostUIState> = _uiState.asStateFlow()

    var toast: String? by mutableStateOf(null)

    fun initWithCamera() {
        _uiState.update {
            it.copy(showCamera = true)
        }
    }

    fun initWithImagePicker() {
        _uiState.update {
            it.copy(showImagePicker = true)
        }
    }

    fun initWithJustText() {
        _uiState.update {
            it.copy(
                dialog = DialogState.InputDialogState.Create(),
            )
        }
    }

    fun redoImage(templateId: Long) {
        _uiState.update {
            it.copy(
                showImagePicker = true,
                templateIdForImageRedo = templateId,
            )
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            val (templateDeleted, imageDeleted) = repository.deleteRecordAndCleanupTemplate(recordId)
            Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
            _uiState.update {
                it.copy(refreshWidgetAndCloseScreen = true)
            }
        }
    }

    fun startWithEdit(recordId: Long) {
        viewModelScope.launch {
            val record = repository.getRecord(recordId)!!
            _uiState.update {
                it.copy(
                    dialog = DialogState.InputDialogState.Edit(
                        recordId = recordId,
                        title = record.template.name,
                        description = record.template.description,
                        validationError = null,
                    ),
                )
            }
        }
    }

    fun onPhotoTaken(currentScreenRotation: Int, bitmap: Bitmap?) {
        viewModelScope.launch {
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

    fun onImagePicked(currentScreenRotation: Int, uri: Uri?) {
        viewModelScope.launch {
            val cachedUri = uri?.let { copyImage(currentScreenRotation, uri) }
            _uiState.value.templateIdForImageRedo?.let {
                repository.updateTemplate(it, cachedUri)
                _uiState.update {
                    it.copy(
                        refreshWidgetAndCloseScreen = true,
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

    fun onDialogDismissed() {
        _uiState.update {
            it.copy(
                refreshWidgetAndCloseScreen = true,
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

    fun onRecordDetailsChanged(title: String, description: String) {
        _uiState.update {
            val dialogState: DialogState.InputDialogState? = it.requireDialog()
            it.copy(
                dialog = dialogState?.withValidationError(validationError = null),
            )
        }
    }

    fun onRecordDetailsSubmitted(title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            var validationError: String? = null
            if (title.isBlank()) {
                validationError = "Cannot be empty"
            }
            if (validationError != null) {
                _uiState.update {
                    val dialogState: DialogState.InputDialogState? = it.requireDialog()
                    it.copy(
                        dialog = dialogState?.withValidationError(validationError = null),
                    )
                }
            } else {
                val dialogState: DialogState.InputDialogState? = _uiState.value.requireDialog()
                when (dialogState) {
                    is DialogState.InputDialogState.Create -> {
                        createRecord(null, title, description)
                        _uiState.update {
                            it.copy(
                                dialog = null,
                                refreshWidgetAndCloseScreen = true,
                            )
                        }
                    }

                    is DialogState.InputDialogState.CreateWithImage -> {
                        createRecord(dialogState.image, title, description)
                        _uiState.update {
                            it.copy(
                                dialog = null,
                                refreshWidgetAndCloseScreen = true,
                            )
                        }
                    }

                    is DialogState.InputDialogState.Edit -> {
                        val record = repository.getRecord(dialogState.recordId)!!
                        val records = repository.getRecords(record.template.id)
                        if (records.size < 2) {
                            editRecord(
                                record = record,
                                title = title,
                                description = description,
                            )
                        } else {
                            _uiState.update {
                                it.copy(
                                    dialog = DialogState.EditTargetConfirmationDialog(
                                        recordId = dialogState.recordId,
                                        count = records.size,
                                        newTitle = title,
                                        newDescription = description,
                                    ),
                                )
                            }
                        }
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

    private inline fun <reified T : DialogState> HostUIState.requireDialog(): T? {
        if (this.dialog?.let { it is T } != true) throw IllegalArgumentException(
            "Expected InputDialogState. Found ${this.dialog}"
        )
        return this.dialog as T?
    }

    fun onEditTargetConfirmed(target: EditTarget) {
        val dialogState: DialogState.EditTargetConfirmationDialog? = _uiState.value.requireDialog()
        dialogState
            ?.let {
                viewModelScope.launch(Dispatchers.IO) {
                    val record = repository.getRecord(dialogState.recordId)!!
                    when (target) {
                        EditTarget.RECORD -> {
                            editRecord(
                                record = record,
                                title = dialogState.newTitle,
                                description = dialogState.newDescription,
                            )
                        }

                        EditTarget.TEMPLATE -> {
                            editTemplate(
                                record = record,
                                title = dialogState.newTitle,
                                description = dialogState.newDescription,
                            )
                        }
                    }
                    _uiState.update {
                        it.copy(
                            dialog = null,
                            refreshWidgetAndCloseScreen = true,
                        )
                    }
                }
            }
    }

    private suspend fun editTemplate(record: Record, title: String, description: String) {
        repository.updateTemplate(
            templateId = record.template.id,
            image = null,
            title = title,
            description = description
        )
    }

    private suspend fun editRecord(record: Record, title: String, description: String) {
        transactionProvider.runInTransaction {
            val (templateDeleted, imageDeleted) = repository.deleteRecordAndCleanupTemplate(record.id)
            Log.d("Notes", "template deleted: $templateDeleted, image deleted: $imageDeleted")
            val newRecord = ToSaveRecord(
                timestamp = record.timestamp,
                template = ToSaveTemplate(
                    image = record.template.image,
                    name = title,
                    description = description,
                ),
            )
            repository.saveRecord(newRecord)
        }
    }

    private suspend fun createRecord(imageUri: Uri?, title: String, description: String) {
        val record = ToSaveRecord(
            timestamp = LocalDateTime.now(),
            template = ToSaveTemplate(
                image = imageUri,
                name = title,
                description = description,
            ),
        )
        repository.saveRecord(record)
    }
}