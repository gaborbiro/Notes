package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.work.WorkManager
import com.google.gson.reflect.TypeToken
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.Template
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.TemplatesUIMapper
import dev.gaborbiro.notes.features.widget.views.NotesWidgetContent
import dev.gaborbiro.notes.features.widget.views.WidgetImageSize
import dev.gaborbiro.notes.features.widget.workers.ReloadWorkRequest
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.util.gson
import dev.gaborbiro.notes.util.px

class NotesWidget : GlanceAppWidget() {

    companion object {
        const val PREFS_RECENT_RECORDS = "recent_records"
        const val PREFS_TOP_TEMPLATES = "top_templates"

        fun reload(context: Context) {
            WorkManager.getInstance(context).enqueue(
                ReloadWorkRequest.getWorkRequest(
                    recentRecordsPrefsKey = PREFS_RECENT_RECORDS,
                    topTemplatesPrefsKey = PREFS_TOP_TEMPLATES,
                    recordDaysToDisplay = 7,
                    templateCount = 30,
                )
            )
        }

        suspend fun cleanup(context: Context) {
            val widgetCount = GlanceAppWidgetManager(context)
                .getGlanceIds(NotesWidget::class.java)
                .size
            if (widgetCount == 0) {
                WorkManager.getInstance(context).cancelAllWork()
            }
        }
    }

    override val stateDefinition = NotesWidgetPreferences

    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val bitmapStore = BitmapStore(LocalContext.current)
        val recordsUIMapper = RecordsUIMapper(bitmapStore)
        val recentRecords = recordsUIMapper.map(
            records = prefs.retrieveRecentRecords(),
            maxImageSizePx = 60.dp.px().toInt()
        )
        val templatesUIMapper = TemplatesUIMapper(bitmapStore)
        val topTemplates = templatesUIMapper.map(
            records = prefs.retrieveTopTemplates(),
            maxImageSizePx = WidgetImageSize.px().toInt()
        )

        NotesWidgetContent(
            modifier = GlanceModifier
                .fillMaxSize(),
            navigator = NotesWidgetNavigatorImpl(),
            recentRecords,
            topTemplates,
        )
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        cleanup(context)
    }
}

fun Preferences.retrieveRecentRecords(): List<Record> {
    val recordsJSON = this[stringPreferencesKey(NotesWidget.PREFS_RECENT_RECORDS)]
    return recordsJSON
        ?.let {
            val itemType = object : TypeToken<List<Record>>() {}.type
            gson.fromJson(recordsJSON, itemType)
        }
        ?: emptyList()
}

fun Preferences.retrieveTopTemplates(): List<Template> {
    val recordsJSON = this[stringPreferencesKey(NotesWidget.PREFS_TOP_TEMPLATES)]
    return recordsJSON
        ?.let {
            val itemType = object : TypeToken<List<Template>>() {}.type
            gson.fromJson(recordsJSON, itemType)
        }
        ?: emptyList()
}

class NotesWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

class NotesWidgetReceiverSmall : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NotesWidget()
}

