package dev.gaborbiro.notes.data.records

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.Transaction
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.Template
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.records.RecordsDAO
import dev.gaborbiro.notes.store.db.records.TemplatesDAO
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel
import dev.gaborbiro.notes.store.file.DocumentDeleter

class RecordsRepositoryImpl(
    private val templatesDAO: TemplatesDAO,
    private val recordsDAO: RecordsDAO,
    private val mapper: DBMapper,
    private val documentDeleter: DocumentDeleter,
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
        val deleted = recordsDAO.delete(recordId) > 0
        val allRecords = recordsDAO.get()
        val allTemplates = templatesDAO.get()
        val unusedTemplates =
            allTemplates.filter { t -> allRecords.none { it.template.id == t.id } }
        unusedTemplates.forEach {
            templatesDAO.delete(it)
            it.image?.let { documentDeleter.delete(it) }
        }
        return deleted
    }

    override suspend fun updateTemplatePhoto(templateId: Long, uri: Uri?) {
        templatesDAO.get(templateId)?.let { oldTemplate ->
            templatesDAO.insertOrUpdate(
                TemplateDBModel(
                    image = uri,
                    name = oldTemplate.name,
                    description = oldTemplate.description,
                ).also {
                    it.id = templateId
                }
            )
            if (oldTemplate.image != null) {
                documentDeleter.delete(oldTemplate.image)
            }
        }
    }

    override suspend fun getTemplatesByName(name: String): List<Template> {
        return templatesDAO.get(name).map(mapper::map)
    }
}