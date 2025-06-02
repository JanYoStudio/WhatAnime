import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinSerialize)
    alias(libs.plugins.kotlinKsp)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.splashscreen)
            implementation(libs.androidx.browser)
            implementation(libs.material)
            //ktor
            implementation(libs.ktor.client.okhttp)
            //koin
            implementation(libs.koin.android)
            //Media3
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.exoplayer.dash)
            implementation(libs.media3.ui)
            implementation(libs.media3.session)
            //room
            implementation(libs.androidx.room.ktx)
            //preference
            implementation(libs.compose.preference)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            //common-viewmodel
            implementation(libs.androidx.lifecycle.viewmodel)
            //common-navigation
            implementation(libs.androidx.navigation)
            //material-icons
            implementation(libs.material.icon)
            implementation(libs.material.icon.extended)
            //kotlinx-serialization
            implementation(libs.kotlinx.serialization)
            //ktorfit
            implementation(libs.ktorfit)
            //ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.content.negotiation)
            implementation(libs.ktor.serialization.json)
            //koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.viewmodel)
            implementation(libs.koin.navigation)
            //coil
            implementation(project.dependencies.platform(libs.coil.bom))
            implementation(libs.coil.compose)
            implementation(libs.coil.ktor3)
            implementation(libs.coil.cache.control)
            //kermit
            implementation(libs.kermit)
            //cmptoast
            implementation(libs.cmptoast)
            //filekit
            implementation(libs.filekit.core)
            implementation(libs.filekit.coil)
            implementation(libs.filekit.dialogs.compose)
            //mmkv
            implementation(libs.mmkv.kotlin)
            //aboutlibraries
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.core)
            implementation(libs.aboutlibraries.compose.m3)
            //room
            implementation(libs.androidx.room)
            //kotlin-crypto-hash
            implementation(project.dependencies.platform(libs.kotlin.crypto.hash.bom))
            implementation(libs.kotlin.crypto.hash.md)
            implementation(libs.kotlin.crypto.hash.sha1)
            implementation(libs.kotlin.crypto.hash.sha2)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

fun String.runCommand(workingDir: File = file("./")): String {
    val parts = this.split("\\s".toRegex())
    val proc = ProcessBuilder(*parts.toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor(1, TimeUnit.MINUTES)
    return proc.inputStream.bufferedReader().readText().trim()
}

val gitVersionCode: Int = "git rev-list HEAD --count".runCommand().toInt()
val gitVersionName = "git rev-parse --short=8 HEAD".runCommand()

android {
    namespace = "pw.janyo.whatanime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pw.janyo.whatanime"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = gitVersionCode
        versionName = "1.8.5"

        setProperty("archivesBaseName", "WhatAnime-$versionName")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = file(SignConfig.signKeyStoreFile)
            storePassword = SignConfig.signKeyStorePassword
            keyAlias = SignConfig.signKeyAlias
            keyPassword = SignConfig.signKeyPassword
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("color", "ic_launcher_background", "#FFEB3B")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = ".d$gitVersionCode.$gitVersionName"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = ".r$gitVersionCode.$gitVersionName"
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

aboutLibraries {
    offlineMode = true
    collect {
        fetchRemoteLicense = false
        fetchRemoteFunding = false
    }
    android {
        registerAndroidTasks = false
    }
    export {
        outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
    }
}
