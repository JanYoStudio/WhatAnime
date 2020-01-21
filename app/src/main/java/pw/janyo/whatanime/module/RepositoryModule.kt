package pw.janyo.whatanime.module

import org.koin.dsl.module
import pw.janyo.whatanime.repository.AnimationRepository

val repositoryModule = module {
	single {
		AnimationRepository(get(), get(), get())
	}
}