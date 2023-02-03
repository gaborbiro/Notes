package dev.gaborbiro.notes.features.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.lifecycle.coroutineScope
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.ui.theme.NotesTheme
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import dev.gaborbiro.notes.util.correctBitmapRotation
import dev.gaborbiro.notes.util.hideActionNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime


class HostActivity : AppCompatActivity() {

    companion object {

        fun getDeleteIntent(context: Context, recordId: Long): Intent {
            return Intent(context, HostActivity::class.java).also {
                it.putExtra(EXTRA_ACTION, ACTION_DELETE)
                it.putExtra(EXTRA_RECORD_ID, recordId)
                it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }

        fun launchAddNoteWithCamera(context: Context) {
            launchActivity(context, getActionIntent(context, ACTION_CAMERA))
        }

        fun launchAddNoteWithImage(context: Context) {
            launchActivity(context, getActionIntent(context, ACTION_IMAGE))
        }

        fun launchAddNote(context: Context) {
            launchActivity(context, getActionIntent(context, ACTION_TEXT_ONLY))
        }

        fun launchRedoImage(context: Context, templateId: Long) {
            launchActivity(
                appContext = context,
                intent = getActionIntent(context, ACTION_REDO_IMAGE),
                EXTRA_TEMPLATE_ID to templateId
            )
        }

        private fun launchActivity(
            appContext: Context,
            intent: Intent,
            vararg extras: Pair<String, out Any>
        ) {
            appContext.startActivity(intent.also { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtras(bundleOf(*extras))
            })
        }

        private fun getActionIntent(context: Context, action: String) =
            Intent(context, HostActivity::class.java).also {
                it.putExtra(EXTRA_ACTION, action)
            }

        private const val EXTRA_ACTION = "extra_action"

        private const val ACTION_CAMERA = "camera"
        private const val ACTION_IMAGE = "image"
        private const val ACTION_TEXT_ONLY = "text_only"

        private const val ACTION_DELETE = "delete"
        private const val EXTRA_RECORD_ID = "recordId"

        private const val ACTION_REDO_IMAGE = "redo_image"
        private const val EXTRA_TEMPLATE_ID = "template_id"
    }

    private val repository by lazy { RecordsRepository.get() }
    private val documentWriter by lazy { DocumentWriter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                val action = intent.getStringExtra(EXTRA_ACTION)
                when (action) {
                    ACTION_CAMERA -> {
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.TakePicturePreview(),
                            onResult = this::onPhotoTaken
                        )
                        SideEffect {
                            launcher.launch(null)
                        }
                    }

                    ACTION_IMAGE -> {
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickVisualMedia(),
                            onResult = ::onImagePicked
                        )
                        SideEffect {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }

                    ACTION_TEXT_ONLY -> {
                        ShowNoteEntryDialog { title, description, error ->
                            lifecycle.coroutineScope.launch {
                                val errorMessage = validate(title, description)
                                errorMessage
                                    ?.let {
                                        error.value = it
                                    }
                                    ?: run {
                                        createNote(image = null, title, description)
                                    }
                            }
                        }
                    }

                    ACTION_REDO_IMAGE -> {
                        val templateId = intent.getLongExtra(EXTRA_TEMPLATE_ID, -1L)

                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.TakePicturePreview(),
                            onResult = { bitmap ->
                                bitmap
                                    ?.let { onPhotoTaken(it, templateId) }
                                    ?: run { finish() }
                            }
                        )
                        SideEffect {
                            launcher.launch(null)
                        }
                    }

                    ACTION_DELETE -> {
                        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)

                        if (recordId != -1L) {
                            SideEffect {
                                lifecycle.coroutineScope.launch {
                                    if (RecordsRepository.get().delete(recordId)) {
                                        Toast.makeText(
                                            this@HostActivity,
                                            "Deleted",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        NotesWidgetsUpdater.oneOffUpdate(this@HostActivity)
                                        finish()
                                    }
                                }
                            }
                        }
                        hideActionNotification()
                    }

                    else -> {
                        Toast.makeText(
                            this,
                            "Unexpected action in HostActivity: $action",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    @Composable
    private fun ShowNoteEntryDialog(onData: (title: String, description: String, error: MutableState<String?>) -> Unit) {
        val dialogState = remember { mutableStateOf(true) }

        if (dialogState.value) {
            val error: MutableState<String?> = remember { mutableStateOf(null) }
            Dialog(onDismissRequest = { dialogState.value = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    NoteInputDialogContent(
                        onCancel = {
                            dialogState.value = false
                        },
                        onData = { title, description ->
                            onData(title, description, error)
                        },
                        error = error,
                    )
                }
            }
        } else {
            finish()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun NoteInputDialogContent(
        onCancel: () -> Unit,
        onData: (String, String) -> Unit,
        error: MutableState<String?>,
    ) {
        val title = remember { mutableStateOf("") }
        val description = remember { mutableStateOf("") }

        val onDone: () -> Unit = {
            onData(title.value.trim(), description.value.trim())
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add a note",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                )
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    contentDescription = "Cancel",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            onCancel()
                        },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(key1 = Unit) {
                delay(100)
                focusRequester.requestFocus()
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isError = error.value != null,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = {
                    Text(
                        text = error.value ?: "Title",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                },
                value = title.value,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                onValueChange = {
                    error.value = null
                    title.value = it
                },
            )

            Spacer(modifier = Modifier.size(PaddingDefault))

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = {
                    Text(
                        text = "Description",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                value = description.value,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
//                    imeAction = ImeAction.Done
                ),
//                keyboardActions = KeyboardActions(onDone = {
//                    onDone()
//                }),
                onValueChange = {
                    description.value = it
                },
            )

            Spacer(modifier = Modifier.size(PaddingDefault))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 40.dp)
            ) {
                Text(text = "Save")
            }
        }
    }

    private fun onPhotoTaken(bitmap: Bitmap?) {
        lifecycle.coroutineScope.launch {
            val uri: Uri? = bitmap?.let { persistBitmap(it) }
            setContent {
                ShowNoteEntryDialog { title, description, error ->
                    lifecycle.coroutineScope.launch {
                        val errorMessage = validate(title, description)
                        errorMessage
                            ?.let {
                                error.value = it
                            }
                            ?: run {
                                createNote(uri, title, description)
                            }
                    }
                }
            }
        }
    }

    private fun onPhotoTaken(bitmap: Bitmap, templateId: Long) {
        lifecycle.coroutineScope.launch {
            val uri = persistBitmap(bitmap)
            repository.updateTemplatePhoto(templateId, uri)
            NotesWidgetsUpdater.oneOffUpdate(this@HostActivity)
            finish()
        }
    }

    private suspend fun persistBitmap(bitmap: Bitmap): Uri {
        val correctedBitmap = correctBitmapRotation(display!!, bitmap)
        val uri = ByteArrayOutputStream().let { stream ->
            correctedBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
            val inStream = ByteArrayInputStream(stream.toByteArray())
            documentWriter.write(inStream, "${System.currentTimeMillis()}.png")
        }
        return uri
    }

    private fun onImagePicked(image: Uri?) {
        setContent {
            ShowNoteEntryDialog { title, description, error ->
                lifecycle.coroutineScope.launch {
                    val errorMessage = validate(title, description)
                    errorMessage
                        ?.let {
                            error.value = it
                        }
                        ?: run {
                            createNote(image, title, description)
                        }
                }
            }
        }
    }

    private fun createNote(image: Bitmap, name: String, description: String) {

    }

    private suspend fun validate(title: String, description: String): String? {
        if (title.isBlank()) return "Cannot be empty"
        val templatesWithSameTitle = repository.getTemplatesByName(title)
        if (templatesWithSameTitle.isNotEmpty()) {
            Toast.makeText(this, "Already exists", Toast.LENGTH_SHORT).show()
            return "Already exists"
        }
        return null
    }

    private suspend fun createNote(image: Uri?, title: String, description: String) {
        val template = ToSaveTemplate(
            image = image,
            name = title,
            description = description,
        )
        val templateId = repository.saveTemplate(template)
        val record = ToSaveRecord(
            timestamp = LocalDateTime.now(),
            templateId = templateId,
            notes = "",
        )
        val id = repository.saveRecord(record)
        Toast.makeText(this@HostActivity, "Record saved ($id)", Toast.LENGTH_SHORT).show()
        NotesWidgetsUpdater.oneOffUpdate(this@HostActivity)
        finish()
    }
}