package dev.gaborbiro.notes.data.chatgpt

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import dev.gaborbiro.notes.data.chatgpt.model.DomainError
import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryRequest
import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryResponse
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTApiError
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTRequest
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTResponse
import dev.gaborbiro.notes.data.chatgpt.service.model.ContentEntry
import dev.gaborbiro.notes.data.chatgpt.service.model.InputContent
import dev.gaborbiro.notes.data.chatgpt.service.model.OutputContent
import dev.gaborbiro.notes.data.chatgpt.service.model.Role

internal fun FoodPicSummaryRequest.toApiModel(): ChatGPTRequest {
    return ChatGPTRequest(
        input = listOf(
            ContentEntry(
                role = Role.system,
                content = listOf(
                    InputContent.Text(
                        "Assistant is an intelligent image analyser designed to help users keep track of their meals. The user will upload a photo of what they ate and Assistant suggests one, concise title as well as total kilo calories. The title will be used as an entry in a food diary list, but later on it will also be used by Assistant (in conjunction with the same image) to estimate calory and macro-nutrients. It is up to the user to either accept the title or write their own. Assistant formats its answer like the following example:\n" +
                                "{\n" +
                                "  \"title\": \"Kefir (Low Fat, 500ml)\",\n" +
                                "  \"kcal\": 42\n" +
                                "}" +
                                "\nAssistant should only give an answer if it has high confidence that the photo is actually of food and it is food the user might have eaten. Otherwise simply return the string null instead of the json."
                    )
                ),
            ),
            ContentEntry(
                role = Role.user,
                content = listOf(
                    InputContent.Image(base64Image)
                )
            )
        )
    )
}

private val gson = GsonBuilder().create()

internal fun ChatGPTResponse.toFoodPicSummaryResponse(): FoodPicSummaryResponse {
    val resultJson: String? = this.output
        .lastOrNull {
            it.role == Role.assistant &&
                    it.content.any { it is OutputContent.Text }
        }
        ?.content
        ?.filterIsInstance<OutputContent.Text>()
        ?.firstOrNull {
            it.text.isNotBlank() && it.text != "null"
        }
        ?.text
    class TitleAndKCal(
        @SerializedName("title") val title: String,
        @SerializedName("kcal") val kcal: Int,
    )
    val titleAndKCal = gson.fromJson(resultJson, TitleAndKCal::class.java)
    return FoodPicSummaryResponse(
        title = titleAndKCal.title,
        kcal = titleAndKCal.kcal,
    )
}

internal fun ChatGPTApiError.toDomainModel(): DomainError {
    return when (this) {
        is ChatGPTApiError.AuthApiError -> DomainError.GoToSignInScreen(message, this)
        is ChatGPTApiError.InternetApiError -> DomainError.DisplayMessageToUser.CheckInternetConnection(this)
        is ChatGPTApiError.MappingApiError, is ChatGPTApiError.ContentNotFoundError -> DomainError.DisplayMessageToUser.ContactSupport(this)
        is ChatGPTApiError.GenericApiError -> message
            ?.let { DomainError.DisplayMessageToUser.Message(it, this) }
            ?: DomainError.DisplayMessageToUser.TryAgain(this)
    }
}
