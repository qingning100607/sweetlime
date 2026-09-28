/*
 * Copyright 2026 Proify, Tomakino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 本文件移植并修改自开源项目 lyricon（https://github.com/kifranei/lyricon），
 * 原实现是 app/src/main/kotlin/io/github/proify/lyricon/app/compose/effect/HyperOsDetector.kt。
 * 按 Apache-2.0 第 4(b) 条声明修改：
 *   1. 去掉「HyperOS 1 不画流光」这一档 —— SweetLime 的开关只有开 / 关两态，
 *      开了就必须看得见效果，跟随系统时一律落到 OS2 或 OS3；
 *   2. 常量收敛成 AUTO / OS2 / OS3 三个；
 *   3. 本文件在 commonMain，不能直接 import android.os.*，全部改走反射。
 */
package com.qingning.sweetlime.ui.effect

/**
 * HyperOS 大版本探测：决定「流光背景」用哪一代观感。
 *
 * - HyperOS 2 → OS2 观感（配色固定，四个光点自己绕圈漂移）
 * - HyperOS 3 及以上 / 非小米设备 / 读不到版本 → OS3 观感（配色随时间在三组色板间流动）
 */
object HyperOsStyle {
    /** 跟随系统：按 HyperOS 大版本自动选。 */
    const val AUTO: Int = 0

    /** 强制 OS2 观感。 */
    const val OS2: Int = 1

    /** 强制 OS3 观感。 */
    const val OS3: Int = 2

    private val XIAOMI_BRANDS = setOf("xiaomi", "redmi", "poco")

    private fun systemProperty(key: String): String = runCatching {
        val clazz = Class.forName("android.os.SystemProperties")
        val get = clazz.getMethod("get", String::class.java, String::class.java)
        (get.invoke(null, key, "") as? String).orEmpty()
    }.getOrDefault("")

    private fun buildField(name: String): String = runCatching {
        val clazz = Class.forName("android.os.Build")
        (clazz.getField(name).get(null) as? String).orEmpty()
    }.getOrDefault("")

    private fun isXiaomiDevice(): Boolean {
        val maker = buildField("MANUFACTURER").lowercase()
        val brand = buildField("BRAND").lowercase()
        return maker in XIAOMI_BRANDS || brand in XIAOMI_BRANDS
    }

    /** HyperOS 主版本号，例如 "OS2.0.5.0" → 2；非小米设备或读不到属性时返回 null。 */
    fun hyperOsMajorVersion(): Int? {
        if (!isXiaomiDevice()) return null
        val raw = systemProperty("ro.mi.os.version.name")
            .ifBlank { systemProperty("ro.build.version.incremental") }
        if (raw.isBlank()) return null
        return Regex("""OS(\d+)""", RegexOption.IGNORE_CASE)
            .find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    /**
     * 把设置里的偏好值解析成「实际用不用 OS3 观感」。
     *
     * 手动选了 OS2 / OS3 就无视系统版本；[AUTO] 时 HyperOS 2 走 OS2，其余全走 OS3。
     */
    fun resolveIsOs3(style: Int): Boolean = when (style) {
        OS2 -> false
        OS3 -> true
        else -> hyperOsMajorVersion() != 2
    }
}