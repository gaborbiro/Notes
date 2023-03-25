package dev.gaborbiro.notes.features.widget.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.wrapContentHeight
import androidx.glance.unit.ColorProvider
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.features.common.model.TemplateUIModel
import dev.gaborbiro.notes.features.widget.NotesWidgetNavigator

@Composable
fun NotesWidgetContent(
    modifier: GlanceModifier,
    navigator: NotesWidgetNavigator,
    recentRecords: List<RecordUIModel>,
    topTemplates: List<TemplateUIModel>,
) {
    Column(
        modifier = modifier
            .background(
                ColorProvider(Color.DarkGray)
            )
            .cornerRadius(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        RecordsList(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            recentRecords = recentRecords,
            topTemplates = topTemplates,
            recordTapActionProvider = { recordId -> navigator.getDuplicateRecordAction(recordId) },
            templateTapActionProvider = { templateId -> navigator.getApplyTemplateAction(templateId) },
        )
        WidgetButtonLayout(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .wrapContentHeight(),
            launchNoteViaCameraAction = { navigator.getLaunchNewNoteViaCameraAction() },
            launchNewNoteViaImagePickerActionProvider = { navigator.getLaunchNewNoteViaImagePickerAction() },
            launchNewNoteViaTextOnlyActionProvider = { navigator.getLaunchNewNoteViaTextOnlyAction() },
            reloadActionProvider = { navigator.getReloadAction() },
        )
    }
}