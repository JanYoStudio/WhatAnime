package pw.janyo.whatanime.module

import org.koin.dsl.module
import pw.janyo.whatanime.Configure
import pw.janyo.whatanime.model.SearchPreferences
import pw.janyo.whatanime.repository.AnimationRepository
import pw.janyo.whatanime.repository.ImageSearchGateway
import pw.janyo.whatanime.repository.HistoryGateway
import pw.janyo.whatanime.repository.RepositoryImageSearchGateway

val repositoryModule = module {
    single { AnimationRepository() }
    single<HistoryGateway> { get<AnimationRepository>() }
    single { SearchPreferences(Configure.cutBorders, Configure.hideSex, Configure.preferWebp) }
    single<ImageSearchGateway> { RepositoryImageSearchGateway(get()) }
}