package dev.gaborbiro.notes.data.records.domain.model

import android.net.Uri

data class ToSaveTemplate(
    val image: Uri?,
    val name: String,
    val description: String,
)

