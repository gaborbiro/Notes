package dev.gaborbiro.notes.features.host.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.gaborbiro.notes.features.host.DialogState
import dev.gaborbiro.notes.features.host.HostViewModel

@Composable
fun InputDialog(
    dialogState: DialogState.InputDialog,
    onDialogDismissed: () -> Unit,
    onRecordDetailsSubmitRequested: (String, String) -> Unit,
    onRecordDetailsUserTyping: (String, String) -> Unit,
) {
    Dialog(
        onDismissRequest = {
            onDialogDismissed()
        },
    ) {
//            val image = (dialogState as? DialogState.InputDialogState.Edit)?.image
        val title = (dialogState as? DialogState.InputDialog.Edit)?.title
        val description = (dialogState as? DialogState.InputDialog.Edit)?.description
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp,
            modifier = Modifier.Companion
                .wrapContentHeight()
        ) {
            NoteInputDialogContent(
                onCancel = {
                    onDialogDismissed()
                },
                onSubmit = { title, description ->
                    onRecordDetailsSubmitRequested(title, description)
                },
                onChange = { title, description ->
                    onRecordDetailsUserTyping(title, description)
                },
                title = title,
                description = description,
                error = dialogState.validationError,
            )
        }
    }
}

@Composable
fun EditTargetConfirmationDialog(
    dialogState: DialogState.EditTargetConfirmationDialog,
    onEditTargetConfirmed: (HostViewModel.Companion.EditTarget) -> Unit,
    onDialogDismissed: () -> Unit,
) {
    TargetConfirmationDialog(
        count = dialogState.count,
        onConfirmed = onEditTargetConfirmed,
        onDialogDismissed = onDialogDismissed,
    )
}

@Composable
fun EditImageTargetConfirmationDialog(
    dialogState: DialogState.EditImageTargetConfirmationDialog,
    onEditImageTargetConfirmed: (HostViewModel.Companion.EditTarget) -> Unit,
    onDialogDismissed: () -> Unit,
) {
    TargetConfirmationDialog(
        count = dialogState.count,
        onConfirmed = onEditImageTargetConfirmed,
        onDialogDismissed = onDialogDismissed,
    )
}

@Composable
fun TargetConfirmationDialog(
    count: Int,
    onConfirmed: (HostViewModel.Companion.EditTarget) -> Unit,
    onDialogDismissed: () -> Unit,
) {
    Dialog(onDismissRequest = onDialogDismissed) {
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
                onCancel = onDialogDismissed,
            )
        }
    }
}

@Composable
fun ImageDialog(
    image: Bitmap,
    onDialogDismissed: () -> Unit,
) {
    Dialog(onDismissRequest = onDialogDismissed) {
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
