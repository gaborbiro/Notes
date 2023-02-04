package dev.gaborbiro.notes.features.widget.views

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.padding
import dev.gaborbiro.notes.R
import dev.gaborbiro.notes.features.widget.NotesWidgetNavigator

@Composable
fun WidgetButtonLayout(
    modifier: GlanceModifier,
    navigator: NotesWidgetNavigator,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WidgetButton(
            modifier = GlanceModifier
                .defaultWeight()
                .padding(vertical = 10.dp),
            iconResId = R.drawable.ic_add_photo,
            contentDescription = "New note via camera",
            tapAction = navigator.getLaunchNewNoteViaCameraAction(),
        )
        WidgetButton(
            modifier = GlanceModifier
                .defaultWeight()
                .padding(vertical = 10.dp),
            iconResId = R.drawable.ic_add_picture,
            contentDescription = "New note via existing image",
            tapAction = navigator.getLaunchNewNoteViaImagePickerAction(),
        )
        WidgetButton(
            modifier = GlanceModifier
                .defaultWeight()
                .padding(vertical = 10.dp),
            iconResId = R.drawable.ic_add,
            contentDescription = "New note",
            tapAction = navigator.getLaunchNewNoteViaTextOnlyAction(),
        )
    }
}

@Composable
private fun WidgetButton(
    modifier: GlanceModifier,
    @DrawableRes iconResId: Int,
    contentDescription: String,
    tapAction: Action,
) {
    Image(
        provider = ImageProvider(resId = iconResId),
        contentDescription = contentDescription,
        modifier = modifier.clickable(tapAction)
    )
}