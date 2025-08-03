package dev.gaborbiro.notes.features.host.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ImageDialog(
    image: Bitmap,
    onDialogDismissed: () -> Unit,
) {
    Dialog(onDismissRequest = onDialogDismissed) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 4.dp,
            modifier = Modifier.Companion.wrapContentSize()
        ) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "",
                modifier = Modifier.Companion.size(
                    width = image.width.dp * 2,
                    height = image.height.dp * 2
                )
            )
        }
    }
}
