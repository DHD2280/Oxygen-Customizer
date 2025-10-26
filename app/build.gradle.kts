import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {

    namespace = "it.dhd.oxygencustomizer"
    compileSdk = 36

    defaultConfig {
        applicationId = "it.dhd.oxygencustomizer"
        minSdk = 33
        targetSdk = 36
        versionCode = 205
        versionName = "beta-205"
        setProperty("archivesBaseName", "OxygenCustomizer.apk")
        buildConfigField("int", "MIN_SDK_VERSION", "$minSdk")
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")

    try {
        val keystoreProperties = Properties()
        FileInputStream(keystorePropertiesFile).use { inputStream ->
            keystoreProperties.load(inputStream)
        }

        releaseSigning = signingConfigs.create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            enableV1Signing = true
            enableV2Signing = true
        }
    } catch (_: Exception) {}

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = releaseSigning
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = releaseSigning
        }
        getByName("debug") {
            versionNameSuffix = ".debug"
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val outputFileName = "OxygenCustomizer.apk"
                output.outputFileName = outputFileName
            }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    packaging {
        jniLibs.excludes += setOf(
            "/META-INF/*",
            "/META-INF/versions/**",
            "/org/bouncycastle/**",
            "/kotlin/**",
            "/kotlinx/**"
        )

        resources.excludes += setOf(
            "/META-INF/*",
            "/META-INF/versions/**",
            "/org/bouncycastle/**",
            "/kotlin/**",
            "/kotlinx/**",
            "rebel.xml",
            "/*.txt",
            "/*.bin",
            "/*.json"
        )

        jniLibs.useLegacyPackaging = true
    }
}

dependencies {

    // Magisk libsu version
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.nio)

    // Xposed
    compileOnly(files("libs/api-82.jar"))
    compileOnly(files("libs/api-82-sources.jar"))

    // App Compat
    implementation(libs.appcompat)

    // Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Recycler View
    implementation(libs.recyclerview)

    // Work
    implementation(libs.work.runtime)
    implementation(libs.concurrent.futures)

    // Biometric Auth
    implementation(libs.biometric)

    // Material Design
    implementation(libs.material)

    // Splash Screen
    implementation(libs.splashscreen)

    // Preference
    implementation(libs.preference)
    implementation(libs.apache.commons.text)

    // SwipeRefreshLayout
    implementation(libs.swiperefreshlayout)

    // ColorPicker
    implementation(libs.colorpicker)

    // Lottie
    implementation(libs.lottie)

    // Glide
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // Fading Edge Layout
    implementation(libs.fadingedgelayout)

    // Constraint
    implementation(libs.constraintlayout)

    // EventBus
    implementation(libs.eventbus)

    // Remote Preferences
    implementation(libs.remotepreferences)

    // Markdown View
    implementation(libs.markdownview)

    // Google Subject Segmentation - MLKit
    implementation(libs.mlkit.segmentation)
    implementation(libs.play.services.base)

    // APK Signer
    implementation(libs.bcpkix)

    // Zip Util
    implementation(libs.zip4j)

    // Dots Indicator
    implementation(libs.dotsindicator)

    // Flexbox
    implementation(libs.flexbox)

    // Palette
    implementation(libs.palette)

    // Persian Date
    implementation(libs.persian.date.time)

    // OkHTTP
    implementation(libs.okhttp)

    // Image Cropper
    implementation(libs.image.cropper)

    // Oneplus UI
    implementation(libs.oneplus.ui)
//    implementation("it.dhd:oneplusui:1.4.5")

    // Hidden API
    implementation(libs.lsposed.hiddenapi)

    //Gson
    implementation(libs.gson)

    implementation(libs.dexplore) {
        exclude(group = "com.google.guava")
    }
    implementation(libs.guava)

    // Stub classes
    compileOnly(project(":oos_launcher"))
    compileOnly(project(":oos_systemui"))

}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName)
}
