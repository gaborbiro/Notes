package dev.gaborbiro.notes.data.records.domain.model

import java.time.LocalDateTime

data class ToSaveRecord(
    val timestamp: LocalDateTime,
    val notes: String,
)