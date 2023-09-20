package com.lord_markus.ranobe_reader.app.domain.repository

import com.lord_markus.ranobe_reader.app.domain.models.SettingsData
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    val settingsDataFlow: Flow<SettingsData>
}
