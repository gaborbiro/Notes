package dev.gaborbiro.notes.features.common

import android.graphics.Bitmap
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.formatShort

class RecordsUIMapper(
    private val bitmapLoader: BitmapLoader,
) {

    fun map(records: List<Record>): List<RecordUIModel> {
        return records.map {
            map(it)
        }
    }

    private fun map(record: Record): RecordUIModel {
        val bitmap: Bitmap? = record.template.image?.let { bitmapLoader.uriToBitmap(it) }
        return RecordUIModel(
            id = record.id,
            bitmap = bitmap,
            timestamp = record.timestamp.formatShort(),
            title = record.template.name
        )
    }
}