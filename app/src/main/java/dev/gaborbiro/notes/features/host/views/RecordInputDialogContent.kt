package dev.gaborbiro.notes.features.host.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.gaborbiro.notes.ui.theme.PaddingDefault
import dev.gaborbiro.notes.ui.theme.PaddingDouble
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
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
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleMedium,
            )
            Icon(
                imageVector = Icons.Filled.Cancel,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                contentDescription = "cancel",
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        onCancel()
                    },
            )
        }

        Spacer(modifier = Modifier.height(PaddingDefault))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .focusRequester(focusRequester),
            isError = error.isNullOrBlank().not(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            placeholder = {
                Text(
                    text = "Title",
                    color = Color.Gray,
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

        LaunchedEffect(key1 = Unit) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
}