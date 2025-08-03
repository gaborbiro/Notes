package dev.gaborbiro.notes.data.chatgpt

import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryRequest
import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryResponse


interface ChatGPTRepository {

    suspend fun summarizeFoodPic(request: FoodPicSummaryRequest): FoodPicSummaryResponse
}
