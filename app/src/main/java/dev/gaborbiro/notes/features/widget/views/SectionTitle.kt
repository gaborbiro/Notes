package dev.gaborbiro.notes.features.widget.views

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.Text
import dev.gaborbiro.notes.ui.theme.PaddingDefaultWidget
import dev.gaborbiro.notes.ui.theme.PaddingDoubleWidget

@Composable
fun SectionTitle(title: String, @DrawableRes trailingImage: Int? = null, onClick: () -> Unit) {
    Box(
        modifier = GlanceModifier
            .padding(horizontal = PaddingDefaultWidget)
            .clickable(onClick),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = title,
            modifier = GlanceModifier
                .background(sectionTitleBackground)
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(PaddingDoubleWidget)
                .cornerRadius(4.dp),
            style = sectionTitleTextStyle,
        )
        trailingImage?.let {
            Image(
                modifier = GlanceModifier
                    .padding(end = PaddingDefaultWidget),
                provider = ImageProvider(trailingImage),
                contentDescription = "",
            )
        }
    }
}