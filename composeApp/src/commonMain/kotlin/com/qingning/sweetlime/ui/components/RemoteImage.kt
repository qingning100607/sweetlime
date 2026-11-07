package com.qingning.sweetlime.ui.components

import com.qingning.sweetlime.core.loadImageBitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

object RemoteImageCache {

    private const val MAX_ENTRIES = 48

    private val entries = LinkedHashMap<String, ImageBitmap>()

    private fun key(url: String, maxPixels: Int) = "$maxPixels|$url"

    fun get(url: String, maxPixels: Int): ImageBitmap? = entries[key(url, maxPixels)]

    fun put(url: String, maxPixels: Int, bitmap: ImageBitmap) {
        entries[key(url, maxPixels)] = bitmap
        while (entries.size > MAX_ENTRIES) {
            val oldest = entries.keys.firstOrNull() ?: break
            entries.remove(oldest)
        }
    }
}

private val slots = Semaphore(4)

@Composable
fun rememberRemoteImage(url: String, maxPixels: Int): ImageBitmap? {
    var bitmap by remember(url, maxPixels) { mutableStateOf(RemoteImageCache.get(url, maxPixels)) }
    LaunchedEffect(url, maxPixels) {
        if (bitmap != null) return@LaunchedEffect
        val loaded = slots.withPermit { loadImageBitmap(url, maxPixels) }
        if (loaded != null) {
            RemoteImageCache.put(url, maxPixels, loaded)
            bitmap = loaded
        }
    }
    return bitmap
}