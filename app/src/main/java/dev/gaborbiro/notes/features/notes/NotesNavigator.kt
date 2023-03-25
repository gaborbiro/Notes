package dev.gaborbiro.notes.features.notes

import android.content.Context
import dev.gaborbiro.notes.features.host.HostActivity

interface NotesListNavigator {

    fun updateRecordPhoto(recordId: Long)

    fun editRecord(recordId: Long)

    fun viewImage(recordId: Long)
}

class NotesListNavigatorImpl(private val appContext: Context) : NotesListNavigator {

    override fun updateRecordPhoto(recordId: Long) {
        HostActivity.launchRedoImage(appContext, recordId)
    }

    override fun editRecord(recordId: Long) {
        HostActivity.launchEdit(appContext, recordId)
    }

    override fun viewImage(recordId: Long) {
        HostActivity.launchShowImage(appContext, recordId)
    }
}