/*
 * Emoji Kitchen support, added in a personal modified build of FUTO Keyboard.
 */

package org.futo.inputmethod.latin.uix.actions.emoji.kitchen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * One Emoji Kitchen sticker: the mix of [first] and [second].
 */
data class EmojiKitchenSticker(
    val first: String,
    val second: String,
    val url: String,
) {
    /** Name of the cached file, derived from the URL path so it is unique per sticker. */
    val fileName: String
        get() = url.substringAfterLast('/')
}

/**
 * Index of the emoji pairs Gboard's Emoji Kitchen has stickers for, loaded from
 * `emoji_kitchen/index.txt` (see `tools/emoji_kitchen`), plus downloading and sending stickers.
 *
 * Stickers are Google's images on www.gstatic.com and are only downloaded when shown.
 */
object EmojiKitchen {
    private const val TAG = "EmojiKitchen"
    private const val INDEX_PATH = "emoji_kitchen/index.txt"
    private const val STICKER_BASE_URL = "https://www.gstatic.com/android/keyboard/emojikitchen"
    private const val CACHE_DIR = "emoji_kitchen"
    const val MIME_TYPE = "image/png"
    // Keep the cache small, it is just a speed-up for recently used stickers
    private const val MAX_CACHE_BYTES = 30L * 1024 * 1024

    internal class Index(
        val dates: List<String>,
        val keys: List<String>,
        val emojis: List<String>,
        val keyIndexByEmoji: Map<String, Int>,
        // For every emoji: the partners it has stickers with, packed as (partner shl 16) or (date shl 1) or isLeft
        val partners: Array<IntArray>,
    )

    private val loadLock = Mutex()
    @Volatile
    private var index: Index? = null
    private val downloadLock = Mutex()

    private fun stripModifiers(emoji: String): String = buildString {
        var i = 0
        while (i < emoji.length) {
            val cp = emoji.codePointAt(i)
            // Drop variation selector 16 and skin tone modifiers, which Emoji Kitchen doesn't distinguish
            if (cp != 0xFE0F && cp !in 0x1F3FB..0x1F3FF) appendCodePoint(cp)
            i += Character.charCount(cp)
        }
    }

    private fun keyToEmoji(key: String): String = buildString {
        for (part in key.split('-')) appendCodePoint(part.toInt(16))
    }

    private fun urlSegment(key: String) = "u" + key.replace("-", "-u")

    private suspend fun loadIndex(context: Context): Index? {
        index?.let { return it }
        return loadLock.withLock {
            index ?: withContext(Dispatchers.IO) {
                try {
                    context.assets.open(INDEX_PATH).bufferedReader().use { parseIndex(it) }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load Emoji Kitchen index", e)
                    null
                }
            }?.also { index = it }
        }
    }

    internal fun parseIndex(reader: BufferedReader): Index {
        reader.let { reader ->
            check(reader.readLine() == "v1") { "Unknown Emoji Kitchen index version" }
            val dates = reader.readLine().split(' ')
            val keys = reader.readLine().split(' ')
            val emojis = keys.map { keyToEmoji(it) }
            val keyIndexByEmoji = HashMap<String, Int>(emojis.size * 2)
            emojis.forEachIndexed { i, emoji ->
                keyIndexByEmoji[emoji] = i
                keyIndexByEmoji.putIfAbsent(stripModifiers(emoji), i)
            }
            val lists = Array(keys.size) { ArrayList<Int>() }
            reader.forEachLine { line ->
                if (line.isEmpty()) return@forEachLine
                val first = line.indexOf(',')
                val second = line.indexOf(',', first + 1)
                val left = line.substring(0, first).toInt()
                val right = line.substring(first + 1, second).toInt()
                val date = line.substring(second + 1).toInt()
                lists[left].add((right shl 16) or (date shl 1) or 1)
                if (left != right) {
                    lists[right].add((left shl 16) or (date shl 1))
                }
            }
            // Order partners like Gboard does, which is the order of the emoji keys
            val partners = Array(keys.size) { i -> lists[i].sortedBy { it ushr 16 }.toIntArray() }
            return Index(dates, keys, emojis, keyIndexByEmoji, partners)
        }
    }

    private fun Index.indexOf(emoji: String): Int? {
        return keyIndexByEmoji[emoji] ?: keyIndexByEmoji[stripModifiers(emoji)]
    }

    private fun Index.stickerFor(emojiIndex: Int, packed: Int): EmojiKitchenSticker {
        val partner = packed ushr 16
        val date = dates[(packed shr 1) and 0x7FFF]
        val isLeft = (packed and 1) == 1
        val (left, right) = if (isLeft) emojiIndex to partner else partner to emojiIndex
        val leftSegment = urlSegment(keys[left])
        val rightSegment = urlSegment(keys[right])
        return EmojiKitchenSticker(
            first = emojis[emojiIndex],
            second = emojis[partner],
            url = "$STICKER_BASE_URL/$date/$leftSegment/${leftSegment}_$rightSegment.png",
        )
    }

    /**
     * Returns whether [emoji] can be mixed at all.
     */
    suspend fun supports(context: Context, emoji: String): Boolean {
        val index = loadIndex(context) ?: return false
        return supports(index, emoji)
    }

    /**
     * Returns the stickers mixing [emoji] with other emojis. Mixes with [preferredPartners] (e.g. recently typed
     * emojis) come first, then the emoji with itself, then the rest in Gboard's order.
     */
    suspend fun stickersFor(
        context: Context,
        emoji: String,
        preferredPartners: List<String> = emptyList(),
    ): List<EmojiKitchenSticker> {
        val index = loadIndex(context) ?: return emptyList()
        return stickersFor(index, emoji, preferredPartners)
    }

    internal fun supports(index: Index, emoji: String): Boolean = index.indexOf(emoji) != null

    internal fun stickersFor(
        index: Index,
        emoji: String,
        preferredPartners: List<String>,
    ): List<EmojiKitchenSticker> {
        val emojiIndex = index.indexOf(emoji) ?: return emptyList()
        val packedPartners = index.partners[emojiIndex]
        val byPartner = HashMap<Int, Int>(packedPartners.size * 2)
        for (packed in packedPartners) byPartner[packed ushr 16] = packed

        val order = LinkedHashSet<Int>()
        for (partnerEmoji in preferredPartners) {
            val partner = index.indexOf(partnerEmoji) ?: continue
            if (partner in byPartner) order.add(partner)
        }
        if (emojiIndex in byPartner) order.add(emojiIndex)
        for (packed in packedPartners) order.add(packed ushr 16)
        return order.map { partner -> index.stickerFor(emojiIndex, byPartner.getValue(partner)) }
    }

    private fun cacheDir(context: Context) = File(context.cacheDir, CACHE_DIR)

    /**
     * Returns the downloaded sticker file, downloading it first if needed. Returns null if the download failed,
     * e.g. because the network permission is turned off.
     */
    suspend fun getStickerFile(context: Context, sticker: EmojiKitchenSticker): File? = withContext(Dispatchers.IO) {
        val dir = cacheDir(context)
        val file = File(dir, sticker.fileName)
        if (file.isFile && file.length() > 0) {
            file.setLastModified(System.currentTimeMillis())
            return@withContext file
        }
        downloadLock.withLock {
            if (file.isFile && file.length() > 0) return@withLock file
            try {
                dir.mkdirs()
                val tmpFile = File(dir, "${sticker.fileName}.tmp")
                val connection = URL(sticker.url).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 15_000
                    connection.setRequestProperty("User-Agent", "FlorisBoard")
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                        throw IOException("HTTP ${connection.responseCode} for ${sticker.url}")
                    }
                    connection.inputStream.use { input ->
                        tmpFile.outputStream().use { output -> input.copyTo(output) }
                    }
                } finally {
                    connection.disconnect()
                }
                if (!tmpFile.renameTo(file)) {
                    tmpFile.delete()
                    throw IOException("Could not move sticker into the cache")
                }
                trimCache(dir)
                file
            } catch (e: Exception) {
                Log.w(TAG, "Failed to download Emoji Kitchen sticker: $e")
                null
            }
        }
    }

    private fun trimCache(dir: File) {
        val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        var total = 0L
        for (file in files) {
            total += file.length()
            if (total > MAX_CACHE_BYTES) file.delete()
        }
    }

    /**
     * Returns a content URI for a downloaded sticker [file] that can be handed to the editor.
     */
    fun uriForSticker(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.emojikitchen", file)
    }
}
