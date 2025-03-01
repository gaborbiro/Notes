package dev.gaborbiro.notes.features.widget.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

val WidgetImageSize = 68.dp

val sectionTitleBackground
    @Composable get() = GlanceTheme.colors.tertiaryContainer

val sectionTitleTextStyle: TextStyle
    @Composable get() = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = GlanceTheme.colors.onTertiaryContainer
    )

val titleTextStyle: TextStyle
    @Composable get() = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        textAlign = TextAlign.Start,
        color = GlanceTheme.colors.onBackground,
    )

val dateTextStyle: TextStyle
    @Composable get() = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        textAlign = TextAlign.Start,
        color = GlanceTheme.colors.onBackground,
    )
