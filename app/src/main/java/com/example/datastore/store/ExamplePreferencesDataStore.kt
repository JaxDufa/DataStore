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

package com.example.datastore.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DATA_STORE_NAME = "preferences_data_store"

interface ExamplePreferencesDataStore {

    val userFlow: Flow<UserInfo>

    val nameFlow: Flow<String>

    val nickNameFlow: Flow<String>

    val ageFlow: Flow<Int>

    val professionFlow: Flow<Profession>

    suspend fun readUser(): UserInfo

    suspend fun readName(): String

    suspend fun readNickName(): String

    suspend fun readAge(): Int

    suspend fun readProfession(): Profession

    suspend fun writeName(name: String)

    suspend fun writeNickName(nickName: String)

    suspend fun writeAge(age: Int)

    suspend fun writeProfession(profession: Profession)

    suspend fun clear()
}

class ExamplePreferencesDataStoreImpl(private val context: Context) : ExamplePreferencesDataStore {

    private object Keys {

        val NAME_KEY = stringPreferencesKey("name")
        val NICK_NAME_KEY = stringPreferencesKey("nick_name")
        val AGE_KEY = intPreferencesKey("age")
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

    // region - Flow
    override val userFlow: Flow<UserInfo>
        get() {
            return context.dataStore.data
                .handleException()
                .map { preferences ->
                    UserInfo(
                        name = preferences[Keys.NAME_KEY].orEmpty(),
                        nickName = preferences[Keys.NICK_NAME_KEY].orEmpty(),
                        age = preferences[Keys.AGE_KEY] ?: 0,
                        profession = preferences[Keys.PROFESSION_KEY]?.let { Profession.valueOf(it) } ?: Profession.OTHER
                    )
                }
        }

    override val nameFlow: Flow<String>
        get() {
            return context.dataStore.data
                .handleException()
                .map { preferences ->
                    preferences[Keys.NAME_KEY].orEmpty()
                }
        }

    override val nickNameFlow: Flow<String>
        get() {
            return context.dataStore.data
                .handleException()
                .map { preferences ->
                    preferences[Keys.NICK_NAME_KEY].orEmpty()
                }
        }

    override val ageFlow: Flow<Int>
        get() {
            return context.dataStore.data
                .handleException()
                .map { preferences ->
                    preferences[Keys.AGE_KEY] ?: 0
                }
        }

    override val professionFlow: Flow<Profession>
        get() {
            return context.dataStore.data
                .handleException()
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
        return nickNameFlow.first()
    }

    override suspend fun readAge(): Int {
        return ageFlow.first()
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

    override suspend fun writeNickName(nickName: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NICK_NAME_KEY] = nickName
        }
    }

    override suspend fun writeAge(age: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.AGE_KEY] = age
        }
    }

    override suspend fun writeProfession(profession: Profession) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PROFESSION_KEY] = profession.name
        }
    }
    // endregion

    // region - Clear
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