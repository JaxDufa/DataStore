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
import com.example.datastore.store.ExamplePreferencesDataStore
import com.example.datastore.store.ExamplePreferencesDataStoreImpl
import com.example.datastore.store.ExampleRxPreferencesDataStore
import com.example.datastore.store.ExampleRxPreferencesDataStoreImpl
import com.example.datastore.store.ExampleSharedPreferences
import com.example.datastore.store.ExampleSharedPreferencesImpl
import com.example.datastore.store.proto.ExampleProtoDataStore
import com.example.datastore.store.proto.ExampleProtoDataStoreImpl
import com.example.datastore.viewmodel.ExampleViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

class MyApplication : Application() {

    private val appModule =  module {
        single<ExampleSharedPreferences> { ExampleSharedPreferencesImpl(androidContext()) }
        single<ExampleRxPreferencesDataStore> { ExampleRxPreferencesDataStoreImpl(androidContext()) }
        single<ExamplePreferencesDataStore> { ExamplePreferencesDataStoreImpl(androidContext()) }
        single<ExampleProtoDataStore> { ExampleProtoDataStoreImpl(androidContext()) }

        viewModel {
            ExampleViewModel(get(), get(), get(), get())
        }
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