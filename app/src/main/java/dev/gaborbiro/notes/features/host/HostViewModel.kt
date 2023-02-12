package dev.gaborbiro.notes.features.host

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.correctBitmap
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
) : ViewModel() {

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
                inputDialogState = it.inputDialogState.copy(visible = true),
            )
        }
    }

    fun redoImage(templateId: Long) {
        _uiState.update {
            it.copy(
                showImagePicker = true,
                templateIdForRedo = templateId,
            )
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            _uiState.update {
                it.copy(refreshWidgetAndCloseScreen = true)
            }
        }
    }

    fun onPhotoTaken(currentScreenRotation: Int, bitmap: Bitmap?) {
        viewModelScope.launch {
            val uri: Uri? =
                bitmap?.let { persistImage(currentScreenRotation, it, correctRotation = true) }
            _uiState.update {
                it.copy(
                    imageUri = uri,
                    showCamera = false,
                    inputDialogState = it.inputDialogState.copy(visible = true),
                )
            }
        }
    }

    fun onImagePicked(currentScreenRotation: Int, uri: Uri?) {
        viewModelScope.launch {
            val cachedUri = uri?.let { copyImage(currentScreenRotation, uri) }
            _uiState.value.templateIdForRedo?.let {
                repository.updateTemplatePhoto(it, cachedUri)
                _uiState.update {
                    it.copy(
                        refreshWidgetAndCloseScreen = true,
                        showImagePicker = false,
                        templateIdForRedo = null,
                    )
                }
            } ?: run {
                _uiState.update {
                    it.copy(
                        imageUri = cachedUri,
                        showImagePicker = false,
                        inputDialogState = it.inputDialogState.copy(visible = true),
                    )
                }
            }
        }
    }

    fun onDialogDismissed() {
        _uiState.update {
            it.copy(
                refreshWidgetAndCloseScreen = true,
                inputDialogState = it.inputDialogState.copy(visible = false),
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
            it.copy(
                inputDialogState = it.inputDialogState.copy(validationError = null),
            )
        }
    }

    fun onRecordDetailsSubmitted(title: String, description: String) {
        viewModelScope.launch {
            var validationError: String? = null
            if (title.isBlank()) {
                validationError = "Cannot be empty"
            }
            if (validationError != null) {
                _uiState.update {
                    it.copy(
                        inputDialogState = it.inputDialogState.copy(validationError = validationError),
                    )
                }
            } else {
                createNote(title, description)
                _uiState.update {
                    it.copy(
                        inputDialogState = it.inputDialogState.copy(visible = false),
                        refreshWidgetAndCloseScreen = true,
                        imageUri = null,
                    )
                }
            }
        }
    }

    private suspend fun createNote(title: String, description: String) {
        val template = ToSaveTemplate(
            image = uiState.value.imageUri,
            name = title,
            description = description,
        )
        val templateId = repository.saveTemplate(template)
        val record = ToSaveRecord(
            timestamp = LocalDateTime.now(),
            templateId = templateId,
            notes = "",
        )
        repository.saveRecord(record)
    }
}