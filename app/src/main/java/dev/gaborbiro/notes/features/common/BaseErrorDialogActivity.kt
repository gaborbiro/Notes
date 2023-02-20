package dev.gaborbiro.notes.features.common

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gaborbiro.notes.features.common.views.ErrorDialog

abstract class BaseErrorDialogActivity : AppCompatActivity() {

    protected abstract fun baseViewModel(): BaseViewModel

    @Composable
    protected fun HandleErrors() {
        val error = baseViewModel().errorState.collectAsStateWithLifecycle()

        error.value?.let {
            ErrorDialog(viewModel = baseViewModel(), error = it)
        }
    }
}