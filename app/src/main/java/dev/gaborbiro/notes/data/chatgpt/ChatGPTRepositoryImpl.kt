package dev.gaborbiro.notes.data.chatgpt

import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryRequest
import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryResponse
import dev.gaborbiro.notes.data.chatgpt.service.ChatGPTService
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTApiError
import dev.gaborbiro.notes.data.chatgpt.util.parse
import dev.gaborbiro.notes.data.chatgpt.util.runCatching


internal class ChatGPTRepositoryImpl(
    private val service: ChatGPTService,
) : ChatGPTRepository {

    override suspend fun summarizeFoodPic(request: FoodPicSummaryRequest): FoodPicSummaryResponse {
        try {
            return runCatching(logTag = "getChatCompletions") {
                val response = service.callResponses(
                    request = request.toApiModel(),
                )
                return@runCatching parse(response)
                    .toFoodPicSummaryResponse()
            }
        } catch (apiError: ChatGPTApiError) {
            throw apiError
                .toDomainModel()
        }
    }
}
