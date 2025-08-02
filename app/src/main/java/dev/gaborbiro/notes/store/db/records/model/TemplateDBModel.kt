package dev.gaborbiro.notes.store.db.records.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import dev.gaborbiro.notes.store.db.common.DBModel

@Entity(tableName = "templates", indices = [Index(value = ["name", "image"], unique = true)])
data class TemplateDBModel(
    val image: String?,
    val name: String,
    val description: String,
) : DBModel() {

    @Ignore
    constructor(id: Long, image: String?, name: String, description: String) : this(
        image,
        name,
        description
    ) {
        this.id = id
    }
}
