
package com.qingning.sweetlime.ui.effect

object HyperOsStyle {

    const val AUTO: Int = 0

    const val OS2: Int = 1

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

    fun hyperOsMajorVersion(): Int? {
        if (!isXiaomiDevice()) return null
        val raw = systemProperty("ro.mi.os.version.name")
            .ifBlank { systemProperty("ro.build.version.incremental") }
        if (raw.isBlank()) return null
        return Regex("""OS(\d+)""", RegexOption.IGNORE_CASE)
            .find(raw)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    fun resolveIsOs3(style: Int): Boolean = when (style) {
        OS2 -> false
        OS3 -> true
        else -> hyperOsMajorVersion() != 2
    }
}