package dev.gaborbiro.notes.data.chatgpt

import dev.gaborbiro.notes.data.chatgpt.model.QueryRequest
import dev.gaborbiro.notes.data.chatgpt.model.Response
import dev.gaborbiro.notes.data.chatgpt.service.ChatGPTService
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTApiError
import dev.gaborbiro.notes.data.chatgpt.util.parse
import dev.gaborbiro.notes.data.chatgpt.util.runCatching


internal class ChatGPTRepositoryImpl(
    private val service: ChatGPTService,
) : ChatGPTRepository {

    override suspend fun query(query: QueryRequest): Response {
        runCatching(logTag = "getChatCompletions") {

        }
        try {
            return runCatching(logTag = "getChatCompletions") {
                val response = service.getChatCompletions(
                    request = query.toApiModel(),
                )
                return@runCatching parse(response)
                    .toDomainModel()
            }
        } catch (apiError: ChatGPTApiError) {
            throw apiError
                .toDomainModel()
        }
    }
}
