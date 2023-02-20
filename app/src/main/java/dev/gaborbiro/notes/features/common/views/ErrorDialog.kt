package dev.gaborbiro.notes.features.common.views

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.gaborbiro.notes.features.common.BaseViewModel
import dev.gaborbiro.notes.features.common.model.ErrorUIModel
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import dev.gaborbiro.notes.ui.theme.PaddingDouble

@Composable
fun ErrorDialog(viewModel: BaseViewModel, error: ErrorUIModel) {
    Dialog(onDismissRequest = { viewModel.onErrorDialogDismissRequested() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 4.dp,
        ) {
            Column(modifier = Modifier.padding(PaddingDefault)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = error.message,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(PaddingDouble))

                Button(
                    onClick = { viewModel.onErrorDialogDismissRequested() },
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
                    Text(text = "Ok")
                }
            }
        }
    }
}