package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr
/** 工具箱里的一个功能。 */
class ToolEntry(
    val id: String,
    val title: String,
    val summary: String,
)

/**
 * 工具箱目录（顺序即列表顺序）。
 *
 * 放在 core 而不是工具页里，是为了让**搜索**也能用它：
 * 搜「汇率」「Base64」应当直接给出工具入口，而不是只能在工具箱里翻。
 */
val TOOL_ENTRIES: List<ToolEntry>
    get() = listOf(
    ToolEntry("symbol", tr("特殊符号"), tr("44 个分类、2797 个符号，点一下即复制")),
    ToolEntry("pinyin", tr("汉字拼音"), tr("整句转带声调拼音：你好世界 → nǐ hǎo shì jiè")),
    ToolEntry("chai", tr("汉字拆分"), tr("把汉字拆成部件：卧项功 → 臣卜工页工力")),
    ToolEntry("daxie", tr("数字大写"), tr("金额「小写 ↔ 大写」双向转换，自动补整")),
    ToolEntry("base64", tr("Base64 编解码"), tr("文字 ↔ Base64 双向转换，UTF-8")),
    ToolEntry("md5", tr("MD5 计算"), tr("文字算 32 位 MD5，附大写与 16 位形式")),
    ToolEntry("sniff", tr("资源嗅探"), tr("输入网页地址，列出页面引用的图片 / 样式 / 脚本等资源")),
    ToolEntry("url", tr("URL 编解码"), tr("文字 ↔ URL 百分号编码（%XX）双向转换")),
    ToolEntry("radix", tr("进制转换"), tr("2–36 进制任意互转，一次给出二 / 八 / 十 / 十六进制")),
    ToolEntry("color", tr("颜色代码转换"), tr("#FF5722 ↔ rgb / hsl，附色块预览")),
    ToolEntry("currency", tr("汇率计算"), tr("常用货币实时换算")),
    ToolEntry("bmi", tr("身体 BMI"), tr("身高体重算 BMI，附健康体重范围")),
    ToolEntry("level", tr("水平仪"), tr("重力感应看水平，气泡居中即平，可校准归零")),
    ToolEntry("editor", tr("代码编辑器"), tr("等宽字体 + 行号，写代码 / 长文本用")),
)
