package dev.gaborbiro.notes.features.widget

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.util.gson

class NotesWidgetsUpdater(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    companion object {

        private const val UNIQUE_WORK_NAME = "widget_update"

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

        fun oneOffUpdate(context: Context) {
            println("NotesWidget oneOffUpdate")
            WorkManager.getInstance(context)
                .enqueue(OneTimeWorkRequestBuilder<NotesWidgetsUpdater>().build())
        }

        suspend fun cleanup(context: Context) {
            val widgetCount =
                GlanceAppWidgetManager(context).getGlanceIds(NotesWidget::class.java).size
            println("NotesWidget cleanup $widgetCount")
            if (widgetCount == 0) {
                WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
            }
        }
    }

    override suspend fun doWork(): Result {
        println("NotesWidget doWork")
        try {
            val glanceIds = GlanceAppWidgetManager(applicationContext)
                .getGlanceIds(NotesWidget::class.java)

            val records: List<Record> = RecordsRepository.get().getRecords()
            val recordsJson = gson.toJson(records)
            println("NotesWidget doWork ${glanceIds.size} widgets, ${records.size} records")
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[stringPreferencesKey(NotesWidget.PREFS_ENTRIES)] = recordsJson
                }
            }
            NotesWidget().updateAll(applicationContext)
            return Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            return Result.failure()
        }
    }
}