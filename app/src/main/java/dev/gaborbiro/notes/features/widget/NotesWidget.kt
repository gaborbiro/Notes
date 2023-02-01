package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.google.gson.reflect.TypeToken
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.gson

class NotesWidget : GlanceAppWidget() {

    companion object {
        const val PREFS_ENTRIES = "entries"
    }

    override val stateDefinition = NotesWidgetPreferences

    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val context = LocalContext.current
        val state = remember { mutableStateOf<List<RecordUIModel>>(emptyList()) }
        val mapper = RecordsUIMapper(BitmapLoader(context))

        val recordsJSON = prefs[stringPreferencesKey(PREFS_ENTRIES)]
        val records: List<Record> = recordsJSON
            ?.let {
                val itemType = object : TypeToken<List<Record>>() {}.type
                gson.fromJson(recordsJSON, itemType)
            }
            ?: emptyList()
        state.value = mapper.map(records)

        NotesWidgetContent(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    ColorProvider(Color.DarkGray)
                )
                .appWidgetBackground()
                .cornerRadius(16.dp),
            records = state.value
        )

    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        NotesWidgetsUpdater.cleanup(context)
    }
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

