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
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)

            implementation(libs.miuix.ui)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.preference)
            implementation(libs.miuix.blur)
            implementation(libs.miuix.nav)
            implementation(libs.miuix.squircle)
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
        versionCode = 2500
        versionName = "2.5.0"
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