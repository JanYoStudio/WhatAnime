plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.mikepenz.aboutlibraries.plugin")
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
val packageName = "pw.janyo.whatanime"

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = packageName
        minSdk = 21
        targetSdk = 34
        versionCode = gitVersionCode
        versionName = "1.8.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        setProperty("archivesBaseName", "WhatAnime-$versionName")
        ksp {
            arg(RoomSchemaArgProvider(File(projectDir, "schemas")))
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = packageName
}

dependencies {
    //androidx
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    //compose
    implementation("androidx.compose:compose-bom:2024.02.00")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.foundation:foundation")
    //lottie
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    //room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    //koin
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")
    //coil
    val coilVersion = "2.5.0"
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    //mmkv
    implementation("com.tencent:mmkv-static:1.3.3")
    //preference
    implementation("me.zhanghai.compose.preference:library:1.0.0")
    //AppCenter
    val appCenterSdkVersion = "5.0.4"
    implementation("com.microsoft.appcenter:appcenter-analytics:${appCenterSdkVersion}")
    implementation("com.microsoft.appcenter:appcenter-crashes:${appCenterSdkVersion}")
    //Media3
    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    //AboutLibraries
    val aboutLibrariesVersion = "10.10.0"
    implementation("com.mikepenz:aboutlibraries-core:$aboutLibrariesVersion")
    implementation("com.mikepenz:aboutlibraries-compose-m3:$aboutLibrariesVersion")
}

aboutLibraries {
//    // - if the automatic registered android tasks are disabled, a similar thing can be achieved manually
//    // - `./gradlew app:exportLibraryDefinitions -PaboutLibraries.exportPath=src/main/res/raw`
//    // - the resulting file can for example be added as part of the SCM
//    // registerAndroidTasks = false
//
//    // define the path configuration files are located in. E.g. additional libraries, licenses to add to the target .json
//    configPath = "config"
//    // allow to enable "offline mode", will disable any network check of the plugin (including [fetchRemoteLicense] or pulling spdx license texts)
//    offlineMode = false
//    // enable fetching of "remote" licenses. Uses the GitHub API
//    fetchRemoteLicense = true
//    // (optional) GitHub token to raise API request limit to allow fetching more licenses
//    gitHubApiToken = getLocalOrGlobalProperty("github.pat")
//
//    // Full license text for license IDs mentioned here will be included, even if no detected dependency uses them.
//    // additionalLicenses = ["mit", "mpl_2_0"]
//
//    // Allows to exclude some fields from the generated meta data field.
//    // excludeFields = ["developers", "funding"]
//
//    // Define the strict mode, will fail if the project uses licenses not allowed
//    // - This will only automatically fail for Android projects which have `registerAndroidTasks` enabled
//    // For non Android projects, execute `exportLibraryDefinitions`
//    strictMode = com.mikepenz.aboutlibraries.plugin.StrictMode.FAIL
//    // Allowed set of licenses, this project will be able to use without build failure
//    allowedLicenses = ["Apache-2.0", "asdkl"]
//    // Enable the duplication mode, allows to merge, or link dependencies which relate
//    duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.LINK
//    // Configure the duplication rule, to match "duplicates" with
//    duplicationRule = com.mikepenz.aboutlibraries.plugin.DuplicateRule.SIMPLE
//    // Enable pretty printing for the generated JSON file
//    prettyPrint = true
//
//    // Allows to only collect dependencies of specific variants during the `collectDependencies` step.
//    // filterVariants = ["debug"]
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}