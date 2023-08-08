package com.lord_markus.ranobe_reader.core.models

import java.io.Serializable

data class UserInfo(val id: Long, val state: UserState) : Serializable
