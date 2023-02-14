package dev.gaborbiro.notes.data.records

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Transaction
import dev.gaborbiro.notes.data.records.domain.RecordsRepository
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.store.db.records.RecordsDAO
import dev.gaborbiro.notes.store.db.records.TemplatesDAO
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel
import dev.gaborbiro.notes.store.file.DocumentDeleter
import dev.gaborbiro.notes.store.file.DocumentWriter
import dev.gaborbiro.notes.util.BitmapLoader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class RecordsRepositoryImpl(
    private val templatesDAO: TemplatesDAO,
    private val recordsDAO: RecordsDAO,
    private val mapper: DBMapper,
    private val documentDeleter: DocumentDeleter,
//    private val bitmapLoader: BitmapLoader,
//    private val documentWriter: DocumentWriter,
) : RecordsRepository {

    override suspend fun getRecords(): List<Record> {
//        templatesDAO.get().forEach { template ->
//            if (template.image?.toString()?.contains("cache") == true) {
//                val bitmap = bitmapLoader.loadBitmap(template.image)!!
//                val uri = ByteArrayOutputStream().let { stream ->
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
//                    val inStream = ByteArrayInputStream(stream.toByteArray())
//                    documentWriter.write(inStream, template.image.lastPathSegment!!)
//                }
//                val id = templatesDAO.insertOrUpdate(
//                    TemplateDBModel(
//                        id = template.id!!,
//                        image = uri,
//                        name = template.name,
//                        description = template.description
//                    )
//                )
//                println(id)
//            }
//        }
        return mapper.map(recordsDAO.get())
    }

    override suspend fun getRecords(templateId: Long): List<Record> {
        return mapper.map(recordsDAO.getByTemplate(templateId))
    }

    override fun getRecordsFlow(): Flow<List<Record>> {
        return recordsDAO.getLiveData()
            .distinctUntilChanged()
            .map(mapper::map)
    }

    override suspend fun saveRecord(record: ToSaveRecord): Long {
        val templateId = templatesDAO.insertOrUpdate(mapper.map(record.template))
        return recordsDAO.insert(mapper.map(record, templateId))
    }

    override suspend fun duplicateRecord(recordId: Long): Long {
        return getRecord(recordId)!!.let { record ->
            recordsDAO.insert(mapper.map(record, LocalDateTime.now()))
        }
    }

    @Transaction
    override suspend fun getRecord(recordId: Long): Record? {
        return recordsDAO.get(recordId)?.let(mapper::map)
    }

    override suspend fun deleteRecordAndCleanupTemplate(recordId: Long): Pair<Boolean, Boolean> {
        val templateId = recordsDAO.get(recordId)!!.template.id!!
        recordsDAO.delete(recordId)
        return deleteTemplateIfUnused(templateId)
    }

    override suspend fun updateTemplate(
        templateId: Long,
        image: Uri?,/* = null */
        title: String?,/* = null */
        description: String?,/* = null */
    ) {
        templatesDAO.get(templateId)?.let { oldTemplate ->
            templatesDAO.insertOrUpdate(
                TemplateDBModel(
                    id = templateId,
                    image = image ?: oldTemplate.image,
                    name = title ?: oldTemplate.name,
                    description = description ?: oldTemplate.description,
                )
            )
            oldTemplate.image?.let { deleteImageIfUnused(it) }
        }
    }

    override suspend fun deleteImage(templateId: Long) {
        templatesDAO.get(templateId)?.let { oldTemplate ->
            templatesDAO.insertOrUpdate(
                TemplateDBModel(
                    id = templateId,
                    image = null,
                    name = oldTemplate.name,
                    description = oldTemplate.description,
                )
            )
            oldTemplate.image?.let { deleteImageIfUnused(it) }
        }
    }

    private suspend fun deleteTemplateIfUnused(templateId: Long): Pair<Boolean, Boolean> {
        return if (recordsDAO.getByTemplate(templateId).isEmpty()) { // template is unused
            val image = templatesDAO.get(templateId)!!.image
            val documentDeleted = templatesDAO.delete(templateId) > 0
            val imageDeleted = image
                ?.let {
                    deleteImageIfUnused(it)
                } == true
            Pair(documentDeleted, imageDeleted)
        } else {
            Pair(false, false)
        }
    }

    private suspend fun deleteImageIfUnused(image: Uri): Boolean {
        if (templatesDAO.getByImage(image).isEmpty()) {
            return documentDeleter.delete(image)
        }
        return false
    }
}