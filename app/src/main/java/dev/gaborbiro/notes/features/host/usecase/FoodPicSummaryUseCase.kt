package dev.gaborbiro.notes.features.host.usecase

import android.graphics.Bitmap
import android.util.Base64
import android.util.Base64OutputStream
import dev.gaborbiro.notes.ImageFileFormat
import dev.gaborbiro.notes.data.chatgpt.ChatGPTRepository
import dev.gaborbiro.notes.data.chatgpt.model.FoodPicSummaryRequest
import dev.gaborbiro.notes.data.chatgpt.service.model.ChatGPTApiError
import dev.gaborbiro.notes.data.chatgpt.toDomainModel
import dev.gaborbiro.notes.store.bitmap.BitmapStore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class FoodPicSummaryUseCase(
    private val bitmapStore: BitmapStore,
    private val chatGPTRepository: ChatGPTRepository,
) {

    suspend fun execute(filename: String): String? {
        val response = try {
            val inputStream = bitmapStore.get(filename, thumbnail = false)
            chatGPTRepository.summarizeFoodPic(
                request = FoodPicSummaryRequest(
                    base64Image = inputStreamToBase64(inputStream)
                )
            )
        } catch (apiError: ChatGPTApiError) {
            throw apiError
                .toDomainModel()
        }
        return response.title + " (${response.kcal} kcal)"
    }

    /**
     * Encodes the input stream to a Base64 string. Does not decode image, just streams raw bytes.
     */
    private fun inputStreamToBase64(
        input: InputStream,
        format: Bitmap.CompressFormat = ImageFileFormat,
    ): String {
        val mimeType = when (format) {
            Bitmap.CompressFormat.PNG -> "image/png"
            Bitmap.CompressFormat.JPEG -> "image/jpeg"
            Bitmap.CompressFormat.WEBP -> "image/webp"
            else -> "application/octet-stream"
        }
        val baos = ByteArrayOutputStream()
        Base64OutputStream(baos, Base64.NO_WRAP).use { b64Out ->
            input.use { inp ->
                val buffer = ByteArray(8 * 1024)
                var read: Int
                while (inp.read(buffer).also { read = it } != -1) {
                    b64Out.write(buffer, 0, read)
                }
                b64Out.flush()
            }
        }
        val base64 = baos.toString("UTF-8")
        return "data:$mimeType;base64,$base64"
    }
}
