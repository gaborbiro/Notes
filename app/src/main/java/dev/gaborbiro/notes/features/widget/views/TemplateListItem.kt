package dev.gaborbiro.notes.features.widget.views

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import dev.gaborbiro.notes.features.common.model.TemplateUIModel
import dev.gaborbiro.notes.ui.theme.PaddingDefaultWidget
import dev.gaborbiro.notes.ui.theme.PaddingHalfWidget

@Composable
fun WidgetTemplateListItem(
    template: TemplateUIModel,
    onWidgetTapAction: Action,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = PaddingHalfWidget)
            .clickable(onWidgetTapAction)
    ) {
        template.bitmap
            ?.let { image: Bitmap ->
                Image(
                    provider = ImageProvider(image),
                    contentDescription = "note image",
                    modifier = GlanceModifier
                        .size(60.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            ?: run {
                Spacer(modifier = GlanceModifier.size(60.dp))
            }
        Text(
            text = template.title,
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .padding(horizontal = PaddingDefaultWidget),
            maxLines = 2,
            style = titleTextStyle
        )
    }
}