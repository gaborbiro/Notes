package dev.gaborbiro.notes.features.common.model

import android.graphics.Bitmap

class RecordUIModel(
    val id: Long,
    val bitmap: Bitmap?,
    val timestamp: String,
    val title: String,
)