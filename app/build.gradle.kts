import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {

    namespace = "it.dhd.oxygencustomizer"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.dhd.oxygencustomizer"
        minSdk = 33
        targetSdk = 34
        versionCode = 10
        versionName = "beta-10"
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
        }
    } catch (ignored: Exception) {
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
                println("OutputFileName: $outputFileName")
                output.outputFileName = outputFileName
            }
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
        buildConfig = true
        aidl = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    useLibrary ("org.apache.http.legacy")
}

dependencies {

    // Magisk libsu version
    val libsuVersion = "5.2.2"

    // Xposed
    compileOnly(files("libs/api-82.jar"))
    compileOnly(files("libs/api-82-sources.jar"))

    // App Compat
    implementation("androidx.appcompat:appcompat:1.7.0")
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Recycler View
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Work
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.concurrent:concurrent-futures:1.2.0")

    // Biometric Auth
    implementation("androidx.biometric:biometric:1.1.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Preference
    //noinspection KtxExtensionAvailable
    implementation("androidx.preference:preference:1.2.1")
    implementation("org.apache.commons:commons-text:${rootProject.extra["commonsTextVersion"]}")
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    // ColorPicker
    implementation("com.jaredrummler:colorpicker:1.1.0") //Color Picker Component for UI

    // Lottie
    implementation("com.airbnb.android:lottie:6.4.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //noinspection KaptUsageInsteadOfKsp
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Fading Edge Layout
    implementation("com.github.bosphere.android-fadingedgelayout:fadingedgelayout:1.0.0")

    // The core module that provides APIs to a shell
    implementation("com.github.topjohnwu.libsu:core:${libsuVersion}")
    // Optional: APIs for creating root services. Depends on ":core"
    implementation("com.github.topjohnwu.libsu:service:${libsuVersion}")
    // Optional: Provides remote file system support
    implementation("com.github.topjohnwu.libsu:nio:${libsuVersion}")

    // Constraint
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.crossbowffs.remotepreferences:remotepreferences:0.8")

    implementation("com.github.tiagohm.MarkdownView:library:0.19.0")

    //Google Subject Segmentation - MLKit
    implementation("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation("com.google.android.gms:play-services-base:18.5.0")

    // APK Signer
    implementation("org.bouncycastle:bcpkix-jdk18on:1.77")

    // Zip Util
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // Dots Indicator
    implementation("com.tbuonomo:dotsindicator:5.0")

    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Palette
    implementation("androidx.palette:palette:1.0.0")
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName)
}