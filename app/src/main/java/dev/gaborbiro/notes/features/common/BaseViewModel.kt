package dev.gaborbiro.notes.features.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gaborbiro.notes.features.common.model.ErrorUIModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    private val _errorState: MutableStateFlow<ErrorUIModel?> = MutableStateFlow(null)
    val errorState: StateFlow<ErrorUIModel?> = _errorState.asStateFlow()

    fun runOnBackgroundThread(task: suspend () -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            launch(Dispatchers.IO) { task() }
        }
    }


    protected fun runSafely(task: suspend () -> Unit) {
        viewModelScope.launch(coroutineExceptionHandler) {
            task()
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _errorState.update {
            ErrorUIModel("Oops. Something went wrong")
        }
        Log.w("BaseViewModel", "Uncaught exception", exception)
    }

    fun onErrorDialogDismissRequested() {
        _errorState.update {
            null
        }
    }
}