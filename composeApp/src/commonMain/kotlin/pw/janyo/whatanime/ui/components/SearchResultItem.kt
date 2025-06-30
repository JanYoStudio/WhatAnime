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
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.model.SearchAnimeResultItem
import pw.janyo.whatanime.utils.formatDecimal
import pw.janyo.whatanime.utils.formatEpisode
import pw.janyo.whatanime.utils.formatTime
import whatanime.composeapp.generated.resources.Res
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
    onClickImage: () -> Unit,
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
                SelectableText(
                    text = stringResource(
                        Res.string.detail_hint_native_title,
                        result.aniList.title.native ?: result.fileName
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                formatEpisode(result.episode)?.let { episodeString ->
                    SelectableText(
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
                        .clickable(onClick = onClickImage),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    Column {
                        SelectableText(stringResource(Res.string.detail_hint_time))
                        SelectableText(stringResource(Res.string.detail_hint_ani_list_id))
                        SelectableText(stringResource(Res.string.detail_hint_my_anime_list_id))
                        SelectableText(
                            stringResource(Res.string.detail_hint_similarity),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        SelectableText("${(result.from.toLong() * 1000).formatTime()} ~ ${(result.to.toLong() * 1000).formatTime()}")
                        result.aniList.id?.let {
                            SelectableText(result.aniList.id.toString())
                        }
                        result.aniList.idMal?.let {
                            SelectableText(result.aniList.idMal.toString())
                        }
                        SelectableText(
                            text = "${formatDecimal(result.similarity * 100, 3)}%",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}