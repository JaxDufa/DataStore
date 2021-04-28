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

package com.example.datastore.store.database

import androidx.annotation.WorkerThread
import com.example.datastore.store.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class UserRoomRepository(private val userDao: UserDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val users: Flow<List<UserInfo>> = userDao.getAlphabetizedUsers().map { list ->
        list.map {
            UserInfo(
                name = it.name,
                email = it.email,
                code = it.code,
                profession = it.profession
            )
        }
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(user: UserInfo) {
        userDao.insert(
            UserEntity(
                name = user.name,
                email = user.email,
                code = user.code,
                profession = user.profession
            )
        )
    }

    @WorkerThread
    suspend fun insertOrReplace(user: UserInfo) {
        userDao.insertOrReplace(
            UserEntity(
                name = user.name,
                email = user.email,
                code = user.code,
                profession = user.profession
            )
        )
    }

    suspend fun removeUser(code: Int) = userDao.delete(code)

    suspend fun clear() = userDao.deleteAll()
}