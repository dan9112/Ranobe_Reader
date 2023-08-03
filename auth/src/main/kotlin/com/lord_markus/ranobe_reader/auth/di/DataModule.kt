package com.lord_markus.ranobe_reader.auth.di


object DataModule {
    /*@Singleton
    @Provides
    fun provideRepository(dataSource: IDataSource): Repository = RepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideDataSource(
        tableUsersDao: ITableUserDao,
        tableUserInfoDao: ITableUserInfoDao,
        tableUserAuthState: ITableUserAuthStateDao,
        database: IAppDatabase,
        @ApplicationContext context: Context
    ): IDataSource =
        DataSource(tableUsersDao, tableUserInfoDao, tableUserAuthState, database, context)

    @Singleton
    @Provides
    fun provideTableUserDao(database: IAppDatabase) = database.tableUserDao()

    @Singleton
    @Provides
    fun provideTableUserInfoDao(database: IAppDatabase) = database.tableUserInfoDao()

    @Singleton
    @Provides
    fun provideTableUserAuthStateDao(database: IAppDatabase) = database.tableUserAuthStateDao()

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext app: Context) = AppDatabase.create(context = app)*/
}
