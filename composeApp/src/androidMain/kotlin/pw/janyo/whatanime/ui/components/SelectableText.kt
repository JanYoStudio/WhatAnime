package pw.janyo.whatanime.ui.components

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle

@Composable
fun SelectableText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null
) {
    val style = LocalTextStyle.current
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                setTextIsSelectable(true)
            }
        },
        update = { textView ->
            textView.text = text
            if (color != Color.Unspecified) {
                textView.setTextColor(color.value.toInt())
            }
            if (fontSize != TextUnit.Unspecified) {
                textView.textSize = if (fontSize.isSp) fontSize.value else style.fontSize.value
            }
            fontWeight?.let {
                textView.typeface = android.graphics.Typeface.create(textView.typeface, it.weight)
            }
        },
        modifier = modifier
    )
}
