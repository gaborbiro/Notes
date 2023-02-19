package dev.gaborbiro.notes.features.widget.views

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

val sectionTitleTextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    textAlign = TextAlign.Center,
    color = ColorProvider(Color.White)
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
