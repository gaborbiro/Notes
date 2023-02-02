package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.glance.unit.ColorProvider
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.util.BitmapLoader

class NotesWidget : GlanceAppWidget() {

    companion object {
        const val PREFS_ENTRIES = "entries"
    }

    override val stateDefinition = NotesWidgetPreferences

    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val mapper = RecordsUIMapper(BitmapLoader(LocalContext.current))
        val records = NotesWidgetsUpdater.retrieveRecords(prefs)

        NotesWidgetContent(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    ColorProvider(Color.DarkGray)
                )
                .cornerRadius(16.dp),
            records = mapper.map(records)
        )
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        NotesWidgetsUpdater.cleanup(context)
    }
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

