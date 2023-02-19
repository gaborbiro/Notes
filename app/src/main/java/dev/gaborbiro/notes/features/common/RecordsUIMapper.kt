package dev.gaborbiro.notes.features.common

import android.graphics.Bitmap
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.util.BitmapLoader
import dev.gaborbiro.notes.util.formatShort
import dev.gaborbiro.notes.util.formatShortTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RecordsUIMapper(
    private val bitmapLoader: BitmapLoader,
) {

    fun map(records: List<Record>): List<RecordUIModel> {
        return records.map(::map)
    }

    fun map(record: Record): RecordUIModel {
        val bitmap: Bitmap? = record.template.image?.let { bitmapLoader.loadBitmap(it) }
        val timestamp = record.timestamp
        val timestampStr = when {
            !timestamp.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)) -> {
                "today at ${timestamp.formatShortTime()}"
            }

            !timestamp.isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(1)) -> {
                "yesterday at ${timestamp.formatShortTime()}"
            }

            else -> timestamp.formatShort()
        }
        return RecordUIModel(
            recordId = record.id,
            templateId = record.template.id,
            image = record.template.image,
            bitmap = bitmap,
            timestamp = timestampStr,
            title = record.template.name
        )
    }
}