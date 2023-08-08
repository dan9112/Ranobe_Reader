package com.lord_markus.ranobe_reader.koin_core.models

import org.koin.core.KoinApplication

interface KoinAppCore {
    fun module(koinApplication: KoinApplication): KoinApplication
}
