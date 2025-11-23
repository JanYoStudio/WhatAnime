package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ShowProgressDialog(
    state: ShowDialogState = rememberShowDialogState(),
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    if (!state.isShowing) {
        return
    }
    Dialog(
        onDismissRequest = { state.hide() },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                LoadingIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = text, fontSize = fontSize,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}