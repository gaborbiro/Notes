package dev.gaborbiro.notes.features.common

import android.graphics.Bitmap
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.features.common.model.RecordUIModel
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import dev.gaborbiro.notes.util.formatShort
import dev.gaborbiro.notes.util.formatShortTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class RecordsUIMapper(
    private val bitmapStore: BitmapStore,
) {

    fun map(records: List<Record>, maxImageSizePx: Int? = null): List<RecordUIModel> {
        return records.map {
            map(it, maxImageSizePx)
        }
    }

    private fun map(record: Record, maxImageSizePx: Int? = null): RecordUIModel {
        var bitmap: Bitmap? = null
        bitmap = record.template.image?.let { bitmapStore.loadBitmap(it, maxImageSizePx) }
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
            bitmap = bitmap,
            timestamp = timestampStr,
            title = record.template.name
        )
    }
}