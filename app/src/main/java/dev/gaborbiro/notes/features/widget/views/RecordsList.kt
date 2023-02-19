package dev.gaborbiro.notes.features.widget.views

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.currentState
import dev.gaborbiro.notes.features.common.RecordsUIMapper
import dev.gaborbiro.notes.features.common.TemplatesUIMapper
import dev.gaborbiro.notes.features.widget.NotesWidgetNavigator
import dev.gaborbiro.notes.features.widget.retrieveRecentRecords
import dev.gaborbiro.notes.features.widget.retrieveTopTemplates
import dev.gaborbiro.notes.util.BitmapLoader

@Composable
fun RecordsList(
    modifier: GlanceModifier,
    navigator: NotesWidgetNavigator,
) {
    val context = LocalContext.current
    val bitmapLoader = BitmapLoader(context)
    val recordsUIMapper = RecordsUIMapper(bitmapLoader)
    val templatesUIMapper = TemplatesUIMapper(bitmapLoader)
    val prefs = currentState<Preferences>()
    val recentRecords = recordsUIMapper.map(prefs.retrieveRecentRecords())
    val topTemplates = templatesUIMapper.map(prefs.retrieveTopTemplates())
    LazyColumn(
        modifier,
    ) {
        items(
            count = recentRecords.size + topTemplates.size + 1,
            itemId = {
                val index = mapListIndex(recentRecords.size, it)
                when {
                    it < recentRecords.size -> {
                        -recentRecords[index].recordId
                    }

                    it == recentRecords.size -> {
                        0
                    }

                    else -> {
                        topTemplates[index].templateId
                    }
                }
            }
        ) {
            val index = mapListIndex(recentRecords.size, it)
            when {
                it < recentRecords.size -> {
                    val record = recentRecords[index]
                    WidgetRecordListItem(
                        record = record,
                        onWidgetTapAction = navigator.getDuplicateRecordAction(recordId = record.recordId),
                    )
                }

                it == recentRecords.size -> {
                    SectionTitle()
                }

                else -> {
                    val template = topTemplates[index]
                    WidgetTemplateListItem(
                        template = template,
                        onWidgetTapAction = navigator.getApplyTemplateAction(templateId = template.templateId)
                    )
                }
            }
        }
    }
}

private fun mapListIndex(recentRecordsSize: Int, index: Int) = when {
    index < recentRecordsSize -> index
    index == recentRecordsSize -> recentRecordsSize
    else -> index - 1 - recentRecordsSize
}