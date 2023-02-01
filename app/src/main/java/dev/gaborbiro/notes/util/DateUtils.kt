package dev.gaborbiro.notes.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.formatShort() = format(DateTimeFormatter.ofPattern("E dd MMM, H:mm"))