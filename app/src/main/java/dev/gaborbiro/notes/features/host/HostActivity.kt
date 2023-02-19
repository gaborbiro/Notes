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
import dev.gaborbiro.notes.features.host.dialog.EditTarget
import dev.gaborbiro.notes.features.host.dialog.EditTargetConfirmationDialogContent
import dev.gaborbiro.notes.features.host.dialog.NoteInputDialogContent
import dev.gaborbiro.notes.features.widget.NotesWidgetsUpdater
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.ui.theme.NotesTheme
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.hideActionNotification


class HostActivity : AppCompatActivity() {

    companion object {

//        fun getDeleteRecordIntent(context: Context, recordId: Long): Intent {
//            return Intent(context, HostActivity::class.java).also {
//                it.putExtra(EXTRA_ACTION, ACTION_DELETE)
//                it.putExtra(EXTRA_RECORD_ID, recordId)
//                it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            }
//        }

        fun launchAddNoteWithCamera(context: Context) =
            launchActivity(context, getCameraIntent(context))

        private fun getCameraIntent(context: Context) = getActionIntent(context, Action.CAMERA)

        fun launchAddNoteWithImage(context: Context) =
            launchActivity(context, getImagePickerIntent(context))

        private fun getImagePickerIntent(context: Context) =
            getActionIntent(context, Action.PICK_IMAGE)

        fun launchAddNote(context: Context) = launchActivity(context, getTextOnlyIntent(context))

        private fun getTextOnlyIntent(context: Context) = getActionIntent(context, Action.TEXT_ONLY)

        fun launchRedoImage(context: Context, templateId: Long) {
            launchActivity(
                appContext = context,
                intent = getActionIntent(context, Action.REDO_IMAGE),
                EXTRA_TEMPLATE_ID to templateId
            )
        }

        fun launchEdit(context: Context, recordId: Long) {
            launchActivity(
                appContext = context,
                intent = getActionIntent(context, Action.EDIT),
                EXTRA_RECORD_ID to recordId
            )
        }

        private fun launchActivity(
            appContext: Context,
            intent: Intent,
            vararg extras: Pair<String, out Any>
        ) {
            appContext.startActivity(intent.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtras(bundleOf(*extras))
            })
        }

        private fun getActionIntent(context: Context, action: Action) =
            Intent(context, HostActivity::class.java).also {
                it.putExtra(EXTRA_ACTION, action.name)
            }

        private const val EXTRA_ACTION = "extra_action"

        private enum class Action {
            CAMERA, PICK_IMAGE, TEXT_ONLY, DELETE, REDO_IMAGE, EDIT
        }

        private const val EXTRA_TEMPLATE_ID = "template_id"
        private const val EXTRA_RECORD_ID = "record_id"
    }

    private val viewModel by lazy {
        val repository = RecordsRepository.get()
        HostViewModel(
            repository,
            DocumentWriter(this),
            BitmapLoader(this),
            CreateRecordUseCase(repository),
            EditRecordUseCase(repository),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra(EXTRA_ACTION)) {
            finish()
            return
        }

        val action = intent.getStringExtra(EXTRA_ACTION)?.let { Action.valueOf(it) }
        intent.removeExtra(EXTRA_ACTION) // consume intent

        when (action) {
            Action.CAMERA -> viewModel.initWithCamera()
            Action.PICK_IMAGE -> viewModel.initWithImagePicker()
            Action.TEXT_ONLY -> viewModel.initWithJustText()

            Action.REDO_IMAGE -> {
                val templateId = intent.getLongExtra(EXTRA_TEMPLATE_ID, -1L)
                viewModel.redoImage(templateId)
            }

            Action.DELETE -> {
                val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                viewModel.deleteRecord(recordId)
                hideActionNotification()
            }

            Action.EDIT -> {
                val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                viewModel.startWithEdit(recordId)
            }

            null -> {
                // ignore
            }
        }

        setContent {
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

            if (uiState.refreshWidget) {
                NotesWidgetsUpdater.oneOffUpdate(this@HostActivity)
            }

            NotesTheme {
                Dialog(viewModel.uiState.collectAsStateWithLifecycle().value.dialog)
            }

            viewModel.toast?.let {
                viewModel.toast = null
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }

            if (uiState.closeScreen) {
                finish()
            }
        }
    }

    @Composable
    fun Dialog(dialogState: DialogState?) {
        when (dialogState) {
            is DialogState.InputDialogState -> InputDialog(dialogState)
            is DialogState.EditTargetConfirmationDialog -> EditTargetConfirmationDialog(dialogState)
            null -> {
                // no dialog is shown
            }
        }
    }

    @Composable
    private fun InputDialog(dialogState: DialogState.InputDialogState) {
        Dialog(onDismissRequest = { viewModel.onDialogDismissed() }) {
            val image = (dialogState as? DialogState.InputDialogState.Edit)?.image
            val title = (dialogState as? DialogState.InputDialogState.Edit)?.title
            val description = (dialogState as? DialogState.InputDialogState.Edit)?.description
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
            ) {
                NoteInputDialogContent(
                    onCancel = { viewModel.onDialogDismissed() },
                    onSubmit = { title, description ->
                        viewModel.onRecordDetailsSubmitted(title, description)
                    },
                    onChange = { title, description ->
                        viewModel.onRecordDetailsChanged(title, description)
                    },
                    title = title,
                    description = description,
                    error = dialogState.validationError,
                )
            }
        }
    }

    @Composable
    private fun EditTargetConfirmationDialog(dialogState: DialogState.EditTargetConfirmationDialog) {
        Dialog(onDismissRequest = { viewModel.onDialogDismissed() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp,
            ) {
                EditTargetConfirmationDialogContent(
                    count = dialogState.count,
                    onSubmit = { target ->
                        val vmTarget = when (target) {
                            EditTarget.RECORD -> HostViewModel.Companion.EditTarget.RECORD
                            EditTarget.TEMPLATE -> HostViewModel.Companion.EditTarget.TEMPLATE
                        }
                        viewModel.onEditTargetConfirmed(vmTarget)
                    },
                    onCancel = { viewModel.onDialogDismissed() },
                )
            }
        }
    }
}

