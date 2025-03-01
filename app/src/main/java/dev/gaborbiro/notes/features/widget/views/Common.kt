package dev.gaborbiro.notes.features.widget.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val WidgetImageSize = 60.dp

val sectionTitleBackground: Color
    @Composable get() = Color(MaterialTheme.colorScheme.surface.toArgb())

val sectionTitleTextStyle: TextStyle
    @Composable get() = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = TextDefaults.defaultTextColor
    )

val titleTextStyle = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    textAlign = TextAlign.Start,
    color = ColorProvider(Color.White)
)

val dateTextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    textAlign = TextAlign.Start,
    color = ColorProvider(Color.LightGray)
)
