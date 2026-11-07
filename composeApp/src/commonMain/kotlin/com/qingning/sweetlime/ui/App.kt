package com.qingning.sweetlime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.platform.LocalHapticFeedback

import com.qingning.sweetlime.ui.effect.rememberHyperHapticFeedback

import com.qingning.sweetlime.core.i18n.AppLanguage
import com.qingning.sweetlime.core.i18n.AppLocale
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.runtime.SideEffect
import kotlinx.coroutines.Job
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import androidx.compose.foundation.pager.PagerDefaults
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerNavigationSpringSpec
import top.yukonga.miuix.kmp.utils.springAnimateToPage
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.BackAction
import com.qingning.sweetlime.core.PreviewCache
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.resolveBackAction
import com.qingning.sweetlime.core.UpdateChecker
import com.qingning.sweetlime.ui.screen.UpdateNotesDialog
import com.qingning.sweetlime.core.OpenSourceLicenses
import com.qingning.sweetlime.core.PrivacyPolicy
import com.qingning.sweetlime.core.copyToClipboard
import com.qingning.sweetlime.core.createKeyValueStore
import com.qingning.sweetlime.core.openUrl
import com.qingning.sweetlime.core.shareText
import com.qingning.sweetlime.data.FavoritesStore
import com.qingning.sweetlime.data.RecentStore
import com.qingning.sweetlime.data.SweetLimeSettings
import com.qingning.sweetlime.ui.components.FloatingBottomBar
import com.qingning.sweetlime.ui.components.FloatingBottomBarItem
import com.qingning.sweetlime.ui.components.glassBar
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.qingning.sweetlime.ui.effect.BgEffectBackground
import com.qingning.sweetlime.ui.effect.FlowingSurface
import com.qingning.sweetlime.ui.effect.flowingPageLayer
import com.qingning.sweetlime.ui.effect.glassBarBackdrop
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import com.qingning.sweetlime.ui.components.rememberTabPagerState
import com.qingning.sweetlime.ui.effect.HyperHaptics
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.nav.Route
import com.qingning.sweetlime.ui.screen.AboutScreen
import com.qingning.sweetlime.ui.screen.DetailScreen
import com.qingning.sweetlime.ui.screen.DocScreen
import com.qingning.sweetlime.ui.screen.FavoriteScreen
import com.qingning.sweetlime.ui.screen.GroupScreen
import com.qingning.sweetlime.ui.screen.HomeScreen
import com.qingning.sweetlime.ui.screen.SearchScreen
import com.qingning.sweetlime.ui.screen.SettingsScreen
import com.qingning.sweetlime.ui.screen.SymbolCategoryScreen
import com.qingning.sweetlime.ui.screen.ToolScreen
import com.qingning.sweetlime.ui.screen.ToolsScreen
import com.qingning.sweetlime.ui.theme.SweetLimeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.effect
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ConvertFile
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.LocalOverScrollState
import top.yukonga.miuix.kmp.utils.MiuixOverscrollFactory
import top.yukonga.miuix.kmp.utils.OverScrollState
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun SweetLimeApp() {
    val store = remember { createKeyValueStore() }
    val settings = remember(store) { SweetLimeSettings(store) }

    HyperHaptics.enabled = settings.hapticFeedback
    val favorites = remember(store) { FavoritesStore(store) }
    val recent = remember(store) { RecentStore(store) }

    val languageCode = settings.language
    SideEffect { AppLocale.applyStored(languageCode) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(input) {
        if (input.isBlank()) return@LaunchedEffect
        delay(120)
        PreviewCache.prefetch(input)
    }

    var updateResult by remember { mutableStateOf<UpdateChecker.Result?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val newerRelease = updateResult as? UpdateChecker.Result.Newer

    fun openUpdateDownload() {
        val newer = updateResult as? UpdateChecker.Result.Newer ?: return
        openUrl(newer.apkUrl ?: newer.releaseUrl ?: UpdateChecker.RELEASES_PAGE_URL)
    }

    fun checkUpdate() {
        if (checkingUpdate) return
        checkingUpdate = true
        scope.launch {
            updateResult = UpdateChecker.check()
            checkingUpdate = false
            if (updateResult is UpdateChecker.Result.Newer) {
                showUpdateDialog = true
            }
        }
    }

    LaunchedEffect(Unit) { checkUpdate() }

    val backStack = rememberNavBackStack<Route>(Route.Home)

    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(selectedTab) { keyboard?.hide() }
    LaunchedEffect(backStack.size) { keyboard?.hide() }
    fun goBack() {

        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    fun push(route: Route) {
        if (backStack.lastOrNull() == route) return
        backStack.add(route)
    }

    fun copy(item: TransformItem) {
        copyToClipboard(item.output)
        scope.launch { snackbarHostState.showSnackbar(trf("已复制「{}」", item.styleTitle)) }
    }

    fun copyRaw(text: String, label: String) {
        copyToClipboard(text)
        scope.launch { snackbarHostState.showSnackbar(trf("已复制{}", label)) }
    }

    fun openStyle(styleId: String, text: String = input) {
        recent.record(styleId)
        push(Route.Item(styleId, text))
    }

    SweetLimeTheme(monet = settings.monet, themeMode = settings.themeMode) {

        val overScrollState = remember { OverScrollState() }

        val hyperHaptic = rememberHyperHapticFeedback()
        CompositionLocalProvider(
            LocalOverscrollFactory provides MiuixOverscrollFactory,
            LocalOverScrollState provides overScrollState,
            LocalHapticFeedback provides hyperHaptic,
        ) {

        Scaffold(

            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {

            BgEffectBackground(
                enabled = settings.flowingBackground,

                isOs3 = HyperOsStyle.resolveIsOs3(settings.flowingStyle),
                modifier = Modifier.fillMaxSize(),
            ) {

            val navSwipeDismiss = if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
                NavSwipeDirection.RightToLeft
            } else {
                NavSwipeDirection.LeftToRight
            }
            FlowingSurface(enabled = settings.flowingBackground) {
            NavDisplay(
                backStack = backStack,
                onBack = { goBack() },

                effects = if (!settings.flowingBackground) {
                    NavDisplayEffects(cornerClipRadius = rememberNavSystemCornerRadius())
                } else {
                    NavDisplayEffects(
                    enableCornerClip = true,
                    cornerClipRadius = rememberNavSystemCornerRadius(),
                    cornerClipMode = NavCornerClipMode.All,

                    dimAmount = if (settings.flowingBackground) 0f else 0.35f,

                    backdropColor = if (settings.flowingBackground) {
                        Color.Transparent
                    } else {
                        MiuixTheme.colorScheme.surface
                    },
                    )
                },
            ) {
                entry<Route.Home> {
                    RootScaffold(
                        favorites = favorites,

                        atRoot = backStack.size <= 1,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        input = input,
                        onInputChange = { input = it },
                        floatingBottomBar = settings.floatingBottomBar,
                        onCopy = ::copy,
                        onOpenGroup = { push(Route.Group(it.name)) },
                        onOpenTool = { push(Route.Tool(it)) },
                        onOpenItem = { openStyle(it.styleId, it.input) },
                        onOpenSettings = { push(Route.Settings) },
                        onOpenSearch = { push(Route.Search) },
                        recentIds = recent.ids,
                        onOpenRecent = { openStyle(it) },
                        newVersion = newerRelease?.latest,
                        onOpenUpdateDialog = { showUpdateDialog = true },
                    )
                }

                entry<Route.Group>(swipeDismiss = navSwipeDismiss) { route ->
                    val group = StyleGroup.entries.firstOrNull { it.name == route.groupId } ?: StyleGroup.LATIN
                    GroupScreen(
                        group = group,
                        input = input,
                        favorites = favorites,
                        onBack = { goBack() },
                        onCopy = ::copy,
                        onToggleFavorite = { favorites.toggle(it.key) },
                        onOpenItem = { openStyle(it.styleId, it.input) },
                    )
                }

                entry<Route.Item>(swipeDismiss = navSwipeDismiss) { route ->
                    val transform = TransformRegistry.byId(route.styleId)
                    if (transform == null) {
                        LaunchedEffect(route) { goBack() }
                    } else {

                        fun favoriteKey(text: String) =
                            TransformItem(transform.id, transform.title, text, transform.transform(text)).key
                        DetailScreen(
                            transform = transform,
                            initialInput = route.input,
                            isFavorite = { favorites.contains(favoriteKey(it)) },
                            onBack = { goBack() },
                            onCopy = { output -> copyRaw(output, "「${transform.title}」") },
                            onShare = { output -> shareText(output) },
                            onToggleFavorite = { text -> favorites.toggle(favoriteKey(text)) },

                            onInputChange = { input = it },
                        )
                    }
                }

                entry<Route.Tool>(swipeDismiss = navSwipeDismiss) { route ->
                    ToolScreen(
                        toolId = route.id,
                        onBack = { goBack() },
                        onCopyText = ::copyRaw,

                        onMessage = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
                        onOpenSymbolCategory = { push(Route.Symbols(it)) },
                    )
                }

                entry<Route.Symbols>(swipeDismiss = navSwipeDismiss) { route ->
                    SymbolCategoryScreen(
                        index = route.index,
                        onBack = { goBack() },
                        onCopyText = ::copyRaw,
                    )
                }

                entry<Route.Settings>(swipeDismiss = navSwipeDismiss) {
                    SettingsScreen(
                        settings = settings,
                        onBack = { goBack() },
                        onCopyText = ::copyRaw,
                        onOpenUrl = ::openUrl,
                        onOpenPrivacy = { push(Route.Privacy) },
                        onOpenLicenses = { push(Route.Licenses) },
                        onOpenAbout = { push(Route.About) },
                        updateResult = updateResult,
                        checkingUpdate = checkingUpdate,
                        onCheckUpdate = { checkUpdate() },
                        onOpenDownload = { openUpdateDownload() },
                    )
                }

                entry<Route.Search>(swipeDismiss = navSwipeDismiss) {
                    SearchScreen(
                        onBack = { goBack() },

                        onOpenStyle = { styleId, text -> openStyle(styleId, text) },
                        onOpenGroup = { push(Route.Group(it)) },
                        onOpenSymbols = { push(Route.Symbols(it)) },
                        onOpenTool = { push(Route.Tool(it)) },
                        onCopyText = ::copyRaw,
                    )
                }

                entry<Route.Privacy>(swipeDismiss = navSwipeDismiss) {
                    DocScreen(
                        title = PrivacyPolicy.TITLE,
                        sections = PrivacyPolicy.SECTIONS,
                        onBack = { goBack() },
                    )
                }

                entry<Route.About>(swipeDismiss = navSwipeDismiss) {
                    AboutScreen(
                        onBack = { goBack() },
                        onOpenUrl = ::openUrl,
                        onCopyText = ::copyRaw,
                        onOpenLicenses = { push(Route.Licenses) },
                        flowingStyle = settings.flowingStyle,
                    )
                }

                entry<Route.Licenses>(swipeDismiss = navSwipeDismiss) {
                    DocScreen(
                        title = OpenSourceLicenses.TITLE,
                        sections = OpenSourceLicenses.SECTIONS,
                        onBack = { goBack() },
                    )
                }

            }

            SnackbarHost(
                state = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (backStack.size > 1) 24.dp else 120.dp),
            )
            val dialogRelease = newerRelease
            if (showUpdateDialog && dialogRelease != null) {
                UpdateNotesDialog(
                    version = dialogRelease.latest,
                    notes = dialogRelease.notes,
                    onDismiss = { showUpdateDialog = false },
                    onOpenDownload = {
                        showUpdateDialog = false
                        openUpdateDownload()
                    },
                )
            }
            }
            }
            }
        }
    }
    }
}

@Composable
private fun RootScaffold(
    favorites: FavoritesStore,

    atRoot: Boolean,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    input: String,
    onInputChange: (String) -> Unit,
    floatingBottomBar: Boolean,
    onCopy: (TransformItem) -> Unit,
    onOpenGroup: (StyleGroup) -> Unit,
    onOpenTool: (String) -> Unit,
    onOpenItem: (TransformItem) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit,
    recentIds: List<String>,
    onOpenRecent: (String) -> Unit,
    newVersion: String?,
    onOpenUpdateDialog: () -> Unit,
) {
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val backdrop = rememberLayerBackdrop()

    val hazeState = remember { HazeState() }

    val scrollBehavior = MiuixScrollBehavior()

    val tabPagerState = rememberPagerState(pageCount = { 3 })

    val pagerScope = rememberCoroutineScope()
    val tabState = rememberTabPagerState(pagerState = tabPagerState, coroutineScope = pagerScope)

    val backAction = resolveBackAction(atRoot = atRoot, selectedTab = tabState.selectedPage)

    val backEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = backEventState,
        isBackEnabled = backAction == BackAction.ToConvertTab,
        onBackCompleted = {
            tabState.animateToPage(0)
        },
    )

    val pagerCurrentPage = tabState.pagerState.currentPage
    LaunchedEffect(pagerCurrentPage) {
        tabState.syncPage()
    }

    val pagerSettledPage = tabState.pagerState.settledPage
    LaunchedEffect(pagerSettledPage) {
        if (selectedTab != pagerSettledPage) onTabSelected(pagerSettledPage)
    }

    var warmPages by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {

        HyperHaptics.hasMiuixCompat()
        delay(1_200)
        warmPages = 1
        delay(3_000)
        warmPages = 2
    }
    val bottomBlurPx = remember(density) { with(density) { 40.dp.toPx() } }

    val glassTint = MiuixTheme.colorScheme.surface
    val flowing = LocalFlowingBackground.current

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .hazeSource(state = hazeState)
                .nestedScroll(scrollBehavior.nestedScrollConnection)

                .then(
                    if (flowing) {
                        Modifier.glassBarBackdrop()
                    } else {

                        val plainBg = MiuixTheme.colorScheme.surface
                        Modifier.drawBehind { drawRect(plainBg) }
                    },
                ),
        ) {
            val contentPadding = remember(topBarHeight, bottomBarHeight) {
                PaddingValues(
                    top = topBarHeight,
                    bottom = bottomBarHeight + 24.dp,
                )
            }
            HorizontalPager(
                state = tabPagerState,
                modifier = Modifier.fillMaxSize(),

                overscrollEffect = null,
                userScrollEnabled = true,
                pageNestedScrollConnection = PagerGestureNestedScrollConnection,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = tabPagerState,
                    snapAnimationSpec = PagerNavigationSpringSpec,
                ),
            ) { page ->
                when (page) {
                0 -> HomeScreen(
                    input = input,
                    onInputChange = onInputChange,
                    outerPadding = contentPadding,
                    onOpenGroup = onOpenGroup,
                    recentIds = recentIds,
                    onOpenRecent = onOpenRecent,
                    newVersion = newVersion,
                    onOpenUpdateDialog = onOpenUpdateDialog,
                )
                1 -> ToolsScreen(
                    outerPadding = contentPadding,
                    onOpenTool = onOpenTool,
                )
                else -> FavoriteScreen(
                    favorites = favorites,
                    outerPadding = contentPadding,
                    onCopy = onCopy,
                    onOpen = onOpenItem,
                )
            }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bleedHorizontal()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(

                            blurRadius = 20.dp,
                            noiseFactor = 0.15f,
                            tint = HazeTint(glassTint.copy(alpha = if (flowing) 0.16f else 0.30f)),
                        ),
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                        )
                    },
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to MiuixTheme.colorScheme.surface.copy(alpha = 0.55f),
                            0.6f to Color.Transparent,
                        ),
                    ),
            )
            TopAppBar(

                title = "SweetLime",
                largeTitle = "SweetLime",
                color = Color.Transparent,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = MiuixIcons.Search,
                            contentDescription = tr("搜索"),
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = MiuixIcons.Settings,
                            contentDescription = tr("设置"),
                            tint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                },
            )
        }

        val bottomBarSink = remember { Animatable(0f) }
        LaunchedEffect(atRoot) {
            bottomBarSink.animateTo(
                targetValue = if (atRoot) 0f else 1f,
                animationSpec = tween(
                    durationMillis = if (atRoot) BOTTOM_BAR_BACK_MS else BOTTOM_BAR_SINK_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { bottomBarHeight = with(density) { it.height.toDp() } }
                .graphicsLayer {
                    translationY = size.height * bottomBarSink.value
                    alpha = 1f - bottomBarSink.value
                },
        ) {
            if (floatingBottomBar) {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    FloatingBottomBar(
                        modifier = Modifier.padding(
                            start = 28.dp,
                            end = 28.dp,

                            bottom = 12.dp +
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                        ),
                        selectedIndex = tabState.selectedPage,
                        onSelected = { tabState.animateToPage(it) },
                        backdrop = backdrop,
                        tabsCount = 3,
                    ) { activateTab ->
                        LiquidBarItem(0, tabState.selectedPage, activateTab, MiuixIcons.ConvertFile, tr("转换"))
                        LiquidBarItem(1, tabState.selectedPage, activateTab, MiuixIcons.Tune, tr("工具"))
                        LiquidBarItem(2, tabState.selectedPage, activateTab, MiuixIcons.Favorites, tr("收藏"))
                    }
                }
            } else {

                val barTint = MiuixTheme.colorScheme.surface
                Box(
                    modifier = Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 25f,
                        colors = BlurColors(
                            blendColors = listOf(
                                BlendColorEntry(color = barTint.copy(alpha = 0.62f)),
                            ),
                        ),
                    ),
                ) {
                    NavigationBar(color = Color.Transparent) {
                    NavigationBarItem(
                        selected = tabState.selectedPage == 0,
                        onClick = { tabState.animateToPage(0) },
                        icon = MiuixIcons.ConvertFile,
                        label = tr("转换"),
                    )
                    NavigationBarItem(
                        selected = tabState.selectedPage == 1,
                        onClick = { tabState.animateToPage(1) },
                        icon = MiuixIcons.Tune,
                        label = tr("工具"),
                    )
                    NavigationBarItem(
                        selected = tabState.selectedPage == 2,
                        onClick = { tabState.animateToPage(2) },
                        icon = MiuixIcons.Favorites,
                        label = tr("收藏"),
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.LiquidBarItem(
    index: Int,
    selectedTab: Int,
    activateTab: (Int) -> Unit,
    icon: ImageVector,
    label: String,
) {
    FloatingBottomBarItem(
        selected = selectedTab == index,
        onClick = { activateTab(index) },
        modifier = Modifier.defaultMinSize(minWidth = 76.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
        )
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

private const val BOTTOM_BAR_SINK_MS = 260

private const val BOTTOM_BAR_BACK_MS = 380
