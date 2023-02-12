package dev.gaborbiro.notes.store.db.records.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import dev.gaborbiro.notes.store.db.common.DBModel

@Entity(tableName = "templates")
class TemplateDBModel(
    val image: Uri?,
    val name: String,
    val description: String,
) : DBModel()