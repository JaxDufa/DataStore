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
import com.example.datastore.UsersPreferences
import com.example.datastore.store.Profession
import com.example.datastore.store.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DATA_STORE_FILE_NAME = "users_preferences.pb"

interface UsersProtoPreferencesDataStore {

    val usersPreferencesFlow: Flow<List<UserInfo>>

    suspend fun addUser(user: UserInfo)

    suspend fun replaceUser(user: UserInfo)

    suspend fun removeUser(code: Int)

    suspend fun clear()
}

class UsersProtoPreferencesDataStoreImpl(private val context: Context) : UsersProtoPreferencesDataStore {

    private val Context.dataStore: DataStore<UsersPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = UsersPreferencesSerializer
    )

    override val usersPreferencesFlow: Flow<List<UserInfo>> = context.dataStore.data
        .handleException()
        .map {
            it.usersOrBuilderList.map { userPref ->
                UserInfo(
                    name = userPref.name,
                    email = userPref.email,
                    code = userPref.code,
                    profession = Profession.valueOf(userPref.profession.name)
                )
            }
        }

    override suspend fun addUser(user: UserInfo) {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .addUsers(
                    UsersPreferences.User.newBuilder().apply {
                        name = user.name
                        email = user.email
                        code = user.code
                        profession = UsersPreferences.User.Profession.valueOf(user.profession.name)
                    }
                )
                .build()
        }
    }

    override suspend fun replaceUser(user: UserInfo) {
        context.dataStore.updateData { preferences ->
            val index = preferences.usersList.indexOfFirst { it.code == user.code }
            preferences.toBuilder()
                .setUsers(
                    index,
                    UsersPreferences.User.newBuilder().apply {
                        this.name = user.name
                        this.email = user.email
                        this.code = user.code
                        this.profession = UsersPreferences.User.Profession.valueOf(user.profession.name)
                    }
                )
                .build()
        }
    }

    override suspend fun removeUser(code: Int) {
        context.dataStore.updateData { preferences ->
            val index = preferences.usersList.indexOfFirst { it.code == code }
            preferences.toBuilder()
                .removeUsers(index)
                .build()
        }
    }

    override suspend fun clear() {
        context.dataStore.updateData { preferences ->
            preferences.toBuilder()
                .clearUsers()
                .build()
        }
    }

    private fun Flow<UsersPreferences>.handleException(): Flow<UsersPreferences> {
        return catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(UsersPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }
    }
}