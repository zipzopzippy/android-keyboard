/*
 * Emoji Kitchen support, added in a personal modified build of FUTO Keyboard.
 */

package org.futo.inputmethod.latin.uix.actions.emoji.kitchen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.LocalKeyboardScheme
import java.io.File

private val StickerSize = 52.dp
private val RowHeight = 64.dp
private val GapBelowRow = 4.dp
private const val ThumbnailPx = 160

/** How much taller the emoji window gets while the row is shown */
val EmojiKitchenRowHeight = RowHeight + GapBelowRow

/**
 * A row of Emoji Kitchen stickers mixing [emoji] with other emojis. Tapping one downloads it if needed and
 * passes the file to [onSend].
 */
@Composable
fun EmojiKitchenRow(
    emoji: String,
    preferredPartners: List<String>,
    onSend: (EmojiKitchenSticker, File) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val downloadFailed = stringResource(R.string.emoji_kitchen_download_failed)

    val stickers by produceState(emptyList<EmojiKitchenSticker>(), emoji) {
        value = EmojiKitchen.stickersFor(context, emoji, preferredPartners)
    }
    LaunchedEffect(emoji) {
        listState.scrollToItem(0)
    }

    // Same container as the emoji grid below it, so the two line up
    Row(
        modifier = modifier
            .padding(bottom = GapBelowRow)
            .fillMaxWidth()
            .height(RowHeight)
            .background(LocalKeyboardScheme.current.keyboardContainer, RoundedCornerShape(9.dp))
            .clip(RoundedCornerShape(9.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            state = listState,
            contentPadding = PaddingValues(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(stickers, key = { it.url }) { sticker ->
                StickerItem(sticker) {
                    scope.launch {
                        val file = EmojiKitchen.getStickerFile(context, sticker)
                        if (file == null) {
                            Toast.makeText(context, downloadFailed, Toast.LENGTH_SHORT).show()
                        } else {
                            onSend(sticker, file)
                        }
                    }
                }
            }
        }
        val closeDescription = stringResource(R.string.emoji_kitchen_close)
        Box(
            modifier = Modifier
                .padding(end = 4.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onClose)
                .semantics { contentDescription = closeDescription },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = LocalKeyboardScheme.current.onKeyboardContainer,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun StickerItem(sticker: EmojiKitchenSticker, onClick: () -> Unit) {
    val context = LocalContext.current
    // null while loading, then the thumbnail or a failure
    val thumbnail by produceState<Result<Bitmap>?>(null, sticker.url) {
        val file = EmojiKitchen.getStickerFile(context, sticker)
        val bitmap = file?.let { withContext(Dispatchers.IO) { decodeThumbnail(it.path) } }
        value = bitmap?.let { Result.success(it) } ?: Result.failure(Exception("Sticker unavailable"))
    }
    val description = "${sticker.first}${sticker.second}"
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(StickerSize)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        val result = thumbnail
        val image = result?.getOrNull()
        when {
            result == null -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = LocalKeyboardScheme.current.primary,
            )
            image != null -> Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxHeight(),
            )
            // Offline or network permission off: show which emojis would be mixed
            else -> Text(text = description, fontSize = 18.sp, color = LocalKeyboardScheme.current.onKeyboardContainer)
        }
    }
}

private fun decodeThumbnail(path: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= ThumbnailPx) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sampleSize })
}
