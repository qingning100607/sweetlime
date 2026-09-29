package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr

/**
 * 样式分组，决定在界面上如何归类展示（顺序即分类 Tab 的先后）。
 *
 * [key] 是简体原文（同时也是词表里的 key），[label] 按当前语言现算——
 * 所以这里不能用构造参数直接存 label，否则枚举初始化时就把语言定死了。
 */
enum class StyleGroup(private val key: String) {
    LATIN("英文花体"),
    SUPSUB("上下标"),
    COLOR("彩色字母"),
    OVERLAY("叠加符号"),
    SYMBOL("特效符号"),
    HUAYANG("花样网名"),
    WING("翅膀装饰"),
    VINE("花藤字"),
    BIRD("飞鸟文"),
    BLANK("空白字符"),
    ;

    val label: String get() = tr(key)
}

/**
 * 一种文字样式。实现必须是**纯函数**：同样的输入永远得到同样的输出。
 * 新增样式只需要实现该接口并注册到 [TransformRegistry]。
 */
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

    /**
     * 传进来的 [title] 是**简体原文**（词表 key），不是已经翻译好的文案。
     *
     * 翻译必须推迟到读取时做：样式表一旦被提前构造（[TransformRegistry.all] 就是），
     * 标题会被定死成构造那一刻的语言，之后切语言就不再跟着变。
     * 这和 [StyleGroup.label] 是同一个坑，那边早已避开。
     */
    private val titleKey: String = title

    override val title: String get() = tr(titleKey)

    override val group: StyleGroup = group

    override fun transform(input: String): String = block(input)
}
