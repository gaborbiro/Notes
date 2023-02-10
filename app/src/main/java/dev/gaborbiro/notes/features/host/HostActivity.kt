package dev.gaborbiro.notes.features.host

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.widget.NotesWidgetsUpdater
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.ui.theme.NotesTheme
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.hideActionNotification


class HostActivity : AppCompatActivity() {

    companion object {

        fun getDeleteRecordIntent(context: Context, recordId: Long): Intent {
            return Intent(context, HostActivity::class.java).also {
                it.putExtra(EXTRA_ACTION, ACTION_DELETE)
                it.putExtra(EXTRA_RECORD_ID, recordId)
                it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        }

        fun launchAddNoteWithCamera(context: Context) =
            launchActivity(context, getCameraIntent(context))

        private fun getCameraIntent(context: Context) = getActionIntent(context, ACTION_CAMERA)

        fun launchAddNoteWithImage(context: Context) =
            launchActivity(context, getImagePickerIntent(context))

        private fun getImagePickerIntent(context: Context) =
            getActionIntent(context, ACTION_PICK_IMAGE)

        fun launchAddNote(context: Context) = launchActivity(context, getTextOnlyIntent(context))

        private fun getTextOnlyIntent(context: Context) = getActionIntent(context, ACTION_TEXT_ONLY)


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
        private const val ACTION_PICK_IMAGE = "pick_image"
        private const val ACTION_TEXT_ONLY = "text_only"

        private const val ACTION_DELETE = "delete"
        private const val EXTRA_RECORD_ID = "recordId"

        private const val ACTION_REDO_IMAGE = "redo_image"
        private const val EXTRA_TEMPLATE_ID = "template_id"
    }

    private val viewModel by lazy {
        HostViewModel(
            RecordsRepository.get(),
            DocumentWriter(this),
            BitmapLoader(this),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra(EXTRA_ACTION)) {
            finish()
            return
        }
        setContent {
            NotesTheme {
                val action = intent.getStringExtra(EXTRA_ACTION)
                intent.removeExtra(EXTRA_ACTION) // consume intent
                when (action) {
                    ACTION_CAMERA -> viewModel.initWithCamera()
                    ACTION_PICK_IMAGE -> viewModel.initWithImagePicker()
                    ACTION_TEXT_ONLY -> viewModel.initWithJustText()

                    ACTION_REDO_IMAGE -> {
                        val templateId = intent.getLongExtra(EXTRA_TEMPLATE_ID, -1L)
                        viewModel.redoImage(templateId)
                    }

                    ACTION_DELETE -> {
                        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                        viewModel.deleteRecord(recordId)
                        hideActionNotification()
                    }

                    null -> {
                        // ignore
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

            val uiState: HostUIState by viewModel.uiState.collectAsStateWithLifecycle()

            if (uiState.showCamera) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview(),
                    onResult = {
                        viewModel.onPhotoTaken(display!!.rotation, it)
                    }
                )
                SideEffect {
                    launcher.launch(null)
                }
            }
            if (uiState.showImagePicker) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia(),
                    onResult = {
                        viewModel.onImagePicked(display!!.rotation, it)
                    }
                )
                SideEffect {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            if (uiState.refreshWidgetAndCloseScreen) {
                NotesWidgetsUpdater.oneOffUpdate(this@HostActivity)
                finish()
            }

            InputDialog(viewModel.uiState.collectAsStateWithLifecycle().value.inputDialogState)

            viewModel.toast?.let {
                viewModel.toast = null
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Composable
    fun InputDialog(dialogState: InputDialogState) {
        if (dialogState.visible) {
            Dialog(onDismissRequest = { viewModel.onDialogDismissed() }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    NoteInputDialogContent(
                        onCancel = { viewModel.onDialogDismissed() },
                        onSubmit = viewModel::onRecordDetailsSubmitted,
                        onChange = viewModel::onRecordDetailsChanged,
                        error = dialogState.validationError,
                    )
                }
            }
        }
    }
}

