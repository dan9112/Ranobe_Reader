package com.lord_markus.ranobe_reader.koin_core.app

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KoinAppBase(vararg koinApps: KoinAppCore) {
    private val koinApps: Array<out KoinAppCore>
    fun onCreate(context: Context) {
        startKoin {
            androidLogger()
            androidContext(androidContext = context)
            koinApps.forEach { it.module(koinApplication = this) }
        }
    }

    init {
        this.koinApps = koinApps
    }
}
