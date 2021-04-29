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

import android.util.Log
import com.example.datastore.store.database.UserRoomRepository
import com.example.datastore.store.preferences.UserPreferencesDataStore
import com.example.datastore.store.preferences.UserSharedPreferences
import com.example.datastore.store.proto.UserProtoPreferencesDataStore
import com.example.datastore.store.proto.UsersProtoPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val TAG = "UserRepository"

interface UserRepository {

    enum class Method {
        SHARED_PREFERENCES, DATA_STORE_PREFERENCES, PROTO_DATA_STORE, ADVANCED_PROTO_DATA_STORE, ROOM_DATABASE
    }

    var method: Method

    suspend fun observeUsers(): Flow<List<UserInfo>>

    suspend fun readUsers(): List<UserInfo>

    suspend fun readUser(code: Int? = null): UserInfo

    suspend fun editUser(code: Int? = null, name: String? = null, email: String? = null, profession: Profession? = null)

    suspend fun addUser(user: UserInfo)

    suspend fun removeUser(code: Int)

    suspend fun clear()
}

class UserRepositoryImpl(
    private val sharedPreferences: UserSharedPreferences,
    private val dataStorePreferencesDataStore: UserPreferencesDataStore,
    private val protoPreferencesDataStore: UserProtoPreferencesDataStore,
    private val advancedProtoPreferencesDataStore: UsersProtoPreferencesDataStore,
    private val roomDatabase: UserRoomRepository
) : UserRepository {

    override var method: UserRepository.Method = UserRepository.Method.ADVANCED_PROTO_DATA_STORE

    override suspend fun observeUsers(): Flow<List<UserInfo>> {
        return when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> sharedPreferences.userFlow.map { listOf(it) }
            UserRepository.Method.DATA_STORE_PREFERENCES -> dataStorePreferencesDataStore.userFlow.map { listOf(it) }
            UserRepository.Method.PROTO_DATA_STORE -> protoPreferencesDataStore.userFlow.map { listOf(it) }
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> advancedProtoPreferencesDataStore.usersPreferencesFlow
            UserRepository.Method.ROOM_DATABASE -> roomDatabase.users
        }.onEach { Log.d(TAG, "${method.name}: Update $it") }
    }

    override suspend fun readUsers(): List<UserInfo> {
        return when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> listOf(sharedPreferences.readUser())
            UserRepository.Method.DATA_STORE_PREFERENCES -> listOf(dataStorePreferencesDataStore.readUser())
            UserRepository.Method.PROTO_DATA_STORE -> listOf(protoPreferencesDataStore.readUser())
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> advancedProtoPreferencesDataStore.usersPreferencesFlow.first()
            UserRepository.Method.ROOM_DATABASE -> roomDatabase.users.first()
        }.apply {
            Log.d(TAG, "${method.name}: Read $this")
        }
    }

    override suspend fun readUser(code: Int?): UserInfo {
        return when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> sharedPreferences.readUser()
            UserRepository.Method.DATA_STORE_PREFERENCES -> dataStorePreferencesDataStore.readUser()
            UserRepository.Method.PROTO_DATA_STORE -> protoPreferencesDataStore.readUser()
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> advancedProtoPreferencesDataStore.usersPreferencesFlow.map { users ->
                code?.let { users.firstOrNull { it.code == code } } ?: users.first()
            }.first()
            UserRepository.Method.ROOM_DATABASE -> roomDatabase.users.map { users ->
                code?.let { users.firstOrNull { it.code == code } } ?: users.first()
            }.first()
        }.apply {
            Log.d(TAG, "${method.name}: Read $this")
        }
    }

    override suspend fun addUser(user: UserInfo) {
        when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> {
                with(sharedPreferences) {
                    writeName(user.name)
                    writeEmail(user.email)
                    writeCode(user.code)
                    writeProfession(user.profession)
                }
            }
            UserRepository.Method.DATA_STORE_PREFERENCES -> {
                with(dataStorePreferencesDataStore) {
                    writeName(user.name)
                    writeEmail(user.email)
                    writeCode(user.code)
                    writeProfession(user.profession)
                }
            }
            UserRepository.Method.PROTO_DATA_STORE -> {
                with(protoPreferencesDataStore) {
                    writeName(user.name)
                    writeEmail(user.email)
                    writeCode(user.code)
                    writeProfession(user.profession)
                }
            }
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> advancedProtoPreferencesDataStore.addUser(user)
            UserRepository.Method.ROOM_DATABASE -> roomDatabase.insert(user)
        }
        Log.d(TAG, "${method.name}: Add $user")
    }

    override suspend fun editUser(code: Int?, name: String?, email: String?, profession: Profession?) {
        when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> {
                with(sharedPreferences) {
                    name?.let { writeName(it) }
                    email?.let { writeEmail(it) }
                    profession?.let { writeProfession(it) }
                }
            }
            UserRepository.Method.DATA_STORE_PREFERENCES -> {
                with(dataStorePreferencesDataStore) {
                    name?.let { writeName(it) }
                    email?.let { writeEmail(it) }
                    profession?.let { writeProfession(it) }
                }
            }
            UserRepository.Method.PROTO_DATA_STORE -> {
                with(protoPreferencesDataStore) {
                    name?.let { writeName(it) }
                    email?.let { writeEmail(it) }
                    profession?.let { writeProfession(it) }
                }
            }
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> {
                code?.let {
                    val user = readUser(it)
                    advancedProtoPreferencesDataStore.replaceUser(
                        UserInfo(name ?: user.name, email ?: user.email, user.code, profession ?: user.profession)
                    )
                }
            }
            UserRepository.Method.ROOM_DATABASE -> {
                code?.let {
                    val user = readUser(it)
                    roomDatabase.insertOrReplace(
                        UserInfo(name ?: user.name, email ?: user.email, user.code, profession ?: user.profession)
                    )
                }
            }
        }
        Log.d(TAG, "${method.name}: Edit $code")
    }

    override suspend fun removeUser(code: Int) {
        Log.d(TAG, "${method.name}: Remove $code")
        when (method) {
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> advancedProtoPreferencesDataStore.removeUser(code)
            UserRepository.Method.ROOM_DATABASE -> roomDatabase.removeUser(code)
            else -> clear()
        }
    }

    override suspend fun clear() {
        when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> sharedPreferences.clear()
            UserRepository.Method.DATA_STORE_PREFERENCES -> dataStorePreferencesDataStore.clear()
            UserRepository.Method.PROTO_DATA_STORE -> protoPreferencesDataStore.clear()
            UserRepository.Method.ADVANCED_PROTO_DATA_STORE -> advancedProtoPreferencesDataStore.clear()
            UserRepository.Method.ROOM_DATABASE -> roomDatabase.clear()
        }
        Log.d(TAG, "${method.name}: Clear")
    }
}