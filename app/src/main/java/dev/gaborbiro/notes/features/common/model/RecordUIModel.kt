package dev.gaborbiro.notes.features.common.model

import android.graphics.Bitmap

class RecordUIModel(
    val recordId: Long,
    val templateId: Long,
    val bitmap: Bitmap?,
    val timestamp: String,
    val title: String,
)