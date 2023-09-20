package com.lord_markus.ranobe_reader.settings.domain.repository

import com.lord_markus.ranobe_reader.settings.domain.models.SettingsData

interface SettingsRepository {
    suspend fun setSettings(settingsData: SettingsData)
    suspend fun setDynamicColor(on: Boolean)
    suspend fun setNightMode(flag: Boolean?)
}
