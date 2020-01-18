package pw.janyo.whatanime.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import pw.janyo.whatanime.repository.AnimationRepository

val repositoryModule = module {
	single {
		AnimationRepository(get(named("base")), get(named("cloud")), get())
	}
}