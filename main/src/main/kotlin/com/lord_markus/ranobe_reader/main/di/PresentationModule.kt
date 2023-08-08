package com.lord_markus.ranobe_reader.main.di

import com.lord_markus.ranobe_reader.main.data.repository.RepositoryImpl
import com.lord_markus.ranobe_reader.main.domain.repository.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {
    @Singleton
    @Provides
    fun getRepository(): Repository = RepositoryImpl()
}
