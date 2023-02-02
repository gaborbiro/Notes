package dev.gaborbiro.notes.data.records

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Transaction
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.records.RecordsDAO
import dev.gaborbiro.notes.store.db.records.TemplatesDAO
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel

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

    override suspend fun saveRecord(
        record: ToSaveRecord,
    ): Long {
        return recordsDAO.insert(mapper.map(record))
    }

    override suspend fun saveTemplate(
        template: ToSaveTemplate
    ): Long {
        return templatesDAO.insertOrUpdate(mapper.map(template))
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

    override suspend fun updateTemplatePhoto(templateId: Long, uri: Uri): Boolean {
        return templatesDAO.get(templateId)?.let { oldTemplate ->
            templatesDAO.insertOrUpdate(
                TemplateDBModel(
                    image = uri,
                    name = oldTemplate.name,
                    description = oldTemplate.description,
                ).also {
                    it.id = templateId
                }
            ) >= 0
        } == true
    }
}