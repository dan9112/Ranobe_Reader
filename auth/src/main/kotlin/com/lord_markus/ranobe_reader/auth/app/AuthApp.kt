package com.lord_markus.ranobe_reader.auth.app

import android.content.Context
import com.lord_markus.ranobe_reader.auth.di.dataModule
import com.lord_markus.ranobe_reader.auth.di.domainModule
import com.lord_markus.ranobe_reader.auth.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AuthApp {
    fun onCreate(context: Context) {
        startKoin {
            androidLogger()
            androidContext(androidContext = context)
            modules(
                dataModule,
                domainModule,
                presentationModule
            )
        }
    }
}