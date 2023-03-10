package dev.gaborbiro.notes.data.records

import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.Template
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.records.model.RecordAndTemplateDBModel
import dev.gaborbiro.notes.store.db.records.model.RecordDBModel
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel
import java.time.LocalDateTime

interface DBMapper {

    companion object {
        fun get(): DBMapper = DBMapperImpl()
    }

    fun map(template: TemplateDBModel): Template

    fun map(records: List<RecordAndTemplateDBModel>): List<Record>

    fun map(record: RecordAndTemplateDBModel): Record

    fun map(record: ToSaveRecord, templateId: Long): RecordDBModel

    fun map(template: ToSaveTemplate): TemplateDBModel

    fun map(record: Record, dateTime: LocalDateTime? = null): RecordDBModel
}