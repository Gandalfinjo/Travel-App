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

/**
 * Extension property that provides a [DataStore] instance for storing session preferences.
 * Uses a single instance per [Context] with the name "session".
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Manages user session persistence using Jetpack DataStore.
 *
 * Handles saving, retrieving and clearing the logged-in user's session data.
 * Session data is persisted across app launches until explicitly cleared.
 */
@Singleton
class SessionManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        /** DataStore key for storing the logged-in user's username. */
        val KEY_USERNAME = stringPreferencesKey("username")

        /** DataStore key for storing the logged-in user's ID. */
        val KEY_USER_ID = intPreferencesKey("user_id")

        /** DataStore key for storing the logged-in user's first name */
        val KEY_FIRSTNAME = stringPreferencesKey("firstname")

        /** DataStore key for storing the logged-in user's last name */
        val KEY_LASTNAME = stringPreferencesKey("lastname")

        /** DataStore key for storing the logged-in user's profile picture path */
        val KEY_PROFILE_PICTURE = stringPreferencesKey("profile_picture_path")

        /** DataStore key for storing the logged-in user's default currency */
        val KEY_DEFAULT_CURRENCY = stringPreferencesKey("default_currency")

        /** DataStore key for storing the theme preference */
        val KEY_THEME = stringPreferencesKey("theme")
    }

    /** Emits the currently logged-in user's username, or null if no session exists. */
    val loggedInUsername: Flow<String?> = context.dataStore.data
        .map { it[KEY_USERNAME] }

    /** Emits the currently logged-in user's ID, or null if no session exists. */
    val loggedInUserId: Flow<Int?> = context.dataStore.data
        .map { it[KEY_USER_ID] }

    /** Emits the currently logged-in user's first name, or null if no session exists. */
    val loggedInUserFirstname: Flow<String?> = context.dataStore.data
        .map { it[KEY_FIRSTNAME] }

    /** Emits the currently logged-in user's last name, or null if no session exists. */
    val loggedInUserLastname: Flow<String?> = context.dataStore.data
        .map { it[KEY_LASTNAME] }

    /** Emits the currently logged-in user's profile picture path, or null if no session exists. */
    val loggedInUserProfilePicture: Flow<String?> = context.dataStore.data
        .map { it[KEY_PROFILE_PICTURE] }

    /** Emits the currently logged-in user's default currency, or null if no session exists. */
    val defaultCurrency: Flow<String> = context.dataStore.data
        .map { it[KEY_DEFAULT_CURRENCY] ?: "EUR" }

    /** Emits the current theme preference */
    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { prefs ->
            when (prefs[KEY_THEME]) {
                "LIGHT" -> ThemePreference.LIGHT
                "DARK" -> ThemePreference.DARK
                else -> ThemePreference.SYSTEM
            }
        }

    /**
     * Persists the user's session data to DataStore.
     *
     * @param username Username of the logged-in user
     * @param userId ID of the logged-in user
     * @param firstname First name of the logged-in user
     * @param lastname Last name of the logged-in user
     * @param profilePicturePath Path to the logged-in user's profile picture
     */
    suspend fun saveSession(
        username: String,
        userId: Int,
        firstname: String,
        lastname: String,
        profilePicturePath: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_USER_ID] = userId
            prefs[KEY_FIRSTNAME] = firstname
            prefs[KEY_LASTNAME] = lastname

            if (profilePicturePath != null) {
                prefs[KEY_PROFILE_PICTURE] = profilePicturePath
            }
            else {
                prefs.remove(KEY_PROFILE_PICTURE)
            }
        }
    }

    /**
     *
     * Persists the default currency session data to DataStore.
     *
     * @param currency Selected currency preference
     */
    suspend fun saveDefaultCurrency(currency: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DEFAULT_CURRENCY] = currency
        }
    }

    /**
     * Persists the theme preference session data to DataStore.
     *
     * @param theme Selected theme preference
     */
    suspend fun saveThemePreference(theme: ThemePreference) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }


    /**
     * Clears all session data from DataStore.
     *
     * Should be called on logout to ensure the user is not
     * automatically logged in on the next app launch.
     */
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}