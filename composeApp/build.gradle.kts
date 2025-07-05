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
val appVersionName = "1.8.6"

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
            linkerOpts.add("-lsqlite3")
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
            //room
            implementation(libs.androidx.room.ktx)
            //mmkv
            implementation(libs.mmkv.android)
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
            //aboutlibraries
            implementation(libs.aboutlibraries.core)
            implementation(libs.aboutlibraries.compose.core)
            implementation(libs.aboutlibraries.compose.m3)
            //room
            implementation(libs.androidx.room)
            //preference
            implementation(libs.compose.preference)
            //kotlin-crypto-hash
            implementation(project.dependencies.platform(libs.kotlin.crypto.hash.bom))
            implementation(libs.kotlin.crypto.hash.md)
            implementation(libs.kotlin.crypto.hash.sha1)
            implementation(libs.kotlin.crypto.hash.sha2)
            //media-player
            implementation(libs.media.player)
        }
        iosMain.dependencies {
            //ktor
            implementation(libs.ktor.client.darwin)
            implementation(libs.ios.settings)
        }
    }
}

android {
    namespace = "pw.janyo.whatanime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "pw.janyo.whatanime"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = gitVersionCode
        versionName = appVersionName

        setProperty("archivesBaseName", "WhatAnime-$versionName")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("sign")
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
            signingConfig = signingConfigs.getByName("sign")
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

tasks.register("updateAppleBuildVersion") {
    doLast {
        val configTemplate = rootProject.file("iosApp/Configuration/Config.xcconfig.template")
        val config = rootProject.file("iosApp/Configuration/Config.xcconfig")
        val content = configTemplate.readText()
        var newContent =
            content.replace(Regex("MARKETING_VERSION=.*"), "MARKETING_VERSION=${appVersionName}")
        newContent = newContent.replace(
            Regex("CURRENT_PROJECT_VERSION=\\d+"),
            "CURRENT_PROJECT_VERSION=${gitVersionCode}"
        )
        config.writeText(newContent)
        println("Updated Config.xcconfig with version $appVersionName (Build $gitVersionCode)")
    }
}

apply(from = rootProject.file("signing.gradle"))
