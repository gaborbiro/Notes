package dev.gaborbiro.notes.features.host.views

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.gaborbiro.notes.features.host.DialogState
import dev.gaborbiro.notes.features.host.HostViewModel

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
