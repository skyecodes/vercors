package app.vercors.feature.instance.data

import app.vercors.feature.instance.data.datasource.InstanceDataSource
import app.vercors.feature.instance.data.repository.InstanceRepositoryImpl
import app.vercors.feature.instance.domain.instance.InstanceRepository
import org.koin.dsl.bind
import org.koin.dsl.module

// TODO: Why is the module not generating properly anymore? WTF
class InstanceDataModule {
    val module = module {
        single { InstanceDataSource(get(), get()) }
        single { InstanceRepositoryImpl(get(), get()) } bind InstanceRepository::class
    }
}
