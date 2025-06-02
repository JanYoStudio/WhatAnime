package pw.janyo.whatanime.ui.theme

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.painterResource
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.ic_github
import whatanime.composeapp.generated.resources.ic_google_play

object WaIcons {
    object Settings {
        val github: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_github)
        val googlePlay: Painter
            @Composable
            get() = painterResource(Res.drawable.ic_google_play)
    }
}

@Composable
fun Icons(
    painter: Painter,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        tint = tint,
    )
}

@Composable
fun Icons(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = imageVector.name,
        modifier = modifier,
        tint = tint,
    )
}