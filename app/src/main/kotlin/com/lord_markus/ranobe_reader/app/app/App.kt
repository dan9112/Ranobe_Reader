package com.lord_markus.ranobe_reader.app.app

import android.app.Application
import com.lord_markus.ranobe_reader.auth.app.AuthApp

class App : Application() {
    private val authApp = AuthApp()
    override fun onCreate() {
        super.onCreate()
        authApp.onCreate(context = this)
    }
}
