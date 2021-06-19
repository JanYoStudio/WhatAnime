dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://nexus3.mystery0.vip/repository/maven-public/")
        google()
        jcenter() // Warning: this repository is going to shut down soon
        mavenCentral()
    }
}
rootProject.name = "WhatAnime"
include(":app")
 