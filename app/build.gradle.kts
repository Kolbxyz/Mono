import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

// Release signing is read from local.properties (never committed). Absent -> unsigned release.
val keystoreProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val hasReleaseKeystore = keystoreProps.getProperty("releaseStoreFile") != null

android {
    namespace = "com.wander.mono"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.wander.mono"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "0.6.0"

        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = file(keystoreProps.getProperty("releaseStoreFile"))
                storePassword = keystoreProps.getProperty("releaseStorePassword")
                keyAlias = keystoreProps.getProperty("releaseKeyAlias")
                keyPassword = keystoreProps.getProperty("releaseKeyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    androidResources {
        // Generates `localeConfig` from the `values-xx` directories, so the phone's own "App
        // languages" screen lists exactly what this build ships.
        generateLocaleConfig = true
        localeFilters += "en"
    }

    lint {
        // androidx.lifecycle's RepeatOnLifecycleDetector crashes on Kotlin 2.3 sources
        // ("AsyncExecutionService.getService must not return null"). This disables the broken
        // detector, not a real finding — drop it once the lifecycle lint artifact is fixed.
        disable += "RepeatOnLifecycleWrongUsage"
        warningsAsErrors = false
        abortOnError = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xannotation-default-target=param-property"
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}

detekt {
    buildUponDefaultConfig = true
    source.setFrom("src/main/java", "src/test/java")
    // Compose naming and layout conventions; see the file.
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
}
