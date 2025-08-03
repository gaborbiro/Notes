package dev.gaborbiro.notes.data.chatgpt

import dev.gaborbiro.notes.data.chatgpt.model.QueryRequest
import dev.gaborbiro.notes.data.chatgpt.model.Response


interface ChatGPTRepository {

    suspend fun query(query: QueryRequest): Response
}
