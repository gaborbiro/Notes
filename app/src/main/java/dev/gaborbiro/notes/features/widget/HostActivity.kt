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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.lifecycle.coroutineScope
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.util.correctBitmapRotation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime


class HostActivity : AppCompatActivity() {

    companion object {

        fun launchAddNoteWithCamera(context: Context) {
            launchActivity(context, getActionIntent(context, EXTRA_ACTION_CAMERA))
        }

        fun launchAddNoteWithImage(context: Context) {
            launchActivity(context, getActionIntent(context, EXTRA_ACTION_IMAGE))
        }

        fun launchAddNote(context: Context) {
            launchActivity(context, getActionIntent(context, EXTRA_ACTION_TEXT_ONLY))
        }

        fun launchRedoImage(context: Context, templateId: Long) {
            launchActivity(
                appContext = context,
                intent = getActionIntent(context, EXTRA_ACTION_REDO_IMAGE),
                EXTRA_KEY_TEMPLATE_ID to templateId
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
        private const val EXTRA_ACTION_CAMERA = "camera"
        private const val EXTRA_ACTION_IMAGE = "image"
        private const val EXTRA_ACTION_TEXT_ONLY = "text_only"

        private const val EXTRA_ACTION_REDO_IMAGE = "redo_image"
        private const val EXTRA_KEY_TEMPLATE_ID = "template_id"

        internal data class Target(
            val record: Long?,
            val template: Long?
        )
    }

    private val repository by lazy { RecordsRepository.get() }
    private val documentWriter by lazy { DocumentWriter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val action = intent.getStringExtra(EXTRA_ACTION)
            when (action) {
                EXTRA_ACTION_CAMERA -> {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.TakePicturePreview(),
                        onResult = this::onPhotoTaken
                    )
                    SideEffect {
                        launcher.launch(null)
                    }
                }

                EXTRA_ACTION_IMAGE -> {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia(),
                        onResult = ::onImagePicked
                    )
                    SideEffect {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }

                EXTRA_ACTION_TEXT_ONLY -> {
                    ShowNoteEntryDialog { name, description ->
                        createNote(image = null, name, description)
                    }
                }

                EXTRA_ACTION_REDO_IMAGE -> {
                    val templateId = intent.getLongExtra(EXTRA_KEY_TEMPLATE_ID, -1L)

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

    @Composable
    private fun ShowNoteEntryDialog(onNoteReady: (name: String, description: String) -> Unit) {
        val dialogState = remember { mutableStateOf(true) }

        if (dialogState.value) {
            Dialog(onDismissRequest = { dialogState.value = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        InputDialogContent(
                            onClose = {
                                dialogState.value = false
                            },
                            setValue = { name, description ->
                                onNoteReady(name, description)
                            }
                        )
                    }
                }
            }
        } else {
            finish()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun InputDialogContent(
        onClose: () -> Unit,
        setValue: (String, String) -> Unit
    ) {
        val inputFieldError = remember { mutableStateOf(false) }
        val inputFieldValue = remember { mutableStateOf("") }
        val inputFieldDescription = remember { mutableStateOf("") }

        val onDone: () -> Unit = {
            if (inputFieldValue.value.isEmpty()) {
                inputFieldError.value = true
            } else {
                setValue(inputFieldValue.value, inputFieldDescription.value)
                onClose()
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add a note",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold
                    )
                )
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = "",
                    tint = colorResource(R.color.black),
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                        .clickable {
                            onClose()
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(key1 = Unit) {
                delay(300)
                focusRequester.requestFocus()
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = colorResource(id = if (inputFieldError.value.not()) R.color.teal_200 else R.color.red)
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Name",
                        color = colorResource(R.color.light_grey)
                    )
                },
                value = inputFieldValue.value,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                onValueChange = {
                    inputFieldError.value = false
                    inputFieldValue.value = it
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = colorResource(id = R.color.teal_200)
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                placeholder = {
                    Text(
                        text = "Description",
                        color = colorResource(R.color.light_grey)
                    )
                },
                value = inputFieldDescription.value,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    onDone()
                }),
                onValueChange = {
                    inputFieldDescription.value = it
                },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                Button(
                    onClick = onDone,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Save")
                }
            }
        }
    }

    private fun onPhotoTaken(bitmap: Bitmap?) {
        lifecycle.coroutineScope.launch {
            val uri: Uri? = bitmap?.let { persistBitmap(it) }
            setContent {
                ShowNoteEntryDialog { name, description ->
                    createNote(uri, name, description)
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
            ShowNoteEntryDialog { name, description ->
                createNote(image, name, description)
            }
        }
    }

    private fun createNote(image: Bitmap, name: String, description: String) {

    }

    private fun createNote(image: Uri?, name: String, description: String) {
        lifecycle.coroutineScope.launch {
            val template = ToSaveTemplate(
                image = image,
                name = name,
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
}