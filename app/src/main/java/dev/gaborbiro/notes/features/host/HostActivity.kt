package dev.gaborbiro.notes.features.host

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.common.BaseErrorDialogActivity
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.features.host.usecase.CacheImageUseCase
import dev.gaborbiro.notes.features.host.usecase.CreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.EditRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateUseCase
import dev.gaborbiro.notes.features.host.usecase.GetRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateCreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditRecordUseCase
import dev.gaborbiro.notes.features.host.views.EditTarget
import dev.gaborbiro.notes.features.host.views.EditTargetConfirmationDialogContent
import dev.gaborbiro.notes.features.host.views.NoteInputDialogContent
import dev.gaborbiro.notes.features.widget.NotesWidget
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.ui.theme.NotesTheme
import java.io.File
import java.time.LocalDateTime


class HostActivity : BaseErrorDialogActivity() {

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

        fun launchShowImage(context: Context, recordId: Long) = launchActivity(
            appContext = context,
            intent = getShowImageIntent(context),
            EXTRA_RECORD_ID to recordId,
        )

        private fun getShowImageIntent(context: Context) =
            getActionIntent(context, Action.SHOW_IMAGE)

        fun launchAddNote(context: Context) = launchActivity(context, getTextOnlyIntent(context))

        private fun getTextOnlyIntent(context: Context) = getActionIntent(context, Action.TEXT_ONLY)

        fun launchRedoImage(context: Context, recordId: Long) {
            launchActivity(
                appContext = context,
                intent = getActionIntent(context, Action.REDO_IMAGE),
                EXTRA_RECORD_ID to recordId
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
            vararg extras: Pair<String, Any>,
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
            CAMERA, PICK_IMAGE, TEXT_ONLY, REDO_IMAGE, EDIT, SHOW_IMAGE
        }

        private const val EXTRA_RECORD_ID = "record_id"
    }

    private val viewModel by lazy {
        val repository = RecordsRepository.get()
        val bitmapStore = BitmapStore(this)
        HostViewModel(
            repository,
            CreateRecordUseCase(repository),
            EditRecordUseCase(repository),
            EditTemplateUseCase(repository),
            ValidateEditRecordUseCase(repository),
            ValidateCreateRecordUseCase(),
            CacheImageUseCase(bitmapStore),
            ValidateEditImageUseCase(repository),
            EditRecordImageUseCase(repository),
            EditTemplateImageUseCase(repository),
            GetRecordImageUseCase(repository, bitmapStore),
        )
    }

    override fun baseViewModel(): BaseViewModel {
        return viewModel
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
            Action.CAMERA -> viewModel.onStartWithCamera()
            Action.PICK_IMAGE -> viewModel.onStartWithImagePicker()
            Action.TEXT_ONLY -> viewModel.onStartWithJustText()

            Action.REDO_IMAGE -> {
                val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                viewModel.onStartWithRedoImage(recordId)
            }

//            Action.DELETE -> {
//                val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
//                viewModel.deleteRecord(recordId)
//                hideActionNotification()
//            }

            Action.EDIT -> {
                val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                viewModel.onStartWithEdit(recordId)
            }

            Action.SHOW_IMAGE -> {
                val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
                viewModel.onStartWithShowImage(recordId)
            }

            null -> {
                // ignore
            }
        }

        setContent {
            HandleErrors()
            val uiState: HostUIState by viewModel.uiState.collectAsStateWithLifecycle()

            if (uiState.showCamera) {
                val file = File(filesDir, "public/${LocalDateTime.now()}.png")
                val uri = FileProvider.getUriForFile(
                    /* context = */ this,
                    /* authority = */ applicationContext.packageName + ".provider",
                    /* file = */ file,
                )

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture(),
                    onResult = {
                        viewModel.onPhotoTaken(uri)
                    }
                )
                SideEffect {
                    launcher.launch(uri)
                }
            }

            when (uiState.imagePicker) {
                is ImagePickerState.Create, is ImagePickerState.EditImage -> {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia(),
                        onResult = {
                            viewModel.onImagePicked(it)
                        }
                    )
                    SideEffect {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }

                null -> {
                    // nothing to do
                }
            }

            if (uiState.refreshWidget) {
                NotesWidget.reload(this@HostActivity)
            }

            NotesTheme {
                NotesDialog(viewModel.uiState.collectAsStateWithLifecycle().value.dialog)
            }

            if (uiState.closeScreen) {
                finish()
            }
        }
    }

    @Composable
    fun NotesDialog(dialogState: DialogState?) {
        when (dialogState) {
            is DialogState.InputDialogState -> InputDialog(dialogState)
            is DialogState.EditTargetConfirmationDialog -> EditTargetConfirmationDialog(dialogState)
            is DialogState.EditImageTargetConfirmationDialog -> EditImageTargetConfirmationDialog(
                dialogState
            )

            is DialogState.ShowImageDialog -> ImageDialog(image = dialogState.bitmap)

            null -> {
                // no dialog is shown
            }
        }
    }

    @Composable
    private fun InputDialog(dialogState: DialogState.InputDialogState) {
        Dialog(
            onDismissRequest = { viewModel.onDialogDismissed() },
        ) {
//            val image = (dialogState as? DialogState.InputDialogState.Edit)?.image
            val title = (dialogState as? DialogState.InputDialogState.Edit)?.title
            val description = (dialogState as? DialogState.InputDialogState.Edit)?.description
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .wrapContentHeight()
            ) {
                NoteInputDialogContent(
                    onCancel = { viewModel.onDialogDismissed() },
                    onSubmit = { title, description ->
                        viewModel.onRecordDetailsSubmitRequested(title, description)
                    },
                    onChange = { title, description ->
                        viewModel.onRecordDetailsUserTyping(title, description)
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
        TargetConfirmationDialog(
            count = dialogState.count,
            onConfirmed = { viewModel.onEditTargetConfirmed(it) })
    }

    @Composable
    private fun EditImageTargetConfirmationDialog(dialogState: DialogState.EditImageTargetConfirmationDialog) {
        TargetConfirmationDialog(
            count = dialogState.count,
            onConfirmed = { viewModel.onEditImageTargetConfirmed(it) })
    }

    @Composable
    private fun TargetConfirmationDialog(
        count: Int,
        onConfirmed: (HostViewModel.Companion.EditTarget) -> Unit,
    ) {
        Dialog(onDismissRequest = { viewModel.onDialogDismissed() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 4.dp,
            ) {
                EditTargetConfirmationDialogContent(
                    count = count,
                    onSubmit = { target ->
                        val vmTarget = when (target) {
                            EditTarget.RECORD -> HostViewModel.Companion.EditTarget.RECORD
                            EditTarget.TEMPLATE -> HostViewModel.Companion.EditTarget.TEMPLATE
                        }
                        onConfirmed(vmTarget)
                    },
                    onCancel = { viewModel.onDialogDismissed() },
                )
            }
        }
    }

    @Composable
    private fun ImageDialog(image: Bitmap) {
        Dialog(onDismissRequest = { viewModel.onDialogDismissed() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 4.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier.size(
                        width = image.width.dp * 2,
                        height = image.height.dp * 2
                    )
                )
            }
        }
    }
}

