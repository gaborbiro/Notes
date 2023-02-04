package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.layout.fillMaxSize
import dev.gaborbiro.notes.features.widget.views.NotesWidgetContent

class NotesWidget : GlanceAppWidget() {

    companion object {
        const val PREFS_ENTRIES = "entries"
    }

    override val stateDefinition = NotesWidgetPreferences

    @Composable
    override fun Content() {
        NotesWidgetContent(
            modifier = GlanceModifier
                .fillMaxSize(),
            navigator = NotesWidgetNavigatorImpl()
        )
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        NotesWidgetsUpdater.cleanup(context)
    }
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

class NotesWidgetReceiverSmall : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

