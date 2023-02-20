package dev.gaborbiro.notes.features.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.notes.features.common.model.ErrorUIModel
import ellipsize
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _errorState: MutableStateFlow<ErrorUIModel?> = MutableStateFlow(null)
    val errorState: StateFlow<ErrorUIModel?> = _errorState.asStateFlow()


    protected fun runSafely(task: suspend () -> Unit) {
        viewModelScope.launch(deleteExceptionHandler) {
            task()
        }
    }

    private val deleteExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _errorState.update {
            ErrorUIModel(
                "Oops. Something went wrong ${
                    exception.message?.let {
                        "\n\n(${
                            it.ellipsize(
                                300
                            )
                        })"
                    } ?: ""
                }")
        }
        Log.w("BaseViewModel", "Uncaught exception", exception)
    }

    fun onErrorDialogDismissRequested() {
        _errorState.update {
            null
        }
    }
}