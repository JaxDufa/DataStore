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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

private const val DATA_STORE_FILE_NAME = "user_preferences.pb"
private const val USER_PREFERENCES_MIGRATION_NAME = "preferences_data_store"
private const val PROFESSION_MIGRATION_KEY = "profession"

interface ExampleProtoDataStore {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun updateName(name: String)

    suspend fun updateNickName(nickName: String)

    suspend fun updateAge(age: Int)

    suspend fun updateProfession(profession: UserPreferences.Profession)
}

class ExampleProtoDataStoreImpl(private val context: Context) : ExampleProtoDataStore {

    private val Context.dataStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UserPreferencesSerializer,
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(
                    context,
                    USER_PREFERENCES_MIGRATION_NAME
                ) { sharedPrefs: SharedPreferencesView, currentData: UserPreferences ->
                    if (currentData.profession == UserPreferences.Profession.UNSPECIFIED) {
                        currentData.toBuilder().setProfession(
                            UserPreferences.Profession.valueOf(
                                sharedPrefs.getString(PROFESSION_MIGRATION_KEY, UserPreferences.Profession.OTHER.name)!!
                            )
                        ).build()
                    } else {
                        currentData
                    }
                }
            )
        }
    )

    override val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.handleException()

    override suspend fun updateName(name: String) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setName(name)
                .build()
        }
    }

    override suspend fun updateNickName(nickName: String) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setNickName(nickName)
                .build()
        }
    }

    override suspend fun updateAge(age: Int) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setAge(age)
                .build()
        }
    }

    override suspend fun updateProfession(profession: UserPreferences.Profession) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .setProfession(profession)
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