package com.lord_markus.ranobe_reader.koin_core.app

import org.koin.core.KoinApplication

interface KoinAppCore {
    fun module(koinApplication: KoinApplication): KoinApplication
}
