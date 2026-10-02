package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.lazy.rememberLazyListState
import top.yukonga.miuix.kmp.utils.overScrollVertical
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.Doc
import com.qingning.sweetlime.ui.components.GlassTopBarScaffold
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 纯文字说明页（隐私政策 / 开源许可共用）。
 *
 * 每段一个加粗小标题 + 一段正文；顶栏用二级页统一的柔光玻璃外壳。
 */
@Composable
fun DocScreen(
    title: String,
    sections: List<Doc>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassTopBarScaffold(title = title, onBack = onBack, modifier = modifier) { topPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .hyperScrollHaptic(),
            contentPadding = PaddingValues(top = topPadding + 8.dp, bottom = 48.dp),
        ) {
            item(key = "doc_body") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        sections.forEachIndexed { index, doc ->
                            if (index != 0) Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = doc.title,
                                style = MiuixTheme.textStyles.main,
                                color = MiuixTheme.colorScheme.onSurfaceContainer,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = doc.body,
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}