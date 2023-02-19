package dev.gaborbiro.notes.features.widget.views

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.Text
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.ui.theme.PaddingDefaultWidget

@Composable
fun SectionTitle() {
    Text(
        text = "Top templates",
        modifier = GlanceModifier
            .background(R.color.list_divider_background)
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(PaddingDefaultWidget),
        style = sectionTitleTextStyle,
    )
}