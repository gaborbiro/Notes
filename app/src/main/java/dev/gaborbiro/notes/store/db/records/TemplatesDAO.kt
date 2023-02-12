package dev.gaborbiro.notes.store.db.records

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel

@Dao
interface TemplatesDAO {

    @Upsert
    suspend fun insertOrUpdate(template: TemplateDBModel): Long

    @Query("SELECT * FROM templates WHERE _id=:id")
    suspend fun get(id: Long): TemplateDBModel?

    @Query("SELECT * FROM templates")
    suspend fun get(): List<TemplateDBModel>

    @Delete
    suspend fun delete(template: TemplateDBModel)
}