package dev.gaborbiro.notes.features.widget.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.Text
import dev.gaborbiro.notes.ui.theme.PaddingDefaultWidget

@Composable
fun SectionTitle(onClick: () -> Unit) {
    Box(
        modifier = GlanceModifier
            .padding(PaddingDefaultWidget)
            .clickable(onClick),
    ) {
        Text(
            text = "Top Templates ",
            modifier = GlanceModifier
                .background(sectionTitleBackground)
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(4.dp)
                .cornerRadius(4.dp),
            style = sectionTitleTextStyle,
        )
    }
}