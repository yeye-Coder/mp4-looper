import java.util.Properties

plugins {
    id("com.android.application")
}

// Signing key details live in keystore.properties (keep it private, never share it).
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// The web page used by the app = ../mp4-looper.html (copied in at build time as index.html).
val htmlAssetsDir = layout.buildDirectory.dir("generated/htmlAssets")

val copyHtml by tasks.registering(Copy::class) {
    from(rootProject.file("../mp4-looper.html"))
    into(htmlAssetsDir)
    rename { "index.html" }
}

android {
    namespace = "com.mp4looper.app"
    compileSdk = 37      // Android 17

    defaultConfig {
        applicationId = "com.mp4looper.app"
        minSdk = 29          // Android 10
        targetSdk = 37       // Android 17
        versionCode = 7
        versionName = "1.6"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(keystoreProps.getProperty("storeFile", "release.jks"))
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].assets.directories.add(htmlAssetsDir.get().asFile.path)
}

tasks.named("preBuild") { dependsOn(copyHtml) }
