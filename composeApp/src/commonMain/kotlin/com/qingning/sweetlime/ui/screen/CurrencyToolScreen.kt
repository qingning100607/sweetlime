package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.core.i18n.tr
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.i18n.AppLocale
import com.qingning.sweetlime.core.tools.CurrencyApi
import com.qingning.sweetlime.core.tools.CurrencyRates
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.round
import com.qingning.sweetlime.ui.components.GlassEntriesPopup
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem

@Composable
internal fun CurrencyToolScreen(onCopyText: (String, String) -> Unit) {
    var amountText by rememberSaveable { mutableStateOf("100") }
    var fromCode by rememberSaveable { mutableStateOf("CNY") }
    var toCode by rememberSaveable { mutableStateOf("USD") }
    var showFrom by remember { mutableStateOf(false) }
    var showTo by remember { mutableStateOf(false) }
    var rates by remember { mutableStateOf<CurrencyRates?>(null) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        rates = CurrencyApi.fetch("CNY")
        finished = true
    }

    val amount = amountText.trim().toDoubleOrNull()
    val rate = rates?.crossRate(fromCode, toCode)
    val converted = if (amount != null && rate != null) amount * rate else null
    val summary = if (amount != null && converted != null && rate != null) {
        "${formatDecimal(amount, 2)} $fromCode = ${formatDecimal(converted, 2)} $toCode\n" +
            "1 $fromCode = ${formatRate(rate)} $toCode"
    } else {
        ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()

            .overScrollVertical(),
        contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
    ) {
        item {
            Column {

        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = tr("金额"),
            useLabelAsPlaceholder = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            CurrencyPickRow(
                title = tr("从"),
                code = fromCode,
                expanded = showFrom,
                onExpand = { showFrom = true },
                onDismiss = { showFrom = false },
                onPick = {
                    fromCode = it
                    showFrom = false
                },
            )
            CurrencyPickRow(
                title = tr("到"),
                code = toCode,
                expanded = showTo,
                onExpand = { showTo = true },
                onDismiss = { showTo = false },
                onPick = {
                    toCode = it
                    showTo = false
                },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tr("汇率来自 exchangerate-api，换货币不用重新请求。"),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        SmallTitle(modifier = Modifier.offset(x = (-16).dp), text = tr("换算结果"))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                val message = when {
                    !finished -> tr("正在获取汇率…")
                    rates == null -> tr("获取汇率失败，请检查网络后重进本页。")
                    amount == null -> tr("（输入金额后这里会显示结果）")
                    converted == null -> tr("该货币暂时没有汇率数据。")
                    else -> null
                }
                if (message != null) {
                    Text(
                        text = message,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                } else {
                    Text(
                        text = "${formatDecimal(amount!!, 2)} $fromCode = " +
                            "${formatDecimal(converted!!, 2)} $toCode",
                        style = MiuixTheme.textStyles.title2,
                        color = MiuixTheme.colorScheme.onSurfaceContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1 $fromCode = ${formatRate(rate!!)} $toCode",
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            text = tr("复制结果"),
            onClick = { if (summary.isNotEmpty()) onCopyText(summary, tr("汇率换算")) },
            enabled = summary.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
        Spacer(modifier = Modifier.height(48.dp))

            }
        }
    }
}

@Composable
private fun CurrencyPickRow(
    title: String,
    code: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {

    Box(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = title,
            summary = CurrencyApi.displayName(code),
            onClick = {
                                onExpand()
            },
            endActions = {
                Text(
                    text = code,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = MiuixIcons.Basic.ArrowUpDown,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )

        val options = remember(AppLocale.current) { CurrencyApi.COMMON }
        GlassEntriesPopup(
            show = expanded,
            entries = listOf(
                DropdownEntry(
                    items = options.map { entry ->
                        DropdownItem(
                            text = "${entry.first} ${entry.second}",
                            selected = entry.second == code,
                            onClick = { onPick(entry.second) },
                        )
                    },
                ),
            ),
            onDismissRequest = onDismiss,
        )
    }
}

private fun formatDecimal(value: Double, digits: Int): String {
    if (!value.isFinite()) return "—"
    var factor = 1L
    repeat(digits) { factor *= 10 }
    val scaled = round(value * factor).toLong()
    val sign = if (scaled < 0) "-" else ""
    val absValue = abs(scaled)
    val intPart = absValue / factor
    if (digits == 0) return "$sign$intPart"
    val fracPart = absValue % factor
    return "$sign$intPart.${fracPart.toString().padStart(digits, '0')}"
}

private fun formatRate(rate: Double): String {
    if (!rate.isFinite()) return "—"
    val digits = when {
        rate >= 100 -> 2
        rate >= 1 -> 4
        rate >= 0.01 -> 6
        else -> 8
    }
    return formatDecimal(rate, digits)
}
