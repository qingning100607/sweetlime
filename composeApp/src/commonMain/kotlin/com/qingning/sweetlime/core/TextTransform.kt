package com.qingning.sweetlime.core

/** 样式分组，决定在界面上如何归类展示（顺序即分类 Tab 的先后）。 */
enum class StyleGroup(val label: String) {
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
    override val title: String = title
    override val group: StyleGroup = group
    override fun transform(input: String): String = block(input)
}
