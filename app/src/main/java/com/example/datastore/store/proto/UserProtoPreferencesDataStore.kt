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

package com.example.datastore.store.proto

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import com.example.datastore.UserPreferences
import com.example.datastore.store.Profession
import com.example.datastore.store.UserInfo
import com.example.datastore.store.preferences.USER_SHARED_PREFERENCES_NAME
import com.example.datastore.store.preferences.UserSharedPreferencesImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DATA_STORE_FILE_NAME = "user_preferences.pb"

interface UserProtoPreferencesDataStore {

    val userFlow: Flow<UserInfo>

    suspend fun readUser() : UserInfo

    suspend fun writeName(name: String)

    suspend fun writeEmail(email: String)

    suspend fun writeCode(code: Int)

    suspend fun writeProfession(profession: Profession)

    suspend fun clear()
}

@ExperimentalCoroutinesApi
class UserProtoPreferencesDataStoreImpl(private val context: Context) : UserProtoPreferencesDataStore {

    private val Context.dataStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer,
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(
                    context,
                    USER_SHARED_PREFERENCES_NAME
                ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
                    if (currentData.profession == UserPreferences.Profession.UNSPECIFIED) {
                        val keys = UserSharedPreferencesImpl.Keys
                        currentData.toBuilder()
                            .setName(sharedPrefs.getString(keys.NAME_KEY))
                            .setEmail(sharedPrefs.getString(keys.EMAIL_KEY))
                            .setCode(sharedPrefs.getInt(keys.CODE_KEY, 0))
                            .setProfession(
                                UserPreferences.Profession.valueOf(
                                    sharedPrefs.getString(keys.PROFESSION_KEY, UserPreferences.Profession.OTHER.name)!!
                                )
                            ).build()
                    } else {
                        currentData
                    }
                }
            )
        }
    )

    override val userFlow: Flow<UserInfo> = context.dataStore.data
        .handleException()
        .map {
            UserInfo(
                name = it.name,
                email = it.email,
                code = it.code,
                profession = if (it.profession == UserPreferences.Profession.UNSPECIFIED) {
                    Profession.OTHER
                } else {
                    Profession.valueOf(it.profession.name)
                }
            )
        }

    override suspend fun readUser(): UserInfo {
        return userFlow.first()
    }

    override suspend fun writeName(name: String) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setName(name)
                .build()
        }
    }

    override suspend fun writeEmail(email: String) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setEmail(email)
                .build()
        }
    }

    override suspend fun writeCode(code: Int) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setCode(code)
                .build()
        }
    }

    override suspend fun writeProfession(profession: Profession) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setProfession(UserPreferences.Profession.valueOf(profession.name))
                .build()
        }
    }

    override suspend fun clear() {
        context.dataStore.updateData { preferences ->
            preferences
                .toBuilder()
                .clear()
                .build()
        }
    }

    private fun Flow<UserPreferences>.handleException(): Flow<UserPreferences> {
        return catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }
    }
}