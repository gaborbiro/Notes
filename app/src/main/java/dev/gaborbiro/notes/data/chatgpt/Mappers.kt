package dev.gaborbiro.notes.data.chatgpt

import dev.gaborbiro.notes.data.chatgpt.model.DomainError
import dev.gaborbiro.notes.data.chatgpt.model.QueryRequest
import dev.gaborbiro.notes.data.chatgpt.model.Response
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTApiError
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTRequest
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTResponse
import dev.gaborbiro.notes.data.chatgpt.service.model.Message
import dev.gaborbiro.notes.data.chatgpt.service.model.Role


internal fun QueryRequest.toApiModel(): ChatGPTRequest {
    return ChatGPTRequest(
        messages = listOf(
            Message(
                role = Role.user,
                content = this.request,
            )
        )
    )
}

internal fun ChatGPTResponse.toDomainModel(): Response {
    return Response(
        this.choices.joinToString(separator = "\n\n") { it.message.content }
    )
}

internal fun ChatGPTApiError.toDomainModel(): DomainError {
    return when (this) {
        is ChatGPTApiError.AuthApiError -> DomainError.GoToSignInScreen(message)
        is ChatGPTApiError.InternetApiError -> DomainError.DisplayMessageToUser.CheckInternetConnection
        is ChatGPTApiError.MappingApiError, is ChatGPTApiError.ContentNotFoundError -> DomainError.DisplayMessageToUser.ContactSupport
        is ChatGPTApiError.GenericApiError -> message
            ?.let { DomainError.DisplayMessageToUser.Message(it) }
            ?: DomainError.DisplayMessageToUser.TryAgain
    }
}
