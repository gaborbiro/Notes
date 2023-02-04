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
import dev.gaborbiro.notes.util.gson

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

        suspend fun sendToWidgets(context: Context, records: List<Record>) {
            val glanceIds = GlanceAppWidgetManager(context)
                .getGlanceIds(NotesWidget::class.java)
            val recordsJson = gson.toJson(records)
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[stringPreferencesKey(NotesWidget.PREFS_ENTRIES)] = recordsJson
                }
            }
            NotesWidget().updateAll(context)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val records: List<Record> = RecordsRepository.get().getRecords()
            sendToWidgets(applicationContext, records)
            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure()
        }
    }
}

fun Preferences.retrieveRecords(): List<Record> {
    val recordsJSON = this[stringPreferencesKey(NotesWidget.PREFS_ENTRIES)]
    return recordsJSON
        ?.let {
            val itemType = object : TypeToken<List<Record>>() {}.type
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
