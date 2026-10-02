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

/**
 * 缩略图 / 预览图的内存缓存。
 *
 * 同一个地址在同一档尺寸上只下载一次：列表上下滚、点开预览再关掉，都不会重复下载。
 * 只在主线程（组合 / LaunchedEffect）里访问，所以没加锁 —— 加载本身在 IO 线程，
 * 回到这里时已经是主线程了。
 */
object RemoteImageCache {

    /** 最多留 48 张。超了丢最早放进来的那张（LinkedHashMap 保持插入顺序）。 */
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

/** 同时最多 4 张图在下载：一页几十张缩略图一起发请求，弱网下谁都下不完。 */
private val slots = Semaphore(4)

/**
 * 取一张网络图（先查缓存，没有才下载）。
 *
 * 只在 LazyColumn **可见的行**里调用，所以滚出屏幕的图不会被请求；
 * 拿不到就返回 null，界面自己显示占位。
 */
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