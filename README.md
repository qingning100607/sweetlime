# SweetLime

**简体中文** | [English](README.en.md)

一款的文字特效 / 花式昵称生成器，附带一组工具
输入任意文字，支持实时生成多种花体样式

## 功能

| 模块 | 说明 |
|---|---|
| 转换页 | 输入框实时转换，英文花体 + 中文 / 叠加样式，分组卡片展示 |
| 详情页 | 大字号预览、原文对照、复制 / 收藏 |
| 收藏页 | 按 `样式 + 原文` 持久化，含最近使用记录 |
| 工具箱 | 符号表、拼音、拆字、大写金额、Base64、进制、颜色、汇率、BMI |

样式清单（`core/styles/`）：

* 英文：粗体、斜体、粗斜体、手写体、粗手写体、哥特体、双线体、圆圈体、小型大写、等宽体
* 叠加：下划线、删除线、斜杠、上点、波浪线
* 另有上下标、方框、模板类样式


## 技术栈 / 版本

| 项 | 值 |
|---|---|
| 语言 / UI | Kotlin Multiplatform **2.4.10** + Compose Multiplatform **1.12.1** |
| UI 库 | Miuix **0.9.4**（`miuix-ui` / `icons` / `preference` / `blur` / `squircle`） |
| AGP / Gradle | **9.1.0** / **9.3.1** |
| 依赖 | androidx activity-compose 1.13.0 |
| SDK | compileSdk **37**、minSdk **33** targetSdk **36** |
| 版本 | versionCode **2991** / versionName **2.9.91** |

> compileSdk 必须是 37：Miuix 0.9.4 与 androidx.compose 1.12.1 的 AAR 元数据要求 37；
> 同时 compose 1.12.1 要求 AGP ≥ 9.1.0，AGP 9.1.0 要求 Gradle ≥ 9.3.1。

## 构建

```bash
./gradlew :composeApp:assembleDebug
# 产物：composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

正式包：

```bash
./gradlew :composeApp:assembleRelease
```
### 注意

自AGP 9 起 `com.android.application` 不再与 `org.jetbrains.kotlin.multiplatform`
直接共用，本项目在 `gradle.properties` 里用官方给出的兼容开关绕过：

```properties
android.builtInKotlin=false
android.newDsl=false
```

### arm64 Linux 构建机补充说明

Google 发布的 `aapt2`（linux 分类）自 AGP 9.1 起只有 x86_64 版本，在 arm64 机器上无法执行。
若在 arm64 Linux 上构建，需要提供可用的 arm64 aapt2 并在用户级 `~/.gradle/gradle.properties` 中指定：

```properties
android.aapt2FromMavenOverride=/绝对路径/aapt2
```

## 测试

```bash
./gradlew :composeApp:testDebugUnitTest
```

纯函数与文本处理逻辑的单元测试，全部跑在 JVM 上，不需要真机。
覆盖 Base64、进制转换、颜色、BMI、汇率、搜索、编辑器输入辅助、编码探测与回写。

> 主代码按 `JVM_21` 编译，测试进程需要 JDK 21（构建脚本已用 toolchain 指定）；
> 机器上只有 JDK 17 会报 `UnsupportedClassVersionError: class file version 65.0`。

## 目录结构

```
composeApp/src/
├── commonMain/kotlin/com/qingning/sweetlime/
│   ├── core/                纯逻辑，无 Android 依赖
│   │   ├── TextTransform.kt / TransformRegistry.kt  样式接口与注册表
│   │   ├── SearchEngine.kt  全站搜索
│   │   ├── ToolCatalog.kt   工具箱目录（搜索与工具箱页共用）
│   │   ├── Platform.kt      expect：剪贴板 / 文件 / 压缩包 / 编码 / 键值存储
│   │   ├── mapping/         码点映射、拼音、拆字、符号数据
│   │   ├── styles/          各类花体样式定义
│   │   └── tools/           Base64 / 进制 / 颜色 / BMI / 汇率（纯函数）
│   ├── data/                Settings / Favorites / Recent
│   └── ui/
│       ├── App.kt           导航与被模糊联动
│       ├── nav/Route.kt     路由
│       ├── components/      玻璃容器、代码高亮等
│       ├── screen/          各页面
│       └── theme/           ThemeController(Monet/System, Spec2025)
├── androidMain/             Platform 的 Android 实现 + 入口
├── commonTest/              JVM 单元测试
└── androidUnitTest/         依赖 java.nio 的测试（编码探测等）
```

新增一种花体样式只需在 `styles/` 里加一行 `mathStyle(...)` 或 `overlayStyle(...)`。

## 已验证

在 arm64 Ubuntu（proot）+ Android 15/16/17 实机上：

* `assembleDebug` / `assembleRelease` 构建成功
* release 包签名校验通过（自签密钥）

## 许可证

[MIT](LICENSE)

杂鱼
