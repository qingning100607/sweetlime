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

val signingProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
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

            implementation(libs.haze)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigationevent.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

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
        versionCode = 2986
        versionName = "2.9.86"
    }
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
    sourceSets["main"].jniLibs.srcDir("${layout.buildDirectory.get().asFile}/nativeLibs")

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    buildTypes {
        getByName("release") {
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
            isMinifyEnabled = true
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
        val libcxx = ndkSysroot?.resolve("usr/lib/aarch64-linux-android/libc++_shared.so")
        if (libcxx?.exists() == true) {
            libcxx.copyTo(File(outDir, "libc++_shared.so"), overwrite = true)
        } else {
            logger.warn("⚠️ 没找到 libc++_shared.so，原生库运行时可能加载失败")
        }
        logger.lifecycle("原生库已生成：${outDir.listFiles()?.joinToString { it.name }}")
    }
}

tasks.matching { it.name == "preBuild" }.configureEach { dependsOn("buildNativeLib") }
