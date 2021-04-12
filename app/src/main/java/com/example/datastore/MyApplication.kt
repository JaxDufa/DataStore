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

package com.example.datastore

import android.app.Application
import com.example.datastore.store.UserRepository
import com.example.datastore.store.UserRepositoryImpl
import com.example.datastore.store.preferences.UserPreferencesDataStore
import com.example.datastore.store.preferences.UserPreferencesDataStoreImpl
import com.example.datastore.store.preferences.UserRxPreferencesDataStore
import com.example.datastore.store.preferences.UserRxPreferencesDataStoreImpl
import com.example.datastore.store.preferences.UserSharedPreferences
import com.example.datastore.store.preferences.UserSharedPreferencesImpl
import com.example.datastore.store.proto.UserProtoPreferencesDataStore
import com.example.datastore.store.proto.UserProtoPreferencesDataStoreImpl
import com.example.datastore.store.proto.UsersProtoPreferencesDataStore
import com.example.datastore.store.proto.UsersProtoPreferencesDataStoreImpl
import com.example.datastore.user.add.AddUserViewModel
import com.example.datastore.user.edit.EditUserViewModel
import com.example.datastore.user.list.UserListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class MyApplication : Application() {

    private val appModule = module {
        single<UserSharedPreferences> { UserSharedPreferencesImpl(androidContext()) }

        single<UserRxPreferencesDataStore> { UserRxPreferencesDataStoreImpl(androidContext()) }
        single<UserPreferencesDataStore> { UserPreferencesDataStoreImpl(androidContext()) }

        single<UserProtoPreferencesDataStore> { UserProtoPreferencesDataStoreImpl(androidContext()) }
        single<UsersProtoPreferencesDataStore> { UsersProtoPreferencesDataStoreImpl(androidContext()) }

        single<UserRepository> { UserRepositoryImpl(get(), get(), get(), get()) }

        viewModel { AddUserViewModel(get()) }
        viewModel { UserListViewModel(get()) }
        viewModel { (userIndex: Int) -> EditUserViewModel(userIndex, get()) }
    }

    override fun onCreate() {
        super.onCreate()


        startKoin {
            // Android context
            androidContext(this@MyApplication)
            androidLogger(Level.DEBUG)

            // Modules
            modules(appModule)
        }
    }
}