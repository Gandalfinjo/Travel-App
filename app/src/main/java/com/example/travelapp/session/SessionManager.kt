package com.example.travelapp.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_USER_ID = intPreferencesKey("user_id")
    }

    val loggedInUsername: Flow<String?> = context.dataStore.data
        .map { it[KEY_USERNAME] }

    val loggedInUserId: Flow<Int?> = context.dataStore.data
        .map { it[KEY_USER_ID] }

    suspend fun saveSession(username: String, userId: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}