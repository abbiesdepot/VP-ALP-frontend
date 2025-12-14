package com.abbie.alpvp.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface UserRepositoryInterface {
    val currentUserToken: Flow<String>
    val currentUsername: Flow<String>
    val currentUserId: Flow<Int>

    suspend fun saveUserSession(token: String, username: String, userId: Int)
    suspend fun clearSession()
}

class UserRepository (
    private val userDataStore: DataStore<Preferences>
): UserRepositoryInterface {
    override val currentUserToken: Flow<String> = userDataStore.data.map { it[USER_TOKEN] ?: "" }
    override val currentUsername: Flow<String> = userDataStore.data.map { it[USERNAME] ?: "User" }
    override val currentUserId: Flow<Int> = userDataStore.data.map { it[USER_ID] ?: -1 }

    override suspend fun saveUserSession(token: String, username: String, userId: Int) {
        userDataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
            preferences[USERNAME] = username
            preferences[USER_ID] = userId
        }
    }

    override suspend fun clearSession() {
        userDataStore.edit { it.clear() }
    }

    private companion object {
        val USER_TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("username")
        val USER_ID = intPreferencesKey("user_id")
    }
}