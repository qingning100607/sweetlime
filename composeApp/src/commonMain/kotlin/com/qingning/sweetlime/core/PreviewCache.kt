package com.qingning.sweetlime.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.time.TimeSource

/** 一条「样式 × 输入」的预计算结果：item 为 null 表示这个样式对这段文字没有变化。 */
class PreviewEntry(val item: TransformItem?, val preview: String)

/**
 * 输入文字的转换结果缓存 —— 「提前算」的地方。
 *
 * 主页输入框一改，[prefetch] 就在后台线程把全量转换连同**列表预览串**一起算好，
 * 整批算完再一次性发布到主线程；进分类页时页面只做数组下标取值，连
 * `listPreview()` 的扫描都不用在组合里做了。
 *
 * 线程约定：读取（[entry]）只发生在主线程；写入只在 [prefetch] 末尾通过
 * `withContext(Dispatchers.Main)` 切回主线程做一次引用赋值 —— 协程调度自带内存屏障，
 * 所以这里用普通数组，不需要额外同步原语。
 */
object PreviewCache {

    /** 还没预热过任何输入时的占位值。 */
    private const val NONE = "\u0000"

    private var cachedInput: String = NONE

    /** 按 [TransformRegistry.all] 的下标存放，O(1) 寻址，不做字符串哈希。 */
    private var slots: Array<PreviewEntry?> = arrayOfNulls(TransformRegistry.all.size)

    /**
     * 预热版本号。
     *
     * 界面把它读进 `remember` 的键里：预热完成时 +1，
     * 这样「进页面时缓存还没覆盖到」的那些行会自己刷新出来。
     */
    var version by mutableIntStateOf(0)
        private set

    /** 这份输入是否已经预热完成。界面据此决定「先给加载动画，还是直接铺列表」。 */
    fun isReady(input: String): Boolean = cachedInput == input

    /** 取某样式对某输入的预计算结果；缓存没覆盖到就当场算一条。 */
    fun entry(transform: TextTransform, input: String): PreviewEntry {
        if (input != cachedInput) return compute(transform, input)
        val index = TransformRegistry.indexOf(transform)
        if (index < 0) return compute(transform, input)
        return slots[index] ?: compute(transform, input)
    }

    private fun compute(transform: TextTransform, input: String): PreviewEntry {
        val item = TransformItem.of(transform, input) ?: return PreviewEntry(null, "")
        return PreviewEntry(item, item.output.listPreview())
    }

    /** 全量预热：把 [input] 下所有样式的转换结果和预览串算好。后台算，主线程发布。 */
    suspend fun prefetch(input: String) {
        if (input == cachedInput) return
        val mark = TimeSource.Monotonic.markNow()
        // 1) 后台线程整批算完（中途输入又变就把这次整个取消掉）。
        val computed: Array<PreviewEntry?> = withContext(Dispatchers.Default) {
            val all = TransformRegistry.all
            val array = arrayOfNulls<PreviewEntry>(all.size)
            for (index in all.indices) {
                if (index and 63 == 0) currentCoroutineContext().ensureActive()
                val transform = all[index]
                val item = TransformItem.of(transform, input)
                array[index] = if (item == null) PreviewEntry(null, "") else PreviewEntry(item, item.output.listPreview())
            }
            array
        }
        // 2) 回到主线程一次性发布：整体换引用再放开版本号，界面只会看到新数据。
        slots = computed
        cachedInput = input
        version++
        println("[PreviewCache] ${computed.size} styles in ${mark.elapsedNow().inWholeMilliseconds}ms")
    }
}