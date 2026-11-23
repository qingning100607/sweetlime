package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.LibraryLicense
import com.qingning.sweetlime.ui.components.GlassTopBarScaffold
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun LicensesScreen(
    title: String,
    libraries: List<LibraryLicense>,
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassTopBarScaffold(title = title, onBack = onBack, modifier = modifier) { topPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = topPadding + 8.dp,
                bottom = 48.dp,
            ),
        ) {
            items(items = libraries, key = { it.name }) { library ->
                val indication = LocalIndication.current
                val url = library.url
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val pressedBackground = if (pressed && !url.isNullOrBlank()) {
                    MiuixTheme.colorScheme.onSurfaceContainer.copy(alpha = 0.06f)
                } else {
                    Color.Transparent
                }
                val clickable = if (url.isNullOrBlank()) {
                    Modifier
                } else {
                    Modifier.clickable(
                        indication = indication,
                        interactionSource = interactionSource,
                        onClick = { onOpenUrl(url) },
                    )
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(pressedBackground)
                            .then(clickable)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = library.name,
                                modifier = Modifier.weight(1f),
                                style = MiuixTheme.textStyles.main,
                                color = MiuixTheme.colorScheme.onSurfaceContainer,
                            )
                            val version = library.artifactVersion
                            if (!version.isNullOrEmpty()) {
                                Text(
                                    text = version,
                                    modifier = Modifier.padding(start = 16.dp),
                                    style = MiuixTheme.textStyles.footnote1,
                                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                        val developers = library.developers
                        if (!developers.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = developers,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary,
                            )
                        }
                        val description = library.description
                        if (!description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = description,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        val licenses = library.licenses
                        if (!licenses.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = licenses,
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}