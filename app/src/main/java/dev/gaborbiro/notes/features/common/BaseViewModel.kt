package dev.gaborbiro.notes.features.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

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
        Log.w("HostViewModel", "Uncaught exception", exception)
    }
}