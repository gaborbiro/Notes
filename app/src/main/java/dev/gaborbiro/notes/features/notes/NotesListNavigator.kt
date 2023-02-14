package dev.gaborbiro.notes.features.notes

import android.content.Context
import dev.gaborbiro.notes.features.host.HostActivity

interface NotesListNavigator {

    fun updateRecordPhoto(templateId: Long)

    fun editRecord(recordId: Long)
}

class NotesListNavigatorImpl(private val appContext: Context) : NotesListNavigator {

    override fun updateRecordPhoto(templateId: Long) {
        HostActivity.launchRedoImage(appContext, templateId)
    }

    override fun editRecord(recordId: Long) {
        HostActivity.launchEdit(appContext, recordId)
    }
}