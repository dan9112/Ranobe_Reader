package com.lord_markus.ranobe_reader.main.domain.repository

import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain

interface MainRepository {
    suspend fun signOut(): SignOutResultMain
    suspend fun signOutWithRemove(): SignOutResultMain
    suspend fun setCurrent(id: Long): SetCurrentResultMain

}
