package com.qingning.sweetlime.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.time.TimeSource

class PreviewEntry(val item: TransformItem?, val preview: String)

object PreviewCache {

    private const val NONE = "\u0000"

    private var cachedInput: String = NONE

    private var slots: Array<PreviewEntry?> = arrayOfNulls(TransformRegistry.all.size)

    var version by mutableIntStateOf(0)
        private set

    fun isReady(input: String): Boolean = cachedInput == input

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

    suspend fun prefetch(input: String) {
        if (input == cachedInput) return
        val mark = TimeSource.Monotonic.markNow()

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

        slots = computed
        cachedInput = input
        version++
        println("[PreviewCache] ${computed.size} styles in ${mark.elapsedNow().inWholeMilliseconds}ms")
    }
}