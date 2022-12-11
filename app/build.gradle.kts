plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
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
    compileSdk = 33
    buildToolsVersion = "33.0.0"

    defaultConfig {
        applicationId = "pw.janyo.whatanime"
        minSdk = 21
        targetSdk = 33
        versionCode = gitVersionCode
        versionName = "1.7.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        setProperty("archivesBaseName", "WhatAnime-$versionName")
        //指定room.schemaLocation生成的文件路径
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }
}

dependencies {
    //androidx
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha01")
    implementation("com.google.android.material:material:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    //compose
    implementation("androidx.compose.ui:ui:1.3.2")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    //material
    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.compose.material3:material3:1.1.0-alpha03")
    //lottie
    implementation("com.airbnb.android:lottie-compose:5.2.0")
    //accompanist
    val accompanistVersion = "0.28.0"
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    //room
    val roomVersion = "2.4.3"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    //koin
    val koinVersion = "3.3.0"
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
    //coil
    val coilVersion = "2.2.2"
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //moshi
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    //mmkv
    implementation("com.tencent:mmkv-static:1.2.14")
    //AppCenter
    val appCenterSdkVersion = "5.0.0"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
    //ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.18.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}