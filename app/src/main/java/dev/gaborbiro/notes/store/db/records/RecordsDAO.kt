package dev.gaborbiro.notes.store.db.records

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.gaborbiro.notes.store.db.records.model.RecordAndTemplateDBModel
import dev.gaborbiro.notes.store.db.records.model.RecordDBModel

@Dao
interface RecordsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordDBModel): Long

    @Transaction
    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    suspend fun get(): List<RecordAndTemplateDBModel>

    @Transaction
    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    fun getLiveData(): LiveData<List<RecordAndTemplateDBModel>>

    @Transaction
    @Query("SELECT * FROM records WHERE _id=:id")
    suspend fun get(id: Long): RecordAndTemplateDBModel?

    @Query("DELETE FROM records WHERE _id = :id")
    suspend fun delete(id: Long): Int
}