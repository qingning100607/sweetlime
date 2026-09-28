package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform

/**
 * 「装饰模板」样式：在原文前、以及每个字符之间 / 文字之后插入固定图案。
 *
 * 渲染规则 = prefix + c1 + between + c2 + ... + cn + suffix
 * 数据全部由 qiwangming.com 各工具页（特效 / 花样 / 翅膀 / 花藤）的模板反推得到。
 * 新建模板只需要在下面的列表里加一行。
 */
internal class TemplateStyle(
    override val id: String,
    override val title: String,
    override val group: StyleGroup,
    private val prefix: String,
    private val between: String,
    private val suffix: String,
) : TextTransform {
    override fun transform(input: String): String = buildString(input.length * 3) {
        append(prefix)
        var first = true
        input.forEach { ch ->
            if (!first) append(between)
            append(ch)
            first = false
        }
        append(suffix)
    }
}

/** 特效符号（对齐「特效网名」32 种）。 */
internal val symbolStyles: List<TextTransform> = listOf(
    TemplateStyle("tx_1", "删除线", StyleGroup.SYMBOL, "", "̶", "̶"),
    TemplateStyle("tx_2", "上划线", StyleGroup.SYMBOL, "", "̄", "̄"),
    TemplateStyle("tx_3", "下划线", StyleGroup.SYMBOL, "", "꯭", "꯭"),
    TemplateStyle("tx_4", "一根毛1", StyleGroup.SYMBOL, "", "", " ༽"),
    TemplateStyle("tx_5", "一根毛2", StyleGroup.SYMBOL, "", "", " ༼"),
    TemplateStyle("tx_6", "菱形文", StyleGroup.SYMBOL, "", "⃟", "⃟"),
    TemplateStyle("tx_7", "禁止字", StyleGroup.SYMBOL, "", "⃠", "⃠"),
    TemplateStyle("tx_8", "三角形文字", StyleGroup.SYMBOL, "", "⃤", "⃤"),
    TemplateStyle("tx_9", "爱心文1", StyleGroup.SYMBOL, "", "ꦿ᭄", "ꦿ᭄"),
    TemplateStyle("tx_10", "爱心文2", StyleGroup.SYMBOL, "", "໌້ᮨ", "໌້ᮨ"),
    TemplateStyle("tx_11", "花藤字1", StyleGroup.SYMBOL, "ζั͡", "ั͡", "ั͡✾ ั"),
    TemplateStyle("tx_12", "花藤字2", StyleGroup.SYMBOL, "ζั͡ ", "ζั͡ ", "ζั͡✿"),
    TemplateStyle("tx_13", "花藤字3", StyleGroup.SYMBOL, "ζั͡ ", "ζั͡ ", "ζั͡❀"),
    TemplateStyle("tx_14", "雪花文", StyleGroup.SYMBOL, "", "⃰", "⃰"),
    TemplateStyle("tx_15", "笑脸文", StyleGroup.SYMBOL, "", "̆̈", "̆̈"),
    TemplateStyle("tx_16", "菊花文1", StyleGroup.SYMBOL, "", "҉", "҉"),
    TemplateStyle("tx_17", "菊花文2", StyleGroup.SYMBOL, "", "҈", "҈"),
    TemplateStyle("tx_18", "菊花文3", StyleGroup.SYMBOL, "", "꙰", "꙰"),
    TemplateStyle("tx_19", "菊花文4", StyleGroup.SYMBOL, "", "꙲", "꙲"),
    TemplateStyle("tx_20", "波浪纹", StyleGroup.SYMBOL, "", "͜", "͜"),
    TemplateStyle("tx_21", "飞鸟文1", StyleGroup.SYMBOL, "", "ོ", "ོ"),
    TemplateStyle("tx_22", "飞鸟文2", StyleGroup.SYMBOL, "", "ཽ", "ཽ"),
    TemplateStyle("tx_23", "蝴蝶文", StyleGroup.SYMBOL, "", "ིྀ", "ིྀ"),
    TemplateStyle("tx_24", "发卡文", StyleGroup.SYMBOL, "", "݉", "݉"),
    TemplateStyle("tx_25", "萌芽文", StyleGroup.SYMBOL, "", "็้", "็้"),
    TemplateStyle("tx_26", "单横线", StyleGroup.SYMBOL, "", "̶", "̶"),
    TemplateStyle("tx_27", "双横线", StyleGroup.SYMBOL, "", "͇", "͇"),
    TemplateStyle("tx_28", "斜线", StyleGroup.SYMBOL, "", "̷", "̷"),
    TemplateStyle("tx_29", "尾巴文", StyleGroup.SYMBOL, "", "༘", "༘"),
    TemplateStyle("tx_30", "冒烟文1", StyleGroup.SYMBOL, "", "ྂ", "ྂ"),
    TemplateStyle("tx_31", "冒烟文2", StyleGroup.SYMBOL, "", "้้้้้้", "้้้้้้"),
    TemplateStyle("tx_32", "冒烟文3", StyleGroup.SYMBOL, "", "ۣۣۣۣۣۣۣ", "ۣۣۣۣۣۣۣ"),
)

/** 花样网名（对齐「花样网名」72 种）。 */
internal val huayangStyles: List<TextTransform> = listOf(
    TemplateStyle("hy_1", "ネф̶ イω ᥬᥬ", StyleGroup.HUAYANG, "ネф̶ イω ᥬᥬ", "", ""),
    TemplateStyle("hy_2", "ᨏ҉ᨏ ོ", StyleGroup.HUAYANG, "ᨏ҉", "", "ᨏ ོ"),
    TemplateStyle("hy_3", "ꦿ໊ོﻬ°", StyleGroup.HUAYANG, "", "", "ꦿ໊ོﻬ°"),
    TemplateStyle("hy_4", "ღ້໌࿐", StyleGroup.HUAYANG, "ღ", "", "້໌࿐"),
    TemplateStyle("hy_5", "დ᭄ꦿ꧔ꦿ℘", StyleGroup.HUAYANG, "დ᭄ꦿ", "", "꧔ꦿ℘"),
    TemplateStyle("hy_6", "ღ᭄ꦿ⁵²º᭄", StyleGroup.HUAYANG, "ღ᭄ꦿ", "", "⁵²º᭄"),
    TemplateStyle("hy_7", "Aî₯㎕.จุ๊บ", StyleGroup.HUAYANG, "Aî₯㎕.", "", "จุ๊บ"),
    TemplateStyle("hy_8", "᭄ꫛꪝ、", StyleGroup.HUAYANG, "᭄ꫛꪝ、", "", ""),
    TemplateStyle("hy_9", "ღ້໌ᮩꦿ᭄࿐", StyleGroup.HUAYANG, "ღ", "້໌ᮩ", "ꦿ᭄࿐"),
    TemplateStyle("hy_10", "ৡᮨོꦿ࿐", StyleGroup.HUAYANG, "ৡ", "ᮨ", "ོꦿ࿐"),
    TemplateStyle("hy_11", "ঞ᭄ꦿ༊", StyleGroup.HUAYANG, "ঞ᭄", "", "ꦿ༊"),
    TemplateStyle("hy_12", "এ⁵²º᭄", StyleGroup.HUAYANG, "", "", "এ⁵²º᭄"),
    TemplateStyle("hy_13", "༄྄ེིོུཉི࿔࿆࿐ོ", StyleGroup.HUAYANG, "༄྄ེིོུ", "", "ཉི࿔࿆࿐ོ"),
    TemplateStyle("hy_14", "⋆᭄͡ꦿ໌້۵ৡ࿐", StyleGroup.HUAYANG, "⋆᭄͡ꦿ໌້۵", "", "ৡ࿐"),
    TemplateStyle("hy_15", "꧁꫞࿅࿆꫞꧂", StyleGroup.HUAYANG, "꧁꫞", "࿅࿆", "꫞꧂"),
    TemplateStyle("hy_16", "꧁꫞꯭꯭꯭꫞꧂", StyleGroup.HUAYANG, "꧁꫞꯭", "꯭", "꯭꫞꧂"),
    TemplateStyle("hy_17", "༺❦❦༻", StyleGroup.HUAYANG, "༺❦", "", "❦༻"),
    TemplateStyle("hy_18", "࿐ཉི༗࿆༗࿆ཉི࿐", StyleGroup.HUAYANG, "࿐ཉི༗࿆", "", "༗࿆ཉི࿐"),
    TemplateStyle("hy_19", "༺།༼࿄࿆࿅࿆༽།༻", StyleGroup.HUAYANG, "༺།༼࿄࿆", "", "࿅࿆༽།༻"),
    TemplateStyle("hy_20", "꧁༺❀ൢൢ❀༻꧂", StyleGroup.HUAYANG, "꧁༺❀ൢ", "", "ൢ❀༻꧂"),
    TemplateStyle("hy_21", "꧁꧂", StyleGroup.HUAYANG, "꧁", "", "꧂"),
    TemplateStyle("hy_22", "꧁꫞꯭꯭꯭꫞꧂", StyleGroup.HUAYANG, "꧁꫞꯭", "꯭", "꯭꫞꧂"),
    TemplateStyle("hy_23", "꧁꫞࿅࿆꫞꧂", StyleGroup.HUAYANG, "꧁꫞", "࿅࿆", "꫞꧂"),
    TemplateStyle("hy_24", "༺༃★༃༻", StyleGroup.HUAYANG, "༺༃", "★", "༃༻"),
    TemplateStyle("hy_25", "༺ཌༀൢൢༀད༻", StyleGroup.HUAYANG, "༺ཌༀൢ", "", "ൢༀད༻"),
    TemplateStyle("hy_26", "ℳ๓°ꦿ⁵²º᭄", StyleGroup.HUAYANG, "ℳ๓", "", "°ꦿ⁵²º᭄"),
    TemplateStyle("hy_27", "⋆᭄͡ꦿ໌້۵ꦿ໌", StyleGroup.HUAYANG, "⋆᭄͡ꦿ໌້۵", "", "ꦿ໌"),
    TemplateStyle("hy_28", "ζั͡ั͡✾", StyleGroup.HUAYANG, "ζั͡", "", "ั͡✾"),
    TemplateStyle("hy_29", "♡̶̶̶", StyleGroup.HUAYANG, "♡̶", "̶", "̶"),
    TemplateStyle("hy_30", "ℒℴѵℯএ⁵²º᭄এ", StyleGroup.HUAYANG, "ℒℴѵℯ", "", "এ⁵²º᭄এ"),
    TemplateStyle("hy_31", "୧⍤⃝sw", StyleGroup.HUAYANG, "୧⍤⃝sw", "", ""),
    TemplateStyle("hy_32", "ᝰ້໌ᮨړ࿐", StyleGroup.HUAYANG, "ᝰ", "", "້໌ᮨړ࿐"),
    TemplateStyle("hy_33", "༄Kⅈꫛᧁ࿐", StyleGroup.HUAYANG, "༄Kⅈꫛᧁ࿐", "", ""),
    TemplateStyle("hy_34", "ꪜiρ⁰¹", StyleGroup.HUAYANG, "ꪜiρ⁰¹", "", ""),
    TemplateStyle("hy_35", "꯭꯭ꦿ᭄", StyleGroup.HUAYANG, "", "꯭", "꯭ꦿ᭄"),
    TemplateStyle("hy_36", "じ☆veう", StyleGroup.HUAYANG, "じ☆ve", "", "う"),
    TemplateStyle("hy_37", "⁵²º⅓¼", StyleGroup.HUAYANG, "", "", "⁵²º⅓¼"),
    TemplateStyle("hy_38", "҉҉✘", StyleGroup.HUAYANG, "", "҉", "҉✘"),
    TemplateStyle("hy_39", "ⴾⵈⵂⵘⵗⵈⴾ", StyleGroup.HUAYANG, "ⴾⵈⵂ", "ⵘ", "ⵗⵈⴾ"),
    TemplateStyle("hy_40", "ᥬ᭄ʚïɞᥬ᭄", StyleGroup.HUAYANG, "ᥬ", "᭄ʚïɞᥬ", "᭄"),
    TemplateStyle("hy_41", "ི༷ïྀི༷༷ïྀ༷", StyleGroup.HUAYANG, "ི༷ïྀ༷", "", "ི༷ïྀ༷"),
    TemplateStyle("hy_42", "࿙ʚ⃝Ⳃ⃝ɞ࿚", StyleGroup.HUAYANG, "࿙ʚ", "⃝Ⳃ", "⃝ɞ࿚"),
    TemplateStyle("hy_43", "ℳ₯㎕﹅ゝ", StyleGroup.HUAYANG, "ℳ₯㎕﹅", "", "ゝ"),
    TemplateStyle("hy_44", "༄ོོ࿆࿆༅", StyleGroup.HUAYANG, "༄", "ོ࿆", "ོ࿆༅"),
    TemplateStyle("hy_45", "ོ͡ꦿ᭄", StyleGroup.HUAYANG, "", "ོ", "͡ꦿ᭄"),
    TemplateStyle("hy_46", "చ꯭꯭꯭⃧⃧", StyleGroup.HUAYANG, "చ꯭", "꯭⃧", "꯭⃧"),
    TemplateStyle("hy_47", "̶̶͠็ꦿﻬ", StyleGroup.HUAYANG, "", "̶͠", "̶็ꦿﻬ"),
    TemplateStyle("hy_48", "༺ۣۣۖۖ༒ۣۣۖ༻", StyleGroup.HUAYANG, "༺ۣۖ", "ۣۖ༒ۣ", "ۣۖ༻"),
    TemplateStyle("hy_49", "ꦿ༻", StyleGroup.HUAYANG, "", "ꦿ", "༻"),
    TemplateStyle("hy_50", "ོ͜❀҉", StyleGroup.HUAYANG, "", "ོ", "͜❀҉"),
    TemplateStyle("hy_51", "້໌ᮩ̶͡ꦿ̶̶᭄͡ꦿ̶̶᭄→", StyleGroup.HUAYANG, "", "້໌ᮩ", "̶͡ꦿ̶̶᭄͡ꦿ̶̶᭄→"),
    TemplateStyle("hy_52", "ʚ̶̶̶͜م", StyleGroup.HUAYANG, "ʚ", "̶", "̶̶͜م"),
    TemplateStyle("hy_53", "এ゛ꦿོ࿐", StyleGroup.HUAYANG, "এ゛", "", "ꦿོ࿐"),
    TemplateStyle("hy_54", "☾⋆꯭᭄ึູ♡̶ꦿོ࿐", StyleGroup.HUAYANG, "☾⋆꯭᭄", "ึູ♡̶", "ꦿོ࿐ "),
    TemplateStyle("hy_55", "ꦿ໊ོﻬꦿ໊ོﻬ", StyleGroup.HUAYANG, "", "ꦿ໊ོﻬ", "ꦿ໊ོﻬ"),
    TemplateStyle("hy_56", "এོོ༊", StyleGroup.HUAYANG, "এ", "ོ", "ོ༊"),
    TemplateStyle("hy_57", "☾⋆᭄໌້ᮨ໌້ᮨ⋆", StyleGroup.HUAYANG, "☾⋆᭄", "໌້ᮨ", "໌້ᮨ⋆"),
    TemplateStyle("hy_58", "ཻཻ࿐", StyleGroup.HUAYANG, "", "ཻ", "ཻ࿐"),
    TemplateStyle("hy_59", "ʚ̶̶̶͜م", StyleGroup.HUAYANG, "ʚ", "̶", "̶̶͜م"),
    TemplateStyle("hy_60", "༗ཻཻ࿐", StyleGroup.HUAYANG, "༗", "ཻ", "ཻ࿐"),
    TemplateStyle("hy_61", "ℒฺℴฺνℯ̶̶̶ฺ⋆", StyleGroup.HUAYANG, "ℒฺℴฺνℯ̶ฺ", "̶", "̶⋆"),
    TemplateStyle("hy_62", "⸐꯭꯭꯭ོ༙ۣ྇ྃم", StyleGroup.HUAYANG, "⸐꯭", "꯭༙྇", "꯭ོۣྃم"),
    TemplateStyle("hy_63", "ζั͡ޓއؓ↭ཱིؓއއ", StyleGroup.HUAYANG, "ζั͡ޓއؓ", "↭ؓ", "ཱིއއ"),
    TemplateStyle("hy_64", "༄ོྂཾ࿆༊࿆࿆ྂ࿐", StyleGroup.HUAYANG, "༄", "ོྂཾ࿆༊࿆", "࿆ྂ࿐"),
    TemplateStyle("hy_65", "༺͈͈̎̎༻", StyleGroup.HUAYANG, "༺", "͈̎", "͈̎༻"),
    TemplateStyle("hy_66", "೭ᘏᙧᙧᘏ೨", StyleGroup.HUAYANG, "೭ᘏᙧ", "", "ᙧᘏ೨"),
    TemplateStyle("hy_67", "ঞꕥ᭄ꦿ⁵²⁰ꕥ", StyleGroup.HUAYANG, "ঞꕥ᭄ꦿ", "", "⁵²⁰ꕥ"),
    TemplateStyle("hy_68", "༺༒˶⍤⃝˶ᵒ༻", StyleGroup.HUAYANG, "༺༒", "", "˶⍤⃝˶ᵒ༻"),
    TemplateStyle("hy_69", "✨⃢⃢⃢✨", StyleGroup.HUAYANG, "✨⃢", "⃢", "⃢✨"),
    TemplateStyle("hy_70", "/ ⃢—⃢ \\", StyleGroup.HUAYANG, "/　⃢", "—", "⃢　\\"),
    TemplateStyle("hy_71", "୧⍤⃝🔥", StyleGroup.HUAYANG, "୧⍤⃝🔥", "", ""),
    TemplateStyle("hy_72", ".💤", StyleGroup.HUAYANG, "", "", ".💤"),
)

/** 翅膀装饰（对齐「翅膀网名」36 种）。 */
internal val wingStyles: List<TextTransform> = listOf(
    TemplateStyle("cb_1", "꧁꧂", StyleGroup.WING, "꧁", "", "꧂"),
    TemplateStyle("cb_2", "꧁༺༻꧂", StyleGroup.WING, "꧁༺", "", "༻꧂"),
    TemplateStyle("cb_3", "꧁༺๑๑༻꧂", StyleGroup.WING, "꧁༺๑", "", "๑༻꧂"),
    TemplateStyle("cb_4", "꧁꫞࿅࿆꫞꧂", StyleGroup.WING, "꧁꫞", "࿅࿆", "꫞꧂"),
    TemplateStyle("cb_5", "꧁꫞꯭꯭꯭꫞꧂", StyleGroup.WING, "꧁꫞꯭", "꯭", "꯭꫞꧂"),
    TemplateStyle("cb_6", "༺ཌༀൢൢༀད༻", StyleGroup.WING, "༺ཌༀൢ", "", "ൢༀད༻"),
    TemplateStyle("cb_7", "꧁༺△△༻꧂", StyleGroup.WING, "꧁༺△", "", "△༻꧂"),
    TemplateStyle("cb_8", "꧁❦༺༻❦꧂", StyleGroup.WING, "꧁❦༺", "", "༻❦꧂"),
    TemplateStyle("cb_9", "꧁༺༽༾ཊཏ༿༼༻꧂", StyleGroup.WING, "꧁༺༽༾ཊ", "", "ཏ༿༼༻꧂"),
    TemplateStyle("cb_10", "꧁༺❀ൢൢ❀༻꧂", StyleGroup.WING, "꧁༺❀ൢ", "", "ൢ❀༻꧂"),
    TemplateStyle("cb_11", "꧁༺༻꧂", StyleGroup.WING, "꧁༺", "", "༻꧂"),
    TemplateStyle("cb_12", "꧁❀❀꧂", StyleGroup.WING, "꧁❀", "", "❀꧂"),
    TemplateStyle("cb_13", "ༀ꧁꫞꫞꧂ༀ", StyleGroup.WING, "ༀ꧁꫞", "", "꫞꧂ༀ"),
    TemplateStyle("cb_14", "࿐ཉི༗࿆༗࿆ཉི࿐", StyleGroup.WING, "࿐ཉི༗࿆", "", "༗࿆ཉི࿐"),
    TemplateStyle("cb_15", "༺༃༃༻", StyleGroup.WING, "༺༃", "", "༃༻"),
    TemplateStyle("cb_16", "༺།༼࿄࿆࿅࿆༽།༻", StyleGroup.WING, "༺།༼࿄࿆", "", "࿅࿆༽།༻"),
    TemplateStyle("cb_17", "༺༻", StyleGroup.WING, "༺", "", "༻"),
    TemplateStyle("cb_18", "࿐ཉི༗࿆ ༗࿆ཉི࿐", StyleGroup.WING, "࿐ཉི༗࿆", "", " ༗࿆ཉི࿐"),
    TemplateStyle("cb_19", "༺༒༒༻", StyleGroup.WING, "༺༒", "", "༒༻"),
    TemplateStyle("cb_20", "༺ཌༀཉི༃ༀད༻", StyleGroup.WING, "༺ཌༀཉི", "", "༃ༀད༻"),
    TemplateStyle("cb_21", "༺ཉི།།ཉྀ༻", StyleGroup.WING, "༺ཉི།", "", "།ཉྀ༻"),
    TemplateStyle("cb_22", "༺❀ൢ༒ൢ❀༻", StyleGroup.WING, "༺❀ൢ", "༒", "ൢ❀༻"),
    TemplateStyle("cb_23", "༺༃★༃༻", StyleGroup.WING, "༺༃", "★", "༃༻"),
    TemplateStyle("cb_24", "༺࿈࿈༻", StyleGroup.WING, "༺࿈", "", "࿈༻"),
    TemplateStyle("cb_25", "༺༽༾ཊཏ༿༼༻", StyleGroup.WING, "༺༽༾ཊ", "", "ཏ༿༼༻"),
    TemplateStyle("cb_26", "༺ཌ༈༈ད༻", StyleGroup.WING, "༺ཌ༈", "", "༈ད༻"),
    TemplateStyle("cb_27", "༄༊ོྂཾ࿆࿐", StyleGroup.WING, "༄༊", "", "ོྂཾ࿆࿐"),
    TemplateStyle("cb_28", "༺ཌༀༀད༻", StyleGroup.WING, "༺ཌༀ", "", "ༀད༻"),
    TemplateStyle("cb_29", "༺❦❦༻", StyleGroup.WING, "༺❦", "", "❦༻"),
    TemplateStyle("cb_30", "︶﹌⋛⋚﹌︶", StyleGroup.WING, "︶﹌⋛", "", "⋚﹌︶"),
    TemplateStyle("cb_31", "ʚɞ", StyleGroup.WING, "ʚ", "", "ɞ"),
    TemplateStyle("cb_32", "εз", StyleGroup.WING, "ε", "", "з"),
    TemplateStyle("cb_33", "ઇଓ", StyleGroup.WING, "ઇ", "", "ଓ"),
    TemplateStyle("cb_34", "⊱⊰", StyleGroup.WING, "⊱", "", "⊰"),
    TemplateStyle("cb_35", "◥◤", StyleGroup.WING, "◥", "", "◤"),
    TemplateStyle("cb_36", "☜☞", StyleGroup.WING, "☜", "", "☞"),
)

/** 花藤字（对齐「花藤字」3 种）。 */
internal val vineStyles: List<TextTransform> = listOf(
    TemplateStyle("ht_1", "花藤字1", StyleGroup.VINE, "ζั͡", "ั͡", "ั͡✾ ั"),
    TemplateStyle("ht_2", "花藤字2", StyleGroup.VINE, "ζั͡ ", "ζั͡ ", "ζั͡✿"),
    TemplateStyle("ht_3", "花藤字3", StyleGroup.VINE, "ζั͡ ", "ζั͡ ", "ζั͡❀"),
)
