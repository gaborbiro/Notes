package dev.gaborbiro.notes.data.chatgpt.service.model

import com.google.gson.annotations.SerializedName

internal data class ChatGPTResponse(
    @SerializedName("output") val output: List<ContentEntry<OutputContent>>,
)
