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
        versionCode = 4
        versionName = "beta-4"
        setProperty("archivesBaseName", "OxygenCustomizer.apk")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        useLibrary ("org.apache.http.legacy")
}

dependencies {

    // Magisk libsu version
    val libsuVersion = "5.2.2"

    // Xposed
    compileOnly(files("libs/api-82.jar"))
    compileOnly(files("libs/api-82-sources.jar"))

    // App Compat
    implementation("androidx.appcompat:appcompat:1.6.1")
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Recycler View
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Work
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("androidx.concurrent:concurrent-futures:1.1.0")

    // Palette
    implementation("androidx.palette:palette:1.0.0")

    // Biometric Auth
    implementation("androidx.biometric:biometric:1.1.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0-beta01")

    // Preference
    implementation("androidx.preference:preference:1.2.1")
    implementation("org.apache.commons:commons-text:1.11.0")
    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    // ColorPicker
    implementation("com.jaredrummler:colorpicker:1.1.0") //Color Picker Component for UI

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Circle Indicator
    implementation("me.relex:circleindicator:2.1.6")

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

    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.crossbowffs.remotepreferences:remotepreferences:0.8")

    implementation("com.github.tiagohm.MarkdownView:library:0.19.0")
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName?.replace("-(Stable|Beta)".toRegex(), ""))
}