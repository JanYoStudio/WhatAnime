package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import org.jetbrains.compose.resources.stringResource
import pw.janyo.whatanime.LocalLayoutSpec
import pw.janyo.whatanime.model.SearchAnimeResultItem
import whatanime.composeapp.generated.resources.*

@Composable
fun OriginalImageCard(image: PlatformFile, loading: Boolean) {
    var unavailable by remember(image) { mutableStateOf(false) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.padding(12.dp)) {
            val preview: @Composable () -> Unit = {
                SubcomposeAsyncImage(
                    model = image, contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(112.dp, 63.dp).clip(MaterialTheme.shapes.small),
                    error = { Icon(Icons.Outlined.BrokenImage, null) },
                    onError = { unavailable = true }, onSuccess = { unavailable = false },
                )
            }
            val caption: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(Res.string.ui_current_image), style = MaterialTheme.typography.labelMedium)
                    Text(image.name, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                    if (unavailable) Text(stringResource(Res.string.ui_image_unavailable), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (maxWidth < 336.dp) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                preview(); caption()
            } else Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                preview()
                Box(Modifier.weight(1f)) { caption() }
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}

@Composable
fun SearchWorkspace(
    image: PlatformFile?, results: List<SearchAnimeResultItem>, loading: Boolean,
    listState: LazyListState, modifier: Modifier = Modifier, bottomSpace: androidx.compose.ui.unit.Dp = 16.dp,
    status: @Composable () -> Unit, onPlay: (SearchAnimeResultItem) -> Unit,
    onMenu: (SearchAnimeResultItem) -> Unit, overlay: @Composable BoxScope.() -> Unit = {},
) {
    val layout = LocalLayoutSpec.current
    val list: @Composable (Modifier, Boolean) -> Unit = { childModifier, dual ->
        LazyColumn(
            childModifier, state = listState,
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomSpace),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (image != null && !dual && layout.stickyImage) {
                stickyHeader(key = "original") {
                    Box(Modifier.background(MaterialTheme.colorScheme.surface).padding(bottom = 8.dp)) { OriginalImageCard(image, loading) }
                }
            } else item(key = "original") { if (image != null && !dual) OriginalImageCard(image, loading) }
            item(key = "status") { if (!dual) status() }
            if (loading) items(3) {
                ElevatedCard(Modifier.fillMaxWidth().height(112.dp)) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHighest))
                }
            }
            itemsIndexed(results, key = { index, _ -> "candidate-$index" }) { _, result ->
                SearchResultItem(result, onPlay = { onPlay(result) }, onClick = { onMenu(result) })
            }
        }
    }
    if (layout.dualPane) {
        Row(modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = bottomSpace),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (image != null) OriginalImageCard(image, loading)
                    status()
                }
                overlay()
            }
            list(Modifier.weight(1f).fillMaxHeight(), true)
        }
    } else Box(modifier.padding(horizontal = 16.dp)) {
        list(Modifier.fillMaxSize(), false)
        overlay()
    }
}

@Composable
fun WorkspaceMessage(message: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
        if (action != null) OutlinedButton(onClick = onAction) { Text(action) }
    }
}
