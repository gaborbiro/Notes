package dev.gaborbiro.notes.features.widget.views

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.currentState
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.widget.NotesWidgetNavigator
import dev.gaborbiro.notes.features.widget.retrieveRecords
import dev.gaborbiro.notes.util.BitmapLoader

@Composable
fun RecordsList(
    modifier: GlanceModifier,
    navigator: NotesWidgetNavigator,
) {
    val context = LocalContext.current
    val mapper = RecordsUIMapper(BitmapLoader(context))
    val prefs = currentState<Preferences>()
    val records = mapper.map(prefs.retrieveRecords())
    LazyColumn(
        modifier
    ) {
        items(records.size, itemId = { it.toLong() }) {
            val record = records[it]
            WidgetRecord(
                record = record,
                onWidgetTapAction = navigator.getDuplicateRecordAction(recordId = record.recordId)
            )
        }
    }
}