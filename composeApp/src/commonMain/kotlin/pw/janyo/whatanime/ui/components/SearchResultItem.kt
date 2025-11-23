package pw.janyo.whatanime.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import whatanime.composeapp.generated.resources.action_copy_title
import whatanime.composeapp.generated.resources.action_play_preview_video
import whatanime.composeapp.generated.resources.action_share_title
import whatanime.composeapp.generated.resources.action_view_anilist
import whatanime.composeapp.generated.resources.detail_hint_ani_list_id
import whatanime.composeapp.generated.resources.detail_hint_episode
import whatanime.composeapp.generated.resources.detail_hint_my_anime_list_id
import whatanime.composeapp.generated.resources.detail_hint_native_title
import whatanime.composeapp.generated.resources.detail_hint_similarity
import whatanime.composeapp.generated.resources.detail_hint_time
import whatanime.composeapp.generated.resources.ic_load_failed
import whatanime.composeapp.generated.resources.janyo_studio


@Composable
fun SearchResultItem(
    result: SearchAnimeResultItem,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column {
                BuildText(
                    text = stringResource(
                        Res.string.detail_hint_native_title,
                        result.aniList.title.native ?: result.fileName
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                formatEpisode(result.episode)?.let { episodeString ->
                    BuildText(
                        text = stringResource(
                            Res.string.detail_hint_episode,
                            episodeString
                        ),
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    modifier = Modifier
                        .height(90.dp)
                        .width(160.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val annotationText = buildAnnotatedString {
                    if (result.from != null && result.from != 0.0 && result.to != null && result.to != 0.0) {
                        append(stringResource(Res.string.detail_hint_time))
                        append("${(result.from.toLong() * 1000).formatTime()} ~ ${(result.to.toLong() * 1000).formatTime()}")
                        appendLine()
                    }
                    result.aniList.id?.let {
                        append(stringResource(Res.string.detail_hint_ani_list_id))
                        append(result.aniList.id.toString())
                        appendLine()
                    }
                    result.aniList.idMal?.let {
                        append(stringResource(Res.string.detail_hint_my_anime_list_id))
                        append(result.aniList.idMal.toString())
                        appendLine()
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(Res.string.detail_hint_similarity))
                        append("${formatDecimal(result.similarity * 100, 3)}%")
                    }
                }
                Text(
                    text = annotationText,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun BuildText(
    text: String,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = 12.sp,
    textColor: Color = Color.Unspecified
) {
    Text(
        text = text,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        color = textColor,
    )
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
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(
                        Res.string.detail_hint_native_title,
                        title
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_copy_title)) },
                    leadingContent = { Icons(Icons.Default.ContentCopy) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        scope.launch { copyToClipboardThenToast(context, title) }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_share_title)) },
                    leadingContent = { Icons(Icons.Default.Share) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        showSharePanel(context, title)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_view_anilist)) },
                    leadingContent = { Icons(Icons.Default.Info) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        uriHandler.openUri("https://anilist.co/anime/${item.aniList.id}")
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.action_play_preview_video)) },
                    leadingContent = { Icons(Icons.Default.PlayArrow) },
                    modifier = Modifier.fillMaxWidth().clickable {
                        openBottomSheet.value = false
                        onPlayVideo(item)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}