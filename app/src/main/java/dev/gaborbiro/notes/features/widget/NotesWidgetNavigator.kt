package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.features.host.HostActivity
import dev.gaborbiro.notes.util.showActionNotification

interface NotesWidgetNavigator {

    fun getLaunchNewNoteViaCameraAction(): Action

    fun getLaunchNewNoteViaImagePickerAction(): Action

    fun getLaunchNewNoteViaTextOnlyAction(): Action

    fun getDuplicateRecordAction(recordId: Long): Action
}

class NotesWidgetNavigatorImpl : NotesWidgetNavigator {

    override fun getLaunchNewNoteViaCameraAction(): Action {
        return actionRunCallback<AddNoteWithCameraAction>()
    }

    override fun getLaunchNewNoteViaImagePickerAction(): Action {
        return actionRunCallback<AddNoteWithImageAction>()
    }

    override fun getLaunchNewNoteViaTextOnlyAction(): Action {
        return actionRunCallback<AddNoteAction>()
    }

    override fun getDuplicateRecordAction(recordId: Long): Action {
        return actionRunCallback<DuplicateNoteAction>(
            actionParametersOf(
                ActionParameters.Key<Long>(PREFS_KEY_RECORD) to recordId
            )
        )
    }
}

class AddNoteWithCameraAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HostActivity.launchAddNoteWithCamera(context)
    }
}

class AddNoteWithImageAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HostActivity.launchAddNoteWithImage(context)
    }
}

class AddNoteAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        HostActivity.launchAddNote(context)
    }
}

class DuplicateNoteAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val recordId = parameters[ActionParameters.Key<Long>(PREFS_KEY_RECORD)]!!
        val newRecordId = RecordsRepository.get().duplicateRecord(recordId, "")
        NotesWidgetsUpdater.oneOffUpdate(context)
//        context.showActionNotification(
//            title = "Undo - duplicate record",
//            action = "Undo",
//            actionIcon = R.drawable.ic_undo,
//            actionIntent = HostActivity.getDeleteRecordIntent(context, newRecordId)
//        )
    }
}

private const val PREFS_KEY_RECORD = "record"