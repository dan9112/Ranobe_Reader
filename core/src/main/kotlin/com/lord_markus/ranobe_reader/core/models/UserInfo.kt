package com.lord_markus.ranobe_reader.core.models

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: Long,
    val name: String,
    @Serializable
    val state: UserState
) : java.io.Serializable
