package com.lord_markus.ranobe_reader.data.di

import android.content.Context
import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.auth_core.domain.repository.AuthCoreRepository
import com.lord_markus.ranobe_reader.data.repository.RepositoryImpl
import com.lord_markus.ranobe_reader.data.storage.implementation.DataSource
import com.lord_markus.ranobe_reader.data.storage.implementation.db.AppDatabase
import com.lord_markus.ranobe_reader.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.data.storage.template.db.dao.ITableUserAuthStateDao
import com.lord_markus.ranobe_reader.data.storage.template.db.dao.ITableUserDao
import com.lord_markus.ranobe_reader.data.storage.template.db.dao.ITableUserInfoDao
import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun getAppDataBase(@ApplicationContext context: Context): IAppDatabase = AppDatabase.create(context)

    @Singleton
    @Provides
    fun getTableUserDao(appDatabase: IAppDatabase) = appDatabase.tableUserDao()

    @Singleton
    @Provides
    fun getTableUserInfoDao(appDatabase: IAppDatabase) = appDatabase.tableUserInfoDao()

    @Singleton
    @Provides
    fun getTableUserAuthStateDao(appDatabase: IAppDatabase) = appDatabase.tableUserAuthStateDao()

    @Singleton
    @Provides
    fun getDataSource(
        tableUsersDao: ITableUserDao,
        tableUserInfoDao: ITableUserInfoDao,
        tableUserAuthState: ITableUserAuthStateDao,
        database: IAppDatabase,
        @ApplicationContext context: Context
    ): IDataSource = DataSource(tableUsersDao, tableUserInfoDao, tableUserAuthState, database, context)

    @Singleton
    private fun getRepository(dataSource: IDataSource) = RepositoryImpl(dataSource)

    @Provides
    fun getAuthCoreRepository(dataSource: IDataSource): AuthCoreRepository = getRepository(dataSource)

    @Provides
    fun getAuthRepository(dataSource: IDataSource): AuthRepository = getRepository(dataSource)

    @Provides
    fun getMainRepository(dataSource: IDataSource): MainRepository = getRepository(dataSource)
}
