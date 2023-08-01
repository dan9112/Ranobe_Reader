package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpResult
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

interface IDataSource {
    fun getUserInfo(login: String, password: String): SignInResult
    fun addUser(login: String, password: String, state: UserState): SignUpResult
    fun removeUser(id: Long): RemoveAccountResult
}
