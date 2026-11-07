package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr

enum class StyleGroup(private val key: String) {
    LATIN("英文花体"),
    SUPSUB("上下标"),
    COLOR("彩色字母"),
    OVERLAY("叠加符号"),
    SYMBOL("特效符号"),
    HUAYANG("网名"),
    WING("翅膀装饰"),
    VINE("花藤字"),
    BLANK("空白字符"),
    ;

    val label: String get() = tr(key)
}

interface TextTransform {
    val id: String
    val title: String
    val group: StyleGroup

    fun transform(input: String): String
}

internal fun transform(
    id: String,
    title: String,
    group: StyleGroup,
    block: (String) -> String,
) = object : TextTransform {
    override val id: String = id

    private val titleKey: String = title

    override val title: String get() = tr(titleKey)

    override val group: StyleGroup = group

    override fun transform(input: String): String = block(input)
}
