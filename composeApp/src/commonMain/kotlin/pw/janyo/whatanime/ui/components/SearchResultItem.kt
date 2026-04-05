package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.UriHandler
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
import pw.janyo.whatanime.ui.theme.Icons
import pw.janyo.whatanime.utils.copyToClipboardThenToast
import pw.janyo.whatanime.utils.formatDecimal
import pw.janyo.whatanime.utils.formatEpisode
import pw.janyo.whatanime.utils.formatTime
import pw.janyo.whatanime.utils.showSharePanel
import whatanime.composeapp.generated.resources.Res
import whatanime.composeapp.generated.resources.action_copy_anilist_id
import whatanime.composeapp.generated.resources.action_copy_my_anime_list_id
import whatanime.composeapp.generated.resources.action_copy_title
import whatanime.composeapp.generated.resources.action_play_preview_video
import whatanime.composeapp.generated.resources.action_share_title
import whatanime.composeapp.generated.resources.action_view_anilist
import whatanime.composeapp.generated.resources.detail_hint_native_title
import whatanime.composeapp.generated.resources.ic_load_failed
import whatanime.composeapp.generated.resources.janyo_studio


@Composable
fun SearchResultItem(
    result: SearchAnimeResultItem,
    onClick: () -> Unit,
) {
    val typography = MaterialTheme.typography
    val colorScheme = MaterialTheme.colorScheme
    val title = result.aniList.title.native ?: result.fileName
    val episodeText = formatEpisode(result.episode)
    val timeText = result.formatTimeRange()
    val metaText = buildList {
        episodeText?.let { add("第 $it 集") }
        timeText?.let { add(it) }
    }.joinToString(" • ")
    val similarityText = "${formatDecimal(result.similarity * 100, 1)}%"

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(result.image)
                    .apply {
                        if (Configure.preferWebp) {
                            httpHeaders(
                                NetworkHeaders.Builder()
                                    .set("Accept", "image/webp")
                                    .build()
                            )
                        }
                    }
                    .build(),
                placeholder = painterResource(Res.drawable.janyo_studio),
                error = painterResource(Res.drawable.ic_load_failed),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(144.dp)
                    .aspectRatio(16f / 9f)
                    .align(Alignment.CenterVertically)
                    .clip(RoundedCornerShape(4.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "《$title》",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                if (metaText.isNotEmpty()) {
                    Text(
                        text = metaText,
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                result.aniList.id?.let {
                    Text(
                        text = "AniList ID：$it",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                result.aniList.idMal?.let {
                    Text(
                        text = "MyAnimeList ID：$it",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = similarityText,
                        style = typography.labelMedium,
                        color = colorScheme.primary,
                        modifier = Modifier.align(Alignment.BottomEnd),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun SearchAnimeResultItem.formatTimeRange(): String? {
    if (from == null || to == null || (from == 0.0 && to == 0.0)) {
        return null
    }
    return "${(from.toLong() * 1000).formatTime()} - ${(to.toLong() * 1000).formatTime()}"
}


@Composable
fun BuildBottomSheet(
    uriHandler: UriHandler,
    context: PlatformContext,
    openBottomSheet: MutableState<Boolean>,
    item: SearchAnimeResultItem?,
    onPlayVideo: (SearchAnimeResultItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (openBottomSheet.value && item != null) {
        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet.value = false
            },
            sheetState = sheetState,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = true,
                shouldDismissOnClickOutside = true,
            )
        ) {
            val title = item.aniList.title.native ?: item.fileName
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = stringResource(
                        Res.string.detail_hint_native_title,
                        title
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(14.dp))
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_copy_title)) },
                    leadingContent = { Icons(Icons.Default.ContentCopy) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        scope.launch { copyToClipboardThenToast(context, title) }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                )
                item.aniList.id?.let { aniListId ->
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.action_copy_anilist_id)) },
                        supportingContent = { Text(aniListId.toString()) },
                        leadingContent = { Icons(Icons.Default.ContentCopy) },
                        modifier = Modifier.fillMaxWidth().clickable {
                            openBottomSheet.value = false
                            scope.launch { copyToClipboardThenToast(context, aniListId.toString()) }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    )
                }
                item.aniList.idMal?.let { myAnimeListId ->
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.action_copy_my_anime_list_id)) },
                        supportingContent = { Text(myAnimeListId.toString()) },
                        leadingContent = { Icons(Icons.Default.ContentCopy) },
                        modifier = Modifier.fillMaxWidth().clickable {
                            openBottomSheet.value = false
                            scope.launch {
                                copyToClipboardThenToast(context, myAnimeListId.toString())
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                    )
                }
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_share_title)) },
                    leadingContent = { Icons(Icons.Default.Share) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        showSharePanel(context, title)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_view_anilist)) },
                    leadingContent = { Icons(Icons.Default.Info) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        uriHandler.openUri("https://anilist.co/anime/${item.aniList.id}")
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_play_preview_video)) },
                    leadingContent = { Icons(Icons.Default.PlayArrow) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        onPlayVideo(item)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                )
            }
        }
    }
}