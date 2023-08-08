package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.type_converters

import androidx.room.TypeConverter
import com.lord_markus.ranobe_reader.core.models.UserState

class UserStateTypeConverter {
    @TypeConverter
    fun fromUserState(state: UserState) = state.hashCode()

    @TypeConverter
    fun toUserState(hashCode: Int): UserState = UserState::class
        .sealedSubclasses
        .first { it.hashCode() == hashCode }
        .objectInstance!!
}
