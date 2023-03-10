package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.layout.fillMaxSize
import dev.gaborbiro.notes.features.widget.views.NotesWidgetContent
import dev.gaborbiro.notes.store.bitmap.BitmapStore

class NotesWidget : GlanceAppWidget() {

    companion object {
        const val PREFS_RECENT_RECORDS = "recent_records"
        const val PREFS_TOP_TEMPLATES = "top_templates"
    }

    override val stateDefinition = NotesWidgetPreferences

    @Composable
    override fun Content() {
        NotesWidgetContent(
            modifier = GlanceModifier
                .fillMaxSize(),
            navigator = NotesWidgetNavigatorImpl(),
            bitmapStore = BitmapStore(LocalContext.current)
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

