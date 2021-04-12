/*
 * Copyright 2021 ArcTouch LLC.
 * All rights reserved.
 *
 * This file, its contents, concepts, methods, behavior, and operation
 * (collectively the "Software") are protected by trade secret, patent,
 * and copyright laws. The use of the Software is governed by a license
 * agreement. Disclosure of the Software to third parties, in any form,
 * in whole or in part, is expressly prohibited except as authorized by
 * the license agreement.
 */

package com.example.datastore.store.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.datastore.store.Profession
import com.example.datastore.store.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DATA_STORE_NAME = "preferences_data_store"

interface UserPreferencesDataStore {

    val userFlow: Flow<UserInfo>

    val nameFlow: Flow<String>

    val emailNameFlow: Flow<String>

    val codeFlow: Flow<Int>

    val professionFlow: Flow<Profession>

    suspend fun readUser(): UserInfo

    suspend fun readName(): String

    suspend fun readNickName(): String

    suspend fun readAge(): Int

    suspend fun readProfession(): Profession

    suspend fun writeName(name: String)

    suspend fun writeEmail(email: String)

    suspend fun writeCode(code: Int)

    suspend fun writeProfession(profession: Profession)

    suspend fun hasData(): Boolean

    suspend fun clear()
}

class UserPreferencesDataStoreImpl(private val context: Context) : UserPreferencesDataStore {

    private object Keys {

        val NAME_KEY = stringPreferencesKey("name")
        val EMAIL_KEY = stringPreferencesKey("email")
        val CODE_KEY = intPreferencesKey("code")
        val PROFESSION_KEY = stringPreferencesKey("profession")
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DATA_STORE_NAME,
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(context, USER_SHARED_PREFERENCES_NAME),
                SharedPreferencesMigration(context, USER_EXTRA_SHARED_PREFERENCES_NAME)
            )
        }
    )

    private val Context.safeData: Flow<Preferences>
        get() = dataStore.data.handleException()

    // region - Flow
    override val userFlow: Flow<UserInfo>
        get() {
            return context.safeData
                .map { preferences ->
                    UserInfo(
                        name = preferences[Keys.NAME_KEY].orEmpty(),
                        email = preferences[Keys.EMAIL_KEY].orEmpty(),
                        code = preferences[Keys.CODE_KEY] ?: 0,
                        profession = preferences[Keys.PROFESSION_KEY]?.let { Profession.valueOf(it) } ?: Profession.OTHER
                    )
                }
        }

    override val nameFlow: Flow<String>
        get() {
            return context.safeData
                .map { preferences ->
                    preferences[Keys.NAME_KEY].orEmpty()
                }
        }

    override val emailNameFlow: Flow<String>
        get() {
            return context.safeData
                .map { preferences ->
                    preferences[Keys.EMAIL_KEY].orEmpty()
                }
        }

    override val codeFlow: Flow<Int>
        get() {
            return context.safeData
                .map { preferences ->
                    preferences[Keys.CODE_KEY] ?: 0
                }
        }

    override val professionFlow: Flow<Profession>
        get() {
            return context.safeData
                .map { preferences ->
                    preferences[Keys.PROFESSION_KEY]?.let { Profession.valueOf(it) } ?: Profession.OTHER
                }
        }
    // endregion

    // region - Read
    override suspend fun readUser(): UserInfo {
        return userFlow.first()
    }

    override suspend fun readName(): String {
        return nameFlow.first()
    }

    override suspend fun readNickName(): String {
        return emailNameFlow.first()
    }

    override suspend fun readAge(): Int {
        return codeFlow.first()
    }

    override suspend fun readProfession(): Profession {
        return professionFlow.first()
    }
    // endregion

    // region - Write
    override suspend fun writeName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NAME_KEY] = name
        }
    }

    override suspend fun writeEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.EMAIL_KEY] = email
        }
    }

    override suspend fun writeCode(code: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CODE_KEY] = code
        }
    }

    override suspend fun writeProfession(profession: Profession) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PROFESSION_KEY] = profession.name
        }
    }
    // endregion

    // region - Others
    override suspend fun hasData(): Boolean {
        return context.dataStore.data
            .handleException()
            .map { it.asMap().isEmpty() }
            .first()
    }

    override suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    // endregion

    private fun Flow<Preferences>.handleException(): Flow<Preferences> {
        return catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
    }
}