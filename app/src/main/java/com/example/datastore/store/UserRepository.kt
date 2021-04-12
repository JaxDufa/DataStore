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
import com.example.datastore.store.preferences.UserPreferencesDataStore
import com.example.datastore.store.preferences.UserSharedPreferences
import com.example.datastore.store.proto.UserProtoPreferencesDataStore
import com.example.datastore.store.proto.UsersProtoPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

private const val TAG = "UserRepository"

interface UserRepository {

    enum class Method {
        SHARED_PREFERENCES, DATA_STORE_PREFERENCES, PROTO_DATA_STORE, LIST_PROTO_DATA_STORE
    }

    var method: Method

    suspend fun observeUsers(): Flow<List<UserInfo>>

    suspend fun readUsers(): List<UserInfo>

    suspend fun readUser(index: Int? = null): UserInfo

    suspend fun writeUser(index: Int? = null, name: String? = null, email: String? = null, profession: Profession? = null)

    suspend fun addUser(user: UserInfo)

    suspend fun removeUser(index: Int)

    suspend fun clear()

    fun release()
}

class UserRepositoryImpl(
    private val sharedPreferences: UserSharedPreferences,
    private val dataStorePreferencesDataStore: UserPreferencesDataStore,
    private val protoPreferencesDataStore: UserProtoPreferencesDataStore,
    private val listProtoPreferencesDataStore: UsersProtoPreferencesDataStore
) : UserRepository {

    override var method: UserRepository.Method = UserRepository.Method.LIST_PROTO_DATA_STORE

    override suspend fun observeUsers(): Flow<List<UserInfo>> {
        return when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> {
                flow {
                    sharedPreferences.registerListener { user ->
                        runBlocking {
                            this@flow.emit(listOf(user))
                        }
                    }
                }
            }
            UserRepository.Method.DATA_STORE_PREFERENCES -> dataStorePreferencesDataStore.userFlow.map { listOf(it) }
            UserRepository.Method.PROTO_DATA_STORE -> protoPreferencesDataStore.userFlow.map { listOf(it) }
            UserRepository.Method.LIST_PROTO_DATA_STORE -> listProtoPreferencesDataStore.usersPreferencesFlow
        }.onEach { Log.d(TAG, "${method.name}: Update $it") }
    }

    override suspend fun readUsers(): List<UserInfo> {
        return when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> listOf(sharedPreferences.readUser())
            UserRepository.Method.DATA_STORE_PREFERENCES -> listOf(dataStorePreferencesDataStore.readUser())
            UserRepository.Method.PROTO_DATA_STORE -> listOf(protoPreferencesDataStore.readUser())
            UserRepository.Method.LIST_PROTO_DATA_STORE -> listProtoPreferencesDataStore.usersPreferencesFlow.first()
        }.apply {
            Log.d(TAG, "${method.name}: Read $this")
        }
    }

    override suspend fun readUser(index: Int?): UserInfo {
        return when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> sharedPreferences.readUser()
            UserRepository.Method.DATA_STORE_PREFERENCES -> dataStorePreferencesDataStore.readUser()
            UserRepository.Method.PROTO_DATA_STORE -> protoPreferencesDataStore.readUser()
            UserRepository.Method.LIST_PROTO_DATA_STORE -> listProtoPreferencesDataStore.usersPreferencesFlow.map { users ->
                index?.let { users[it] } ?: users.first()
            }.first()
        }.apply {
            Log.d(TAG, "${method.name}: Read $this")
        }
    }

    override suspend fun writeUser(index: Int?, name: String?, email: String?, profession: Profession?) {
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
                    name?.let { updateName(it) }
                    email?.let { updateEmail(it) }
                    profession?.let { updateProfession(it) }
                }
            }
            UserRepository.Method.LIST_PROTO_DATA_STORE -> {
                with(listProtoPreferencesDataStore) {
                    index?.let {
                        editUser(
                            it,
                            name,
                            email,
                            profession
                        )
                    }
                }
            }
        }
        Log.d(TAG, "${method.name}: Edit $index")
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
                    updateName(user.name)
                    updateEmail(user.email)
                    updateCode(user.code)
                    updateProfession(user.profession)
                }
            }
            UserRepository.Method.LIST_PROTO_DATA_STORE -> listProtoPreferencesDataStore.addUser(user)
        }
        Log.d(TAG, "${method.name}: Add $user")
    }

    override suspend fun removeUser(index: Int) {
        when (method) {
            UserRepository.Method.LIST_PROTO_DATA_STORE -> {
                listProtoPreferencesDataStore.removeUser(index)
                Log.d(TAG, "${method.name}: Remove $index")
            }
            else -> clear()
        }
    }

    override suspend fun clear() {
        when (method) {
            UserRepository.Method.SHARED_PREFERENCES -> sharedPreferences.clear()
            UserRepository.Method.DATA_STORE_PREFERENCES -> dataStorePreferencesDataStore.clear()
            UserRepository.Method.PROTO_DATA_STORE -> protoPreferencesDataStore.clear()
            UserRepository.Method.LIST_PROTO_DATA_STORE -> listProtoPreferencesDataStore.clear()
        }
        Log.d(TAG, "${method.name}: Clear")
    }

    override fun release() {
        sharedPreferences.unregisterListener()
    }
}