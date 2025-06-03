package pw.janyo.whatanime

import org.koin.core.context.startKoin
import pw.janyo.whatanime.module.moduleList

fun initKoin(){
    startKoin {
        modules(moduleList())
    }
}