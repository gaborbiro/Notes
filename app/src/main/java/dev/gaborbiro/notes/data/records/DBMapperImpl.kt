package dev.gaborbiro.notes.data.records

import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.Template
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.records.model.RecordAndTemplateDBModel
import dev.gaborbiro.notes.store.db.records.model.RecordDBModel
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel
import java.time.LocalDateTime

class DBMapperImpl : DBMapper {

    override fun map(template: TemplateDBModel): Template {
        return Template(
            id = template.id!!,
            image = template.image,
            name = template.name,
            description = template.description,
        )
    }

    override fun map(records: List<RecordAndTemplateDBModel>): List<Record> {
        return records.map(::map)
    }

    override fun map(record: RecordAndTemplateDBModel): Record {
        return Record(
            id = record.record.id!!,
            timestamp = record.record.timestamp,
            template = map(record.template),
            notes = record.record.notes,
        )
    }

    override fun map(record: ToSaveRecord): RecordDBModel {
        return RecordDBModel(
            timestamp = record.timestamp,
            templateId = record.templateId,
            notes = record.notes,
        )
    }

    override fun map(template: ToSaveTemplate): TemplateDBModel {
        return TemplateDBModel(
            image = template.image,
            name = template.name,
            description = template.description,
        )
    }

    override fun map(record: Record, notes: String): RecordDBModel {
        return map(
            ToSaveRecord(
                timestamp = LocalDateTime.now(),
                templateId = record.template.id,
                notes = notes
            )
        )
    }
}