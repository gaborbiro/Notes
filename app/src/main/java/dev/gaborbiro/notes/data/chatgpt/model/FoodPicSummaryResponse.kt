package dev.gaborbiro.notes.data.chatgpt.model

import com.google.gson.annotations.SerializedName

data class FoodPicSummaryResponse(
    @SerializedName("title") val title: String,
    @SerializedName("kcal") val kcal: Int,
)
