package dev.gaborbiro.notes.store.file

import android.content.Context
import android.net.Uri

class DocumentDeleter(private val appContext: Context) {

    fun delete(uri: Uri): Boolean {
        return appContext.contentResolver.delete(uri, null) > 0
    }
}