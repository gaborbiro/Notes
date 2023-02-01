package dev.gaborbiro.notes.data.records.domain

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

    suspend fun saveTemplateAndRecord(record: ToSaveRecord, template: ToSaveTemplate): Long

    suspend fun saveRecord(record: ToSaveRecord, templateId: Long): Long

    suspend fun duplicateRecord(recordId: Long, notes: String): Long

    suspend fun getRecord(recordId: Long): Record?

    suspend fun delete(recordId: Long): Boolean
}