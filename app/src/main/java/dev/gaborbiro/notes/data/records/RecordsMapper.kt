package dev.gaborbiro.notes.data.records

import android.net.Uri
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.store.db.records.model.RecordAndTemplateDBModel
import dev.gaborbiro.notes.store.db.records.model.RecordDBModel
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel

interface RecordsMapper {

    companion object {
        fun get(): RecordsMapper = RecordsMapperImpl()
    }

    fun map(record: RecordAndTemplateDBModel): Record

    fun map(record: ToSaveRecord, templateId: Long): RecordDBModel

    fun map(template: ToSaveTemplate): TemplateDBModel

    fun map(record: Record, notes: String): RecordDBModel
}