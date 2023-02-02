package dev.gaborbiro.notes.data.records.domain

import android.net.Uri
import androidx.lifecycle.LiveData
import dev.gaborbiro.notes.data.records.RecordsMapper
import dev.gaborbiro.notes.data.records.RecordsRepositoryImpl
import dev.gaborbiro.notes.data.records.domain.model.Record
import dev.gaborbiro.notes.data.records.domain.model.ToSaveRecord
import dev.gaborbiro.notes.data.records.domain.model.ToSaveTemplate
import dev.gaborbiro.notes.store.db.AppDatabase

interface RecordsRepository {

    companion object {

        private lateinit var INSTANCE: RecordsRepository

        fun get(): RecordsRepository {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = RecordsRepositoryImpl(
                    templatesDAO = AppDatabase.getInstance().templatesDAO(),
                    recordsDAO = AppDatabase.getInstance().recordsDAO(),
                    mapper = RecordsMapper.get(),
                )
            }
            return INSTANCE
        }
    }

    suspend fun getRecords(): List<Record>

    fun getRecordsLiveData(): LiveData<List<Record>>

    suspend fun saveTemplate(template: ToSaveTemplate): Long

    suspend fun saveRecord(record: ToSaveRecord): Long

    suspend fun duplicateRecord(recordId: Long, notes: String): Long

    suspend fun getRecord(recordId: Long): Record?

    suspend fun delete(recordId: Long): Boolean
    suspend fun updateTemplatePhoto(templateId: Long, uri: Uri?): Boolean
}