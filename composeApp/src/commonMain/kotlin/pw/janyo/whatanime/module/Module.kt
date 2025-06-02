package pw.janyo.whatanime.module

import org.koin.core.module.Module

fun moduleList(): List<Module> =
    listOf(
        platformModule(),
        databaseModule,
        networkModule,
        viewModelModule,
        repositoryModule,
    )