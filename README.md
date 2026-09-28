# SweetLime

一款**离线**的文字特效 / 花式昵称生成器，附带一组随手可用的小工具。
输入任意文字，实时生成多种花体样式，一键复制或收藏。

UI 使用 [Miuix](https://github.com/compose-miuix-ui/miuix)（Compose Multiplatform 版 HyperOS 设计语言），
即 KernelSU 同款风格。

## 功能

| 模块 | 说明 |
|---|---|
| 转换页 | 输入框实时转换，英文花体 + 中文 / 叠加样式，分组卡片展示 |
| 详情页 | 大字号预览、原文对照、复制 / 收藏 |
| 收藏页 | 按 `样式 + 原文` 持久化，含最近使用记录 |
| 搜索 | 一个输入框搜全站：汉字释义、拼音、拆字、花体样式、符号、工具箱 |
| 工具箱 | 符号表、拼音、拆字、大写金额、Base64、进制、颜色、汇率、BMI、代码编辑器 |
| 设置页 | 动态取色（Monet）、深色模式（跟随系统 / 浅色 / 深色）、隐私与兼容性说明 |
| 手势 | 系统预测性返回：跟手滑动详情页，同时底层主界面高斯模糊（最大 28dp） |

样式清单（`core/styles/`）：

* 英文：粗体、斜体、粗斜体、手写体、粗手写体、哥特体、双线体、圆圈体、小型大写、等宽体
* 叠加：下划线、删除线、斜杠、上点、波浪线（组合字符，对中英文都生效）
* 另有上下标、方框、模板类样式

## 代码编辑器

一个能对付日常小活的文本框，重点在「别把用户的数据弄坏」：

* **导入**普通文本文件，或**追加导入**到当前内容后面
* **读压缩包**：能进 zip 里逐层浏览、编辑其中某个文件
* **查找 / 替换**：单个替换（带环绕）、全部替换、匹配计数
* **输入辅助**：自动补右括号 / 引号、回车保留上一行缩进、成对符号自动拆行
* **编码不硬改**：读取时探测（UTF-8 / UTF-8-BOM / UTF-16LE / UTF-16BE / GB18030），
  保存时**按原编码写回** —— 一个 GBK 的老文件不会被悄悄改成 UTF-8
* **写回可回滚**：覆盖前先把原内容留在内存，写失败立刻写回去
* **解压炸弹防护**：解压总量 > 96 MB 或条目数 > 4096 直接拒绝；单个导入文件上限 32 MB

限制（有意为之）：只支持 zip（rar / 7z / tar.gz 不支持）；写回不是原子操作，
硬掉电理论上仍可能留下半截文件。

## 技术栈 / 版本

| 项 | 值 |
|---|---|
| 语言 / UI | Kotlin Multiplatform **2.4.10** + Compose Multiplatform **1.12.1** |
| UI 库 | Miuix **0.9.4**（`miuix-ui` / `icons` / `preference` / `blur` / `squircle`） |
| AGP / Gradle | **9.1.0** / **9.3.1** |
| 依赖 | androidx activity-compose 1.13.0 |
| SDK | compileSdk **37**、minSdk **33**（毛玻璃与预测性返回的下限）、targetSdk **36** |
| 版本 | versionCode **2500** / versionName **2.5.0** |

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

### 正式签名与密钥

**本仓库不包含签名密钥**（`*.jks` / `local.properties` 都在 `.gitignore` 里）。
签名信息从 `local.properties` 读取，缺了就只出未签名包，不会让编译失败：

```properties
sweetlime.storeFile=sweetlime-release.jks
sweetlime.storePassword=你的仓库口令
sweetlime.keyAlias=sweetlime
sweetlime.keyPassword=你的密钥口令
```

要发自己的包，用 `keytool` 生成一份密钥放到 `composeApp/` 下，按上面填好即可。

> 覆盖安装要求签名一致：换了密钥就必须先卸载旧版。

注意：AGP 9 起 `com.android.application` 不再与 `org.jetbrains.kotlin.multiplatform`
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

（普通 x86_64 电脑 / Android Studio 不需要这一项。）

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

在 arm64 Ubuntu（proot）+ Android 15/16 真机上：

* `assembleDebug` / `assembleRelease` 构建成功
* release 包签名校验通过（自签密钥）
* 单元测试全绿
* 安装并启动成功，无崩溃

## 许可证

[MIT](LICENSE) —— 随便用、随便改、随便发，保留版权声明即可。

## 待办

* 应用图标目前是矢量占位图（`res/drawable/ic_launcher.xml`），待替换为正式设计
* 自适应图标（mipmap + adaptive-icon）尚未补齐
* 更新检查接的是本仓库的 Releases：主源 `releases/latest`，备用源 `master/version.json`
  （GitHub 匿名 API 有频率限制，共享 IP 容易撞到，raw 没有）

* 预测性返回与 Miuix 组件的手感需真机手动确认；如与 `miuix-nav` 手势冲突，需二选一
* 压缩包只支持 zip；写回尚非事务（已有内存备份回滚）