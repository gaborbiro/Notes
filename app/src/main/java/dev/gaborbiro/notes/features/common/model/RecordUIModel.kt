package dev.gaborbiro.notes.features.common.model

import android.graphics.Bitmap

class RecordUIModel(
    val recordId: Long,
    val templateId: Long,
    val bitmap: Bitmap?,
    val timestamp: String,
    val title: String,
) {
    override fun toString(): String {
        return "RecordUIModel(recordId=$recordId, templateId=$templateId, bitmap=${bitmap?.byteCount ?: 0} bytes, timestamp='$timestamp', title='$title')"
    }
}