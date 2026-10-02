package com.qingning.sweetlime.ui

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.UpdateChecker
import com.qingning.sweetlime.core.OpenSourceLicenses
import com.qingning.sweetlime.core.PrivacyPolicy
import com.qingning.sweetlime.core.copyToClipboard
import com.qingning.sweetlime.core.createKeyValueStore
import com.qingning.sweetlime.core.downloadApkToPrivateDir
import com.qingning.sweetlime.core.installApkFile
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
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import com.qingning.sweetlime.ui.components.rememberTabPagerState
import com.qingning.sweetlime.ui.effect.HyperHaptics
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
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
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.effect
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
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

/**
 * 应用外壳。
 *
 * 页面切换交给 **miuix-nav**（Miuix 0.9.4 自带的导航运行时）：
 * - 整个返回栈由一条 `Animatable<Float>`（animatedTop）驱动，每个页面的样子是「相对深度」的纯函数；
 * - 默认过渡 `NavTransitions.MiuixDefault` = 整屏滑入 + 四分之一视差 + 被覆盖层轻微淡出；
 * - 圆角裁切与压暗由 `NavDisplayEffects` 提供（默认开启），过渡只负责沿手势塑造它的曲线；
 * - 返回手势是 1:1 跟手的预测性返回，松手要么提交（spring 收尾）要么回弹。
 *
 * 我们自己只做两件事：把状态（输入框、Tab、收藏、设置）提到这一层，
 * 以及给底部导航栏铺一层玻璃。
 */
@Composable
fun SweetLimeApp() {
    val store = remember { createKeyValueStore() }
    val settings = remember(store) { SweetLimeSettings(store) }
    // 震动总开关：读一次设置就写进 HyperHaptics，设置页一改立刻生效。
    HyperHaptics.enabled = settings.hapticFeedback
    val favorites = remember(store) { FavoritesStore(store) }
    val recent = remember(store) { RecentStore(store) }
    // 界面语言：在组合期订阅设置里的 code，再用 SideEffect 落到全局 AppLocale 上。
    // 这样改语言 = 改一个 State，整个界面按新语言重组，不用重启 Activity。
    val languageCode = settings.language
    SideEffect { AppLocale.apply(AppLanguage.fromCode(languageCode)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }

    // 启动时静默查一次更新：只在查到「有更新的版本」时，主页顶部才会多出一条提示。
    // 一次请求，失败 / 离线都不影响使用（UpdateChecker 自己会兜底并返回 Failed）。
    var updateResult by remember { mutableStateOf<UpdateChecker.Result?>(null) }
    var checkingUpdate by remember { mutableStateOf(false) }
    // 「下载 → 唤起安装器」的进度：主页横幅和设置页共用同一份，两边永远一致。
    var downloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }
    // 用户点过「×」的那个版本不再提示；出了更新的一版会重新提示。
    val newVersion = UpdateChecker.homeBanner(updateResult, settings.dismissedUpdate)

    fun checkUpdate() {
        if (checkingUpdate) return
        checkingUpdate = true
        scope.launch {
            updateResult = UpdateChecker.check()
            checkingUpdate = false
        }
    }

    /**
     * 主页横幅 / 设置页「检查更新」那一行，点一下的统一入口：
     * 没查到就先查 → 查到了就在应用内下载 → 下好了交给系统安装器。
     *
     * 下载走应用私有目录，失败会停在 Failed（界面提示"点一下重试"），不会留下半截文件。
     */
    fun onUpdateAction() {
        // 包已经在本地了：交给系统安装器；缺「安装未知应用」权限就标成 NeedsPermission，
        // 界面会提示"点一下去授权"（installApkFile 内部会把授权页打开）。
        fun startInstall(path: String) {
            if (!installApkFile(path)) {
                downloadState = UpdateDownloadState.NeedsPermission(path)
            }
        }

        when (val state = downloadState) {
            // 下载中重复点没意义，忽略。
            is UpdateDownloadState.Downloading -> Unit

            // 已经下好了：再点一下就是重新拉起系统安装器。
            is UpdateDownloadState.Ready -> startInstall(state.path)

            // 上次点的时候没权限：再点一下要么装上，要么再去授权页。
            is UpdateDownloadState.NeedsPermission -> startInstall(state.path)

            else -> {
                val newer = updateResult as? UpdateChecker.Result.Newer
                if (newer == null) {
                    checkUpdate()
                } else {
                    val url = newer.apkUrl
                    if (url == null) {
                        // 更新源没给安装包直链（例如临时换成了纯文本源）：退回到打开发布页。
                        openUrl(newer.releaseUrl ?: UpdateChecker.RELEASES_PAGE_URL)
                    } else {
                        downloadState = UpdateDownloadState.Downloading(-1f)
                        scope.launch {
                            val path = downloadApkToPrivateDir(
                                url = url,
                                fileName = "SweetLime-${newer.latest}.apk",
                            ) { progress ->
                                downloadState = UpdateDownloadState.Downloading(progress)
                            }
                            if (path == null) {
                                downloadState = UpdateDownloadState.Failed
                            } else {
                                downloadState = UpdateDownloadState.Ready(path)
                                // 下完直接把安装器叫起来，省用户一下点击；装不装由用户决定。
                                // 首次装多半会缺「安装未知应用」权限，startInstall 会把它标成 NeedsPermission。
                                startInstall(path)
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) { checkUpdate() }

    // 返回栈：add / removeLastOrNull 都由 miuix-nav 接管动画与手势。
    val backStack = rememberNavBackStack<Route>(Route.Home)
    fun goBack() {
        // 双击返回时也会同一帧跑两次：把栈底那一页也弹掉的话，返回栈就空了。
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    /**
     * 唯一的入栈入口。
     *
     * 手速快（尤其双击）时，同一个点击回调会在同一帧里跑两次 —— 于是同一个路由
     * **连续入栈两次**。miuix-nav 拿路由本身当 entry 的 contentKey，重复的 key 会直接
     * 抛 `IllegalArgumentException: Duplicate contentKey on the back stack` → 闪退。
     * 栈顶已经是同一个路由就把这次点击吞掉：观感上只是「第二下没生效」，而不是崩掉。
     */
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

    /**
     * 打开某个样式的详情页（顺便记一笔「最近使用」，搜索 / 分类 / 最近使用列表都走这里）。
     *
     * [text] 是要转换的原文：主页进来就是主页输入框里的内容；
     * 搜索页进来则是**搜索框里的那行字** —— 搜什么，详情页的「原文」就是什么。
     */
    fun openStyle(styleId: String, text: String = input) {
        recent.record(styleId)
        push(Route.Item(styleId, text))
    }

    SweetLimeTheme(monet = settings.monet, themeMode = settings.themeMode) {
        // 全局把 Compose 默认的 overscroll 换成 **miuix 那套 iOS 风格弹性回弹**：
        // 滑到顶/底之后还能继续拖（内容跟手走，松手弹回去），而不是硬邦邦地顶住。
        // 换的是 CompositionLocal，所以整个软件里所有 LazyColumn / verticalScroll 都自动生效，
        // 不用一页一页去挂 modifier。
        val overScrollState = remember { OverScrollState() }
        // 震动逻辑（移植自 HyperLight）：把 Compose 的 HapticFeedback 全局换掉 ——
        // 所有控件（按钮/开关/滑块/长按/文本选择）都先走 HyperOS 原生马达，
        // 拿不到再按 HyperLight 的映射表转发 Android 常量；下面再挂「顶到边还在拉」的边界震动。
        val hyperHaptic = rememberHyperHapticFeedback()
        CompositionLocalProvider(
            LocalOverscrollFactory provides MiuixOverscrollFactory,
            LocalOverScrollState provides overScrollState,
            LocalHapticFeedback provides hyperHaptic,
        ) {
            // 顶到列表边界还继续拉时响一次（HyperLight rb0.java 那条逻辑）。
        // 外面套一层 miuix 的 Scaffold：它负责提供 LocalPopupStates / LocalRootPopupStates，
        // 并在最外层渲染 MiuixPopupHost()。设置页里「点一下弹出圆角菜单」的 OverlayListPopup
        // 就是靠它才有地方渲染 —— 之前手写的 Box 没有这个宿主，所以点了完全没反应。
        // contentWindowInsets = 0：边到边由我们自己管，不要再被系统栏内边距顶一次。
        Scaffold(
            // 底色交给最底下的「流光背景」自己铺 —— 它永远会先画一层主题底色，
            // 开了流光再叠着色器；这一层保持透明，不然会把流光整个盖住。
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
            // 最底层：主题底色 +（设置里开启时）HyperOS 那种流光着色器。
            // 放在 NavDisplay 之前，所以永远在所有内容之下；
            // 机型不支持 RuntimeShader 时自动退化成纯底色，不会崩。
            BgEffectBackground(
                enabled = settings.flowingBackground,
                // 跟随系统 / OS2 / OS3：见 HyperOsStyle。手动选了就无视 HyperOS 大版本。
                isOs3 = HyperOsStyle.resolveIsOs3(settings.flowingStyle),
                modifier = Modifier.fillMaxSize(),
            ) {
            // 页面容器：开了「流光背景」时，把 Miuix 的几档容器色换成半透明，让卡片
            // 浮在流光上；每个二级页再拿**同一帧的同一支画刷**给自己刷一层实流光当底
            // （见 FlowingLayer.kt / flowingPageLayer()），
            // 所以二级页 / 设置页的背景就是「和主页一模一样的实流光」，完全不透。
            // 不开则完全原样（连一层 MiuixTheme 都不套），观感和以前一模一样。
            FlowingSurface(enabled = settings.flowingBackground) {
            NavDisplay(
                backStack = backStack,
                onBack = { goBack() },
                // 大 R 角 + 轻微压暗：过渡中两张页面（滑进来的新的、以及底下被揭开的上一级）
                // 四角都是圆角，露出底下那层背景色，就是 HyperOS 那种「卡片」观感。
                // 圆角半径跟 KernelSU 一样取**系统圆角**（本机是多少就用多少），
                // 不是我们自己瞎定的数字。
                effects = NavDisplayEffects(
                    enableCornerClip = true,
                    cornerClipRadius = rememberNavSystemCornerRadius(),
                    cornerClipMode = NavCornerClipMode.All,
                    // 开着流光时不要压暗：这层压暗是画在「页面之下、流光之上」的，
                    // 二级页/设置页本身是透明的（对齐上游 lyricon：页面不铺自己的底色），
                    // 于是这层 0.35 的黑纱直接盖在流光上 —— 页面背景就变成一片灰，
                    // 实测左边距从 (242,242,247) 掉到 (141,142,162)，正好是 0.65 倍。
                    // 上游没有这层压暗，所以那边二级页的流光是亮的。
                    dimAmount = if (settings.flowingBackground) 0f else 0.35f,
                    // 这层是「页面圆角外露出来的底色」。开着流光时必须透明，
                    // 否则它会把最底下那层流光整个盖住（页圆角也就露不出流光了）。
                    backdropColor = if (settings.flowingBackground) {
                        Color.Transparent
                    } else {
                        MiuixTheme.colorScheme.surface
                    },
                ),
            ) {
                entry<Route.Home> {
                    RootScaffold(
                        favorites = favorites,
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
                        newVersion = newVersion?.latest,
                        updateDownload = downloadState,
                        onUpdateAction = { onUpdateAction() },
                        // 点「×」：记下这个版本，之后不再提示（出了更新的一版会重新提示）。
                        onDismissUpdate = {
                            newVersion?.let { settings.dismissedUpdate = it.latest }
                        },
                    )
                }

                entry<Route.Group>(swipeDismiss = NavSwipeDirection.LeftToRight) { route ->
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

                entry<Route.Item>(swipeDismiss = NavSwipeDirection.LeftToRight) { route ->
                    val transform = TransformRegistry.byId(route.styleId)
                    if (transform == null) {
                        LaunchedEffect(route) { goBack() }
                    } else {
                        // 收藏键 = 样式 + 原文，原文在详情页里可以随时改，所以实时算。
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
                            // 详情页里改的原文同步回主页输入框，返回后前后一致。
                            onInputChange = { input = it },
                        )
                    }
                }

                entry<Route.Tool>(swipeDismiss = NavSwipeDirection.LeftToRight) { route ->
                    ToolScreen(
                        toolId = route.id,
                        onBack = { goBack() },
                        onCopyText = ::copyRaw,
                        // 工具页里需要一句提示（比如「已保存到下载」）时弹 Snackbar。
                        onMessage = { message -> scope.launch { snackbarHostState.showSnackbar(message) } },
                        onOpenSymbolCategory = { push(Route.Symbols(it)) },
                    )
                }

                entry<Route.Symbols>(swipeDismiss = NavSwipeDirection.LeftToRight) { route ->
                    SymbolCategoryScreen(
                        index = route.index,
                        onBack = { goBack() },
                        onCopyText = ::copyRaw,
                    )
                }

                entry<Route.Settings>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    SettingsScreen(
                        settings = settings,
                        onBack = { goBack() },
                        onCopyText = ::copyRaw,
                        onOpenUrl = ::openUrl,
                        onOpenPrivacy = { push(Route.Privacy) },
                        onOpenLicenses = { push(Route.Licenses) },
                        onOpenAbout = { push(Route.About) },
                        // 检查更新 / 下载安装：和主页横幅共用同一份状态，两边显示永远一致。
                        updateResult = updateResult,
                        checkingUpdate = checkingUpdate,
                        updateDownload = downloadState,
                        onCheckUpdate = { checkUpdate() },
                        onUpdateAction = { onUpdateAction() },
                    )
                }

                entry<Route.Search>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    SearchScreen(
                        onBack = { goBack() },
                        // 搜索页的原文 = 搜索框里那行字，直接带进详情页。
                        onOpenStyle = { styleId, text -> openStyle(styleId, text) },
                        onOpenGroup = { push(Route.Group(it)) },
                        onOpenSymbols = { push(Route.Symbols(it)) },
                        onOpenTool = { push(Route.Tool(it)) },
                        onCopyText = ::copyRaw,
                    )
                }

                entry<Route.Privacy>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    DocScreen(
                        title = PrivacyPolicy.TITLE,
                        sections = PrivacyPolicy.SECTIONS,
                        onBack = { goBack() },
                    )
                }

                entry<Route.About>(swipeDismiss = NavSwipeDirection.LeftToRight) {
                    AboutScreen(
                        onBack = { goBack() },
                        onOpenUrl = ::openUrl,
                        onCopyText = ::copyRaw,
                        onOpenLicenses = { push(Route.Licenses) },
                    )
                }

                entry<Route.Licenses>(swipeDismiss = NavSwipeDirection.LeftToRight) {
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
            }
            }
            }
        }
    }
    }
}

/**
 * 底层脚手架：顶栏（大标题）+ 内容 + **压在内容之上的玻璃导航栏**。
 *
 * 玻璃（miuix-blur 的 LayerBackdrop）只采样「顶栏 + 内容」这一层 ——
 * 导航栏自己是它的兄弟节点，不会被一起采样，所以不会出现「自己采样自己」的递归。
 * 内容底部按实测栏高避让，最后一条永远完整可见。
 */
@Composable
private fun RootScaffold(
    favorites: FavoritesStore,
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
    /** 查到的新版本号；null = 主页不显示顶部提示。 */
    newVersion: String?,
    updateDownload: UpdateDownloadState,
    onUpdateAction: () -> Unit,
    onDismissUpdate: () -> Unit,
) {
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var bottomBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val backdrop = rememberLayerBackdrop()
    // 顶栏「实时模糊」用：下面内容层当 source，顶栏当 effect（Haze 的 AGSL 逐帧高斯）。
    val hazeState = remember { HazeState() }
    // HyperOS 的大标题：往上滑时大标题收起、往下滑时再展开。
    // 它靠嵌套滚动驱动，所以下面的内容层要挂上它的 nestedScrollConnection。
    val scrollBehavior = MiuixScrollBehavior()

    // 三个 Tab 的横向分页：手指左右滑换页；点底栏则是带动画滚过去 ——
    // 这就是 KernelSU 的切换方式（它的 miuix 里也是 Pager + animateToPage 那一套）。
    val tabPagerState = rememberPagerState(pageCount = { 3 })
    // KernelSU 的 MainPagerState（见 ui/components/TabPagerState.kt）：
    // 底栏「图标选中的页」和 Pager 的真实页分开管 —— 点一下图标立刻过去，
    // 动画在另一条协程里跑；只有不在导航中，才允许 pager 回写选中态。
    // 之前把两者混用，连点时会互相回写，表现就是「画面和图标不跟手」。
    val pagerScope = rememberCoroutineScope()
    val tabState = rememberTabPagerState(pagerState = tabPagerState, coroutineScope = pagerScope)

    // 和 KernelSU 一样：currentPage 变了才 syncPage（导航中它自己会拒绝回写）。
    val pagerCurrentPage = tabState.pagerState.currentPage
    LaunchedEffect(pagerCurrentPage) {
        tabState.syncPage()
    }
    // 停稳后同步给外部状态（各页面/其它逻辑用的 selectedTab）。
    val pagerSettledPage = tabState.pagerState.settledPage
    LaunchedEffect(pagerSettledPage) {
        if (selectedTab != pagerSettledPage) onTabSelected(pagerSettledPage)
    }

    // 预热策略（防「刚进软件前几次点击掉帧」）：
    // KernelSU 是先把所有页组合好再让你进主界面（它有启动闪屏吃掉这笔开销）。
    // 我们没有闪屏，如果在第一帧就把三个 Tab 页全组合出来，这笔开销就全压在用户
    // 刚进软件、正要开始点的那几帧上 -> 前几次点击掉帧。
    // 所以错峰：先只组合当前页，闲下来之后再陆续把邻居页 / 全部页放进组合。
    var warmPages by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        // 顺手把几处「首次用到才初始化」的东西提前备好（都是毫秒级，但要避开点击）。
        HyperHaptics.hasMiuixCompat()
        delay(1_200)
        warmPages = 1   // 左右邻居各一页（切页时目标页内容已就绪）
        delay(3_000)
        warmPages = 2   // 三页全在组合里
    }
    val bottomBlurPx = remember(density) { with(density) { 40.dp.toPx() } }
    // HyperOS 那种玻璃是有「底色」的：模糊之上再蒙一层很淡的主题色，
    // 这样它看起来是「磨砂玻璃」而不是「把内容压成一团糊」。
    // 但流光模式下不铺这层玻璃（对齐上游 lyricon 的 hazeState = null）：顶栏保持透明，
    // 让底层流光直接透上来。
    val glassTint = MiuixTheme.colorScheme.surface
    val flowing = LocalFlowingBackground.current

    Box(modifier = Modifier.fillMaxSize()) {
        // 内容层：铺满整屏，既当玻璃的采样源，也能从顶栏 / 底栏下面滑过去。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
                .hazeSource(state = hazeState)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                // 背景也画进这一层里。
                //
                // 底栏玻璃是从「这一层」采样去模糊的 —— 而这三页自己是透明的
                // （背景由更底下的 BgEffectBackground 铺），所以只要底栏后面没有内容
                // 垫着（收藏页空列表时就是），采样区就是一片空白，miuix 的液态玻璃管线
                // 在空白处会出一层不透明黑 → 整条胶囊看起来「切到了深色模式」。
                // 这里把同一帧的流光（或纯主题底色）也铺进来，背板就永远有东西可采。
                .then(
                    if (flowing) {
                        Modifier.flowingPageLayer()
                    } else {
                        // 不能在 lambda 里读主题（drawBehind 不是 @Composable 作用域），先取出来。
                        // 这里必须是 surface（#F7F7F7），**不能**用 colorScheme.background
                        // （那是纯白 #FFFFFF）：这三页的卡片/列表容器本身就是白卡，铺白底
                        // 会让卡片、列表的边缘和背景糊成一片白 —— 主页/工具/收藏「边缘混成白色」
                        // 就是这一句造成的。全软件其它页铺的都是 surface，这里跟着统一。
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
                modifier = Modifier
                    .fillMaxSize()
                    // KernelSU 那半段我之前漏了：手势交给 miuix 的拦截器统一管
                    // （斜滑判定 + 点到就刹车），不再和内置手滑两套抢。
                    .pagerGestureOverride(
                        pagerState = tabPagerState,
                        mode = PagerInterceptionMode.CrossAxisInterceptor,
                        enabled = true,
                    ),
                beyondViewportPageCount = warmPages,
                overscrollEffect = null,
                // 和 KernelSU 一样：内置手滑关掉，手势只走上面那个拦截器。
                userScrollEnabled = false,
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
                    updateDownload = updateDownload,
                    onUpdateAction = onUpdateAction,
                    onDismissUpdate = onDismissUpdate,
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

        // 顶部玻璃栏。
        //
        // 顶栏的模糊走 **Haze**（AGSL 逐帧高斯，RenderEffect.createRuntimeShaderEffect）——
        // 内容层是 source、这里是 effect，所以：**列表从下面滑过去时是实时糊的**，
        // 而且用 progressive 做成「贴着最顶上最糊、往下渐隐到 0」的渐变，
        // 而不是「一条硬边的整块模糊」。
        // 流光模式下也照铺（以前这时顶栏是全透明的，滚动时一点模糊都没有）。
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            // 半径 20dp / 噪点 0.15：跟 HyperLight 那边同一套参数。
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
            TopAppBar(
                // 大标题会随内容上滑收起、下滑展开（HyperOS 那种），
                // 收起后小标题才会出现在栏里 —— 不再是「SweetLime 一直杵在顶上」。
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

        // 底部玻璃栏：浮在内容之上，列表从它下面滑过时被高斯模糊。
        //
        // 两种形态（由设置里的「悬浮底栏」切换）：
        // - 普通：贴底整条，玻璃矩形铺满整个宽度，上沿用渐变淡出；
        // - 悬浮：抄 KernelSU 那种浮起来的小圆角胶囊（miuix 自带的 FloatingNavigationBar），
        //   玻璃直接画在胶囊自己身上（同形状同尺寸），不做渐隐，左右留白。
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { bottomBarHeight = with(density) { it.height.toDp() } },
        ) {
            if (floatingBottomBar) {
                // KernelSU 那套「液态玻璃」胶囊（移植自它的 FloatingBottomBar，见
                // ui/components/FloatingBottomBar.kt）：玻璃、随重力转的高光反光、
                // 按下放大、以及那枚**能跟着手指拖动的指示器**，全部由组件自己画。
                // 我们只负责把「内容那一层」的 backdrop 递进去当采样源，并按它的规矩留边距。
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    FloatingBottomBar(
                        modifier = Modifier.padding(
                            start = 28.dp,
                            end = 28.dp,
                            // 胶囊浮在系统手势条之上：KernelSU 是 8dp + 系统内边距，
                            // 这里给得略松一点（12dp + 内边距），观感更透气。
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
                // KernelSU 的默认形态 = miuix 标准 NavigationBar：贴底、主题底色，一条实心栏。
                // 不铺玻璃、不做模糊、不浮起（那套液态玻璃只在「悬浮底栏」打开时才用）。
                NavigationBar {
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

/**
 * 液态玻璃底栏里的单个入口：图标 + 一行小字，跟 KernelSU 那边一致。
 *
 * 之所以把「图标 + 文字」都按 KernelSU 的写法直接塞进去（而不是用 miuix 现成的 item）：
 * 拖动那枚指示器时，玻璃会把胶囊**内部的这一层内容**重新着色成强调色 —— 前提是图标和文字
 * 都取 `LocalContentColor`，组件自己会在指示器滑过来时把 LocalContentColor 换成强调色。
 *
 * [activateTab] 来自 [FloatingBottomBar] 的 content lambda：点一下就带动画地滑过去。
 */
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