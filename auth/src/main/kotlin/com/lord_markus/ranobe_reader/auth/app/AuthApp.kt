package com.lord_markus.ranobe_reader.auth.app

import com.lord_markus.ranobe_reader.auth.di.dataModule
import com.lord_markus.ranobe_reader.auth.di.domainModule
import com.lord_markus.ranobe_reader.auth.di.presentationModule
import com.lord_markus.ranobe_reader.koin_core.app.KoinAppCore
import org.koin.core.KoinApplication

class AuthApp : KoinAppCore {
    override fun module(koinApplication: KoinApplication) = koinApplication.modules(
        dataModule,
        domainModule,
        presentationModule
    )
}
