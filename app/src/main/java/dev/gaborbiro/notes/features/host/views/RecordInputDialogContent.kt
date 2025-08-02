package dev.gaborbiro.notes.features.host.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.gaborbiro.notes.design.PaddingDefault
import dev.gaborbiro.notes.design.PaddingDouble
import kotlinx.coroutines.delay

@Composable
fun NoteInputDialogContent(
    onCancel: () -> Unit,
    onSubmit: (String, String) -> Unit,
    onChange: (String, String) -> Unit,
    title: String? = null,
    description: String? = null,
    error: String?,
) {
    val focusRequester = remember { FocusRequester() }

    var titleState by remember {
        mutableStateOf(title ?: "")
    }
    var descriptionState by remember {
        mutableStateOf(description ?: "")
    }

    val onDone: () -> Unit = {
        onSubmit(titleState.trim(), descriptionState.trim())
    }

    Column(
        modifier = Modifier.padding(PaddingDefault),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add a note",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.height(PaddingDefault))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .focusRequester(focusRequester),
            isError = error.isNullOrBlank().not(),
            textStyle = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            placeholder = {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            value = titleState,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            onValueChange = {
                titleState = it
                onChange(titleState, descriptionState)
            },
        )
        if (error.isNullOrBlank().not()) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .height(24.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.height(PaddingDefault))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            value = descriptionState,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Sentences,
            ),
            onValueChange = {
                descriptionState = it
                onChange(titleState, descriptionState)
            },
        )

        Spacer(modifier = Modifier.height(PaddingDouble))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onCancel) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = PaddingDefault),
                    color = MaterialTheme.colorScheme.primary,
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            TextButton(onDone) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = PaddingDefault),
                    color = MaterialTheme.colorScheme.primary,
                    text = "Save",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        LaunchedEffect(key1 = Unit) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
}
