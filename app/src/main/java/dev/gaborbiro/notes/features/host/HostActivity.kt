package dev.gaborbiro.notes.features.host

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.design.NotesTheme
import dev.gaborbiro.notes.features.common.BaseErrorDialogActivity
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.features.host.usecase.CreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.EditRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateImageUseCase
import dev.gaborbiro.notes.features.host.usecase.EditTemplateUseCase
import dev.gaborbiro.notes.features.host.usecase.GetRecordImageUseCase
import dev.gaborbiro.notes.features.host.usecase.SaveImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateCreateRecordUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditImageUseCase
import dev.gaborbiro.notes.features.host.usecase.ValidateEditRecordUseCase
import dev.gaborbiro.notes.features.host.views.EditImageTargetConfirmationDialog
import dev.gaborbiro.notes.features.host.views.EditTargetConfirmationDialog
import dev.gaborbiro.notes.features.host.views.ImageDialog
import dev.gaborbiro.notes.features.host.views.InputDialog
import dev.gaborbiro.notes.features.widget.NotesWidget
import dev.gaborbiro.notes.imageFilename
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.store.file.FileStoreFactoryImpl


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

    private val fileStore = FileStoreFactoryImpl(this).getStore("public", keepFiles = true)

    private val viewModel by lazy {
        val repository = RecordsRepository.get(fileStore)
        val bitmapStore = BitmapStore(fileStore)
        HostViewModel(
            repository,
            CreateRecordUseCase(repository),
            EditRecordUseCase(repository),
            EditTemplateUseCase(repository),
            ValidateEditRecordUseCase(repository),
            ValidateCreateRecordUseCase(),
            SaveImageUseCase(this, bitmapStore),
            ValidateEditImageUseCase(repository),
            EditRecordImageUseCase(repository),
            EditTemplateImageUseCase(repository),
            GetRecordImageUseCase(repository, bitmapStore),
            bitmapStore,
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
                // nothing to do
            }
        }

        setContent {
            HandleErrors()
            val uiState: HostUIState by viewModel.uiState.collectAsStateWithLifecycle()

            if (uiState.showCamera) {
                val filename = imageFilename()
                val uri = fileStore.createFile(filename).toUri()

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture(),
                    onResult = {
                        viewModel.onPhotoTaken(filename)
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
            is DialogState.InputDialog -> InputDialog(
                dialogState = dialogState,
                onDialogDismissed = viewModel::onDialogDismissed,
                onRecordDetailsSubmitRequested = viewModel::onRecordDetailsSubmitRequested,
                onRecordDetailsUserTyping = viewModel::onRecordDetailsUserTyping,
            )

            is DialogState.EditTargetConfirmationDialog -> EditTargetConfirmationDialog(
                dialogState = dialogState,
                onEditTargetConfirmed = viewModel::onEditTargetConfirmed,
                onDialogDismissed = viewModel::onDialogDismissed,
            )

            is DialogState.EditImageTargetConfirmationDialog -> EditImageTargetConfirmationDialog(
                dialogState = dialogState,
                onDialogDismissed = viewModel::onDialogDismissed,
                onEditImageTargetConfirmed = viewModel::onEditImageTargetConfirmed,
            )

            is DialogState.ShowImageDialog -> ImageDialog(
                image = dialogState.bitmap,
                onDialogDismissed = viewModel::onDialogDismissed,
            )

            null -> {
                // no dialog is shown
            }
        }
    }
}
