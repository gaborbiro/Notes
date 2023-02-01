package dev.gaborbiro.notes.store.db.common

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

abstract class DBModel {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    var id: Long? = null

}

const val COLUMN_ID = "_id"
