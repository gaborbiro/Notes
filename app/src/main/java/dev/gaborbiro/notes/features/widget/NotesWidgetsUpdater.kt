package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.reflect.TypeToken
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.Template
import dev.gaborbiro.notes.util.gson
import java.time.LocalDateTime

class NotesWidgetsUpdater(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    companion object {

        private const val UNIQUE_WORK_NAME = "dev.gaborbiro.notes.widget_update"

        fun oneOffUpdate(context: Context) {
            WorkManager.getInstance(context)
                .enqueue(OneTimeWorkRequestBuilder<NotesWidgetsUpdater>().build())
        }

        suspend fun cleanup(context: Context) {
            val widgetCount = GlanceAppWidgetManager(context)
                .getGlanceIds(NotesWidget::class.java)
                .size
            if (widgetCount == 0) {
                WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            }
        }

        suspend fun sendToWidgets(
            context: Context,
            recentRecords: List<Record>,
            topTemplates: List<Template>
        ) {
            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(NotesWidget::class.java)
            val recordsJson = gson.toJson(recentRecords)
            val templatesJson = gson.toJson(topTemplates)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(NotesWidget.PREFS_RECENT_RECORDS)] = recordsJson
                    prefs[stringPreferencesKey(NotesWidget.PREFS_TOP_TEMPLATES)] = templatesJson
                }
            }
            NotesWidget().updateAll(context)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val repository = RecordsRepository.get()
            val recentRecords: List<Record> =
                repository.getRecords(LocalDateTime.now().minusDays(3))
            val topTemplates: List<Template> = repository.getTemplatesByFrequency().take(30)
            sendToWidgets(applicationContext, recentRecords, topTemplates)
            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure()
        }
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

//        suspend fun startUpdate(context: Context) {
//            val widgetCount =
//                GlanceAppWidgetManager(context).getGlanceIds(NotesWidget::class.java).size
//            println("NotesWidget startUpdate $widgetCount")
//            if (widgetCount > 0) {
//                val request = PeriodicWorkRequest
//                    .Builder(NotesWidgetsUpdater::class.java, 15, TimeUnit.MINUTES)
//                    .build()
//
//                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//                    UNIQUE_WORK_NAME,
//                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
//                    request,
//                )
//            }
//        }
