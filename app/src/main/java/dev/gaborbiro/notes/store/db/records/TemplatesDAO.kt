package dev.gaborbiro.notes.store.db.records

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel

@Dao
interface TemplatesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(template: TemplateDBModel): Long
}