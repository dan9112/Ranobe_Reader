package com.lord_markus.ranobe_reader.app.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp// Не нашёл способа добавить Hilt без использования библиотеки в модулях, где он не нужен!
class App : Application()
