package dev.gaborbiro.notes.data.records

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Transaction
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.records.RecordsDAO
import dev.gaborbiro.notes.store.db.records.TemplatesDAO

class RecordsRepositoryImpl(
    private val templatesDAO: TemplatesDAO,
    private val recordsDAO: RecordsDAO,
    private val mapper: RecordsMapper,
) : RecordsRepository {

    override suspend fun getRecords(): List<Record> {
        return mapper.map(recordsDAO.get())
    }

    override fun getRecordsLiveData(): LiveData<List<Record>> {
        return Transformations.map(recordsDAO.getLiveData(), mapper::map)
    }

    override suspend fun saveTemplateAndRecord(
        record: ToSaveRecord,
        template: ToSaveTemplate
    ): Long {
        val templateId = templatesDAO.insertOrUpdate(mapper.map(template))
        return recordsDAO.insert(mapper.map(record, templateId))
    }

    override suspend fun saveRecord(record: ToSaveRecord, templateId: Long): Long {
        return recordsDAO.insert(mapper.map(record, templateId))
    }

    override suspend fun duplicateRecord(recordId: Long, notes: String): Long {
        return getRecord(recordId)!!.let { record: Record ->
            recordsDAO.insert(mapper.map(record, notes))
        }
    }

    @Transaction
    override suspend fun getRecord(recordId: Long): Record? {
        return recordsDAO.get(recordId)?.let(mapper::map)
    }

    override suspend fun delete(recordId: Long): Boolean {
        return recordsDAO.delete(recordId) > 0
    }
}