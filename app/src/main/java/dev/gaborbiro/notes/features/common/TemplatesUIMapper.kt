package dev.gaborbiro.notes.features.common

import android.graphics.Bitmap
import dev.gaborbiro.notes.data.records.domain.model.Template
import dev.gaborbiro.notes.features.common.model.TemplateUIModel
import dev.gaborbiro.notes.util.BitmapLoader

class TemplatesUIMapper(
    private val bitmapLoader: BitmapLoader,
) {

    fun map(records: List<Template>): List<TemplateUIModel> {
        return records.map(::map)
    }

    private fun map(template: Template): TemplateUIModel {
        val bitmap: Bitmap? = template.image?.let { bitmapLoader.loadBitmap(it) }
        return TemplateUIModel(
            templateId = template.id,
            bitmap = bitmap,
            title = template.name,
        )
    }
}