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
import dev.gaborbiro.notes.features.widget.NotesWidgetNavigator

@Composable
fun NotesWidgetContent(
    modifier: GlanceModifier,
    navigator: NotesWidgetNavigator,
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
            navigator
        )
        WidgetButtonLayout(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .wrapContentHeight(),
            navigator
        )
    }
}
