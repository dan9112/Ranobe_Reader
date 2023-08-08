package com.lord_markus.ranobe_reader.app.app

import android.app.Application
import com.lord_markus.ranobe_reader.auth.app.AuthApp
import com.lord_markus.ranobe_reader.koin_core.models.KoinAppBase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp// Не нашёл способа добавить Hilt без использования библиотеки в модулях, где он не нужен!
class App : Application() {
    private val koinApp = KoinAppBase(AuthApp())
    override fun onCreate() {
        super.onCreate()
        koinApp.onCreate(context = this)
    }
}
