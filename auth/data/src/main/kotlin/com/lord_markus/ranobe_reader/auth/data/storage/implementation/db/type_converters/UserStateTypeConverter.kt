package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.type_converters

import androidx.room.TypeConverter
import com.lord_markus.ranobe_reader.core.models.UserState

class UserStateTypeConverter {
    @TypeConverter
    fun fromUserState(state: UserState) = state.number

    @TypeConverter
    fun toUserState(number: Short) =
        if (number == UserState.Admin.number) UserState.Admin else UserState.User
}
