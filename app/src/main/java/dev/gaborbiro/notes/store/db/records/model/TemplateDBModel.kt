package dev.gaborbiro.notes.store.db.records.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import dev.gaborbiro.notes.store.db.common.DBModel

@Entity(tableName = "templates", indices = [Index(value = ["name", "image"], unique = true)])
class TemplateDBModel(
    val image: Uri?,
    val name: String,
    val description: String,
) : DBModel() {

    @Ignore
    constructor(id: Long, image: Uri?, name: String, description: String) : this(
        image,
        name,
        description
    ) {
        this.id = id
    }
}