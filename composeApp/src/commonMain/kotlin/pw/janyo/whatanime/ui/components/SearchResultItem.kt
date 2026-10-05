package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.PlatformContext
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.utils.*
import whatanime.composeapp.generated.resources.*

@Composable
fun SearchResultItem(result: SearchAnimeResultItem, onPlay: () -> Unit = {}, onClick: () -> Unit) {
    val title = result.aniList.title.native ?: result.fileName
    val playLabel = stringResource(Res.string.ui_play)
    val menuLabel = stringResource(Res.string.ui_more)
    ElevatedCard(Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.padding(12.dp)) {
            val vertical = maxWidth < 336.dp || (LocalDensity.current.fontScale >= 1.5f && maxWidth < 376.dp)
            val thumbnail: @Composable (Modifier) -> Unit = { modifier ->
                Box(
                    modifier.aspectRatio(16f / 9f).clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable(role = Role.Button, onClickLabel = playLabel, onClick = onPlay),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current).data(result.image).apply {
                            if (Configure.preferWebp) httpHeaders(NetworkHeaders.Builder().set("Accept", "image/webp").build())
                        }.build(),
                        error = painterResource(Res.drawable.ic_load_failed), contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
                    )
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                        Icon(Icons.Default.PlayArrow, playLabel, Modifier.padding(12.dp).size(24.dp))
                    }
                }
            }
            val information: @Composable (Modifier) -> Unit = { modifier ->
                Column(
                    modifier.clickable(role = Role.Button, onClickLabel = menuLabel, onClick = onClick).padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    formatEpisode(result.episode)?.let { Text(stringResource(Res.string.ui_episode, it), style = MaterialTheme.typography.bodyMedium) }
                    result.formatTimeRange()?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.ui_similarity, "${formatDecimal(result.similarity * 100, 1)}%"),
                            Modifier.weight(1f), color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                        IconButton(onClick = onClick) { Icon(Icons.Default.MoreVert, menuLabel) }
                    }
                }
            }
            if (vertical) Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                thumbnail(Modifier.fillMaxWidth())
                information(Modifier.fillMaxWidth())
            } else Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                thumbnail(Modifier.width(128.dp))
                information(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SearchAnimeResultItem.formatTimeRange(): String? {
    if (from == null || to == null || (from == 0.0 && to == 0.0)) return null
    val units = mapOf(
        TimeUnit.MILLISECOND to stringResource(Res.string.ui_time_millisecond),
        TimeUnit.SECOND to stringResource(Res.string.ui_time_second),
        TimeUnit.MINUTE to stringResource(Res.string.ui_time_minute),
        TimeUnit.HOUR to stringResource(Res.string.ui_time_hour),
        TimeUnit.DAY to stringResource(Res.string.ui_time_day),
    )
    fun format(seconds: Double) = (seconds.toLong() * 1000).formatTime(
        minTimeUnit = TimeUnit.SECOND, unitLabel = { units.getValue(it) },
    )
    return "${format(from)} - ${format(to)}"
}

@Composable
fun BuildBottomSheet(
    uriHandler: UriHandler,
    context: PlatformContext,
    openBottomSheet: MutableState<Boolean>,
    item: SearchAnimeResultItem?,
    onPlayVideo: (SearchAnimeResultItem) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    if (openBottomSheet.value && item != null) {
        ModalBottomSheet(onDismissRequest = { openBottomSheet.value = false }, sheetState = sheetState) {
            val title = item.aniList.title.native ?: item.fileName
            Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                Text(title, Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                MenuAction(stringResource(Res.string.action_copy_title)) {
                    openBottomSheet.value = false
                    scope.launch { copyToClipboardThenToast(context, title) }
                }
                item.aniList.id?.takeIf { it > 0 }?.let { id ->
                    MenuAction(stringResource(Res.string.action_copy_anilist_id), id.toString()) {
                        openBottomSheet.value = false
                        scope.launch { copyToClipboardThenToast(context, id.toString()) }
                    }
                }
                item.aniList.idMal?.takeIf { it > 0 }?.let { id ->
                    MenuAction(stringResource(Res.string.action_copy_my_anime_list_id), id.toString()) {
                        openBottomSheet.value = false
                        scope.launch { copyToClipboardThenToast(context, id.toString()) }
                    }
                }
                MenuAction(stringResource(Res.string.action_share_title)) {
                    openBottomSheet.value = false
                    showSharePanel(context, title)
                }
                item.aniList.id?.takeIf { it > 0 }?.let { id ->
                    MenuAction(stringResource(Res.string.action_view_anilist)) {
                        openBottomSheet.value = false
                        uriHandler.openUri("https://anilist.co/anime/$id")
                    }
                }
                MenuAction(stringResource(Res.string.action_play_preview_video)) {
                    openBottomSheet.value = false
                    onPlayVideo(item)
                }
            }
        }
    }
}

@Composable
private fun MenuAction(label: String, detail: String? = null, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onClick).padding(16.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium)
    }
}
