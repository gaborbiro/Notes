package dev.gaborbiro.notes.features.common.model

import android.graphics.Bitmap
import android.net.Uri

class RecordUIModel(
    val recordId: Long,
    val templateId: Long,
    val image: Uri?,
    val bitmap: Bitmap?,
    val timestamp: String,
    val title: String,
)