package com.lord_markus.ranobe_reader.auth.di

import com.lord_markus.ranobe_reader.auth.data.repository.RepositoryImpl
import com.lord_markus.ranobe_reader.auth.data.storage.implementation.DataSource
import com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.AppDatabase
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<IAppDatabase> { AppDatabase.create(androidContext()) }

    single {
        get<IAppDatabase>().tableUserDao()
    }
    single {
        get<IAppDatabase>().tableUserInfoDao()
    }
    single {
        get<IAppDatabase>().tableUserAuthStateDao()
    }
    single<IDataSource> {
        DataSource(
            tableUsersDao = get(),
            tableUserInfoDao = get(),
            tableUserAuthState = get(),
            database = get(),
            context = androidContext()
        )
    }
    single<Repository> {
        RepositoryImpl(dataSource = get())
    }
}
