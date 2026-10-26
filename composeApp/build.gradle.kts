import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

/*
 * 签名口令从 local.properties 读（该文件已在 .gitignore 里，不进版本库）。
 *
 * 以前这里写的是明文口令，推到 GitHub 就等于把密钥口令一起送出去。
 * 现在缺口令/缺密钥文件时就不配签名、只出未签名包，而不是让编译报错 ——
 * 别人 clone 下来不用改任何东西就能编译。
 */
val signingProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            // expect/actual 的「类」形式（NativeText / PlatformBackHandler 这些 object）
            // 在 Kotlin 里仍是 Beta：功能一直是好的，只是编译器每次都要提醒一句。
            // 压掉它，让构建输出保持干净 —— 真出问题时反而更容易看见。
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            implementation(libs.miuix.ui)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.preference)
            implementation(libs.miuix.blur)
            implementation(libs.miuix.nav)
            implementation(libs.miuix.squircle)

            // 顶栏的「实时模糊 / 渐变模糊」走 Haze（和 HyperOS 那些液态玻璃 App 同源）。
            implementation(libs.haze)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

/*
 * 单元测试强制跑在 JDK 21 上。
 *
 * 主代码按 JVM_21 编译（class 65），而 Gradle 默认用的 JDK 17 只认到 61，
 * 不指定的话测试进程会直接报 UnsupportedClassVersionError。
 */
tasks.withType<Test>().configureEach {
    javaLauncher.set(
        project.extensions.getByType(JavaToolchainService::class.java).launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

android {
    namespace = "com.qingning.sweetlime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.qingning.sweetlime"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2950
        versionName = "2.9.50"
    }
    // 正式签名（打包用）。密钥文件与口令都从 local.properties 取：
    //   sweetlime.storeFile     密钥路径（相对 composeApp/，默认 sweetlime-release.jks）
    //   sweetlime.storePassword 仓库口令
    //   sweetlime.keyAlias      别名（默认 sweetlime）
    //   sweetlime.keyPassword   密钥口令（默认同仓库口令）
    signingConfigs {
        create("release") {
            val storePath = signingProps.getProperty("sweetlime.storeFile", "sweetlime-release.jks")
            val storePass = signingProps.getProperty("sweetlime.storePassword")
            val keyPass = signingProps.getProperty("sweetlime.keyPassword", storePass)
            val alias = signingProps.getProperty("sweetlime.keyAlias", "sweetlime")
            val jks = file(storePath)
            if (!storePass.isNullOrEmpty() && jks.exists()) {
                storeFile = jks
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            }
        }
    }

    defaultConfig {
        ndk {
            abiFilters += "arm64-v8a"
        }
    }
    // 原生库（自己编译，见文件末尾的 buildNativeLib 任务）放在这里。
    // 注意：AGP 的 SourceSet API 不吃 Provider，这里得传路径。
    sourceSets["main"].jniLibs.srcDir("${layout.buildDirectory.get().asFile}/nativeLibs")

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    buildTypes {
        getByName("release") {
            // 正式签名：和 debug 分开，符合发布规范（同机覆盖安装需先卸载旧版）。
            // 没有口令时保持未签名，不阻断编译。
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            // release 的流畅度关键：R8 混淆 + 压缩，去掉调试期的大量额外开销。
            isMinifyEnabled = true
            // 资源瘦身：去掉未被引用的资源。配合下面的 keep 规则一起用。
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}


/*
 * ---------------------------------------------------------------------------
 * 原生库：libsweetlime.so（arm64-v8a）
 *
 * 为什么不用 CMake / NDK 自带工具链：Google 的 NDK 预编译包只有 **linux-x86_64**，
 * 本机是 arm64 —— 那份 clang 起不来（报 "required file not found"），CMake 直接失败。
 * 所以这里用**系统 clang**（Ubuntu 自带 18）配上 NDK 的 sysroot 与 compiler-rt 交叉编译，
 * 产物丢进 build/nativeLibs/arm64-v8a，再由上面的 jniLibs 打进 APK。
 *
 * 找不到 NDK / clang 时这个任务会自己跳过 —— 那 App 就整体走 Kotlin 实现，
 * 功能不受影响（NativeText.available = false），只是慢一点。
 */
val nativeLibsDir = layout.buildDirectory.dir("nativeLibs/arm64-v8a")

val ndkDir: File? = run {
    val sdk = System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: "/root/Android"
    File(sdk, "ndk").listFiles()?.filter { it.isDirectory }?.maxByOrNull { it.name }
}
val ndkSysroot: File? = ndkDir?.resolve("toolchains/llvm/prebuilt/linux-x86_64/sysroot")
val ndkClangResource: File? = ndkDir
    ?.resolve("toolchains/llvm/prebuilt/linux-x86_64/lib/clang")
    ?.listFiles()
    ?.filter { it.isDirectory }
    ?.maxByOrNull { it.name }

tasks.register<Exec>("buildNativeLib") {
    description = "交叉编译 libsweetlime.so（arm64-v8a）"
    group = "build"

    val outDir = nativeLibsDir.get().asFile
    val cppDir = file("src/androidMain/cpp")

    onlyIf {
        val ok = ndkSysroot?.exists() == true && ndkClangResource != null
        if (!ok) logger.warn("⚠️ 没有可用的 NDK sysroot，跳过原生库：本次构建全部走 Kotlin 实现")
        ok
    }

    inputs.dir(cppDir)
    outputs.dir(outDir)

    doFirst { outDir.mkdirs() }

    // 注意：--target / --sysroot / -resource-dir 缺一不可，否则 apt 的 clang 会去找宿主的
    // libgcc（错误信息是 "unable to find library -lgcc"）。
    commandLine(
        listOf(
            "clang++",
            "--target=aarch64-linux-android33",
            "--rtlib=compiler-rt",
            "--unwindlib=libunwind",
            "-resource-dir=${ndkClangResource?.absolutePath}",
            "-fPIC",
            "-shared",
            "-O2",
            "-fno-rtti",
            "-fvisibility=hidden",
            "-fuse-ld=lld",
            "-stdlib=libc++",
            "-DANDROID",
            "--sysroot=${ndkSysroot?.absolutePath}",
            "-o",
            "${outDir.absolutePath}/libsweetlime.so",
            "${cppDir.absolutePath}/native_text.cpp",
            "-lc++_shared",
        ),
    )

    doLast {
        // libc++ 用的是共享版，这个 .so 也得一起打进 APK，否则运行时加载不到。
        val libcxx = ndkSysroot?.resolve("usr/lib/aarch64-linux-android/libc++_shared.so")
        if (libcxx?.exists() == true) {
            libcxx.copyTo(File(outDir, "libc++_shared.so"), overwrite = true)
        } else {
            logger.warn("⚠️ 没找到 libc++_shared.so，原生库运行时可能加载失败")
        }
        logger.lifecycle("原生库已生成：${outDir.listFiles()?.joinToString { it.name }}")
    }
}

// 打进 APK 之前先编好原生库。
tasks.matching { it.name == "preBuild" }.configureEach { dependsOn("buildNativeLib") }
