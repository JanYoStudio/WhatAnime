plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialize) apply false
    alias(libs.plugins.kotlinKsp) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.ktorfit) apply false
    alias(libs.plugins.room) apply false
}