package dev.gaborbiro.notes.data.records.domain

import android.net.Uri
import dev.gaborbiro.notes.App
import dev.gaborbiro.notes.data.records.DBMapper
import dev.gaborbiro.notes.data.records.RecordsRepositoryImpl
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.AppDatabase
import dev.gaborbiro.notes.store.file.DocumentDeleter
import kotlinx.coroutines.flow.Flow

interface RecordsRepository {

    companion object {

        private lateinit var INSTANCE: RecordsRepository

        fun get(): RecordsRepository {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = RecordsRepositoryImpl(
                    templatesDAO = AppDatabase.getInstance().templatesDAO(),
                    recordsDAO = AppDatabase.getInstance().recordsDAO(),
                    mapper = DBMapper.get(),
                    documentDeleter = DocumentDeleter(App.appContext)
                )
            }
            return INSTANCE
        }
    }

    suspend fun getRecords(): List<Record>

    fun getRecordsFlow(): Flow<List<Record>>

    suspend fun saveTemplate(template: ToSaveTemplate): Long

    suspend fun saveRecord(record: ToSaveRecord): Long

    suspend fun duplicateRecord(recordId: Long): Long

    suspend fun getRecord(recordId: Long): Record?

    suspend fun deleteRecord(recordId: Long): Boolean

    suspend fun updateTemplatePhoto(templateId: Long, uri: Uri?)
}