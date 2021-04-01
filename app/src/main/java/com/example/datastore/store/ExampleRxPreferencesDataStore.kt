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
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava2.rxPreferencesDataStore
import androidx.datastore.rxjava2.RxDataStore
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException

private const val DATA_STORE_NAME = "rx_preferences_data_store"

interface ExampleRxPreferencesDataStore {

    val userObservable: Flowable<UserInfo>

    val nameObservable: Flowable<String>

    val nickNameObservable: Flowable<String>

    val ageObservable: Flowable<Int>

    val emailObservable: Flowable<String>

    fun readUser(): Single<UserInfo>

    fun readName(): Single<String>

    fun readNickName(): Single<String>

    fun readAge(): Single<Int>

    fun readEmail(): Single<String>

    fun writeName(name: String): Completable

    fun writeAge(age: Int): Completable

    fun clear(): Completable
}

@ExperimentalCoroutinesApi
class ExampleRxPreferencesDataStoreImpl(private val context: Context) : ExampleRxPreferencesDataStore {

    private object Keys {

        val NAME_KEY = stringPreferencesKey("name")
        val NICK_NAME_KEY = stringPreferencesKey("nick_name")
        val AGE_KEY = intPreferencesKey("age")
        val EMAIL_KEY = stringPreferencesKey("email")
    }

    private val Context.dataStore: RxDataStore<Preferences> by rxPreferencesDataStore(
        name = DATA_STORE_NAME,
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(context, USER_SHARED_PREFERENCES_NAME),
                SharedPreferencesMigration(context, USER_EXTRA_SHARED_PREFERENCES_NAME)
            )
        }
    )

    // region - Observable
    override val userObservable: Flowable<UserInfo>
        get() {
            return context.dataStore.data()
                .handleException()
                .map { preferences ->
                    UserInfo(
                        name = preferences[Keys.NAME_KEY].orEmpty(),
                        nickName = preferences[Keys.NICK_NAME_KEY].orEmpty(),
                        age = preferences[Keys.AGE_KEY] ?: 0,
                        profession = Profession.OTHER
                    )
                }
        }
    override val nameObservable: Flowable<String>
        get() {
            return context.dataStore.data()
                .handleException()
                .map { preferences ->
                    preferences[Keys.NAME_KEY].orEmpty()
                }
        }

    override val nickNameObservable: Flowable<String>
        get() {
            return context.dataStore.data()
                .handleException()
                .map { preferences ->
                    preferences[Keys.NICK_NAME_KEY].orEmpty()
                }
        }

    override val ageObservable: Flowable<Int>
        get() {
            return context.dataStore.data()
                .handleException()
                .map { preferences ->
                    preferences[Keys.AGE_KEY] ?: 0
                }
        }

    override val emailObservable: Flowable<String>
        get() {
            return context.dataStore.data()
                .handleException()
                .map { preferences ->
                    preferences[Keys.EMAIL_KEY].orEmpty()
                }
        }
    // endregion

    // region - Read
    override fun readUser(): Single<UserInfo> {
        return userObservable.firstOrError()
    }

    override fun readName(): Single<String> {
        return nameObservable.firstOrError()
    }

    override fun readNickName(): Single<String> {
        return nickNameObservable.firstOrError()
    }

    override fun readAge(): Single<Int> {
        return ageObservable.firstOrError()
    }

    override fun readEmail(): Single<String> {
        return emailObservable.firstOrError()
    }
    // endregion

    // region - Write
    override fun writeName(name: String): Completable {
        return context.dataStore.updateDataAsync {
            val mutablePreferences = it.toMutablePreferences()
            mutablePreferences[Keys.NAME_KEY] = name
            Single.just(mutablePreferences)
        }.ignoreElement()
    }

    override fun writeAge(age: Int): Completable {
        return context.dataStore.updateDataAsync {
            val mutablePreferences = it.toMutablePreferences()
            mutablePreferences[Keys.AGE_KEY] = age
            Single.just(mutablePreferences)
        }.ignoreElement()
    }
    // endregion

    // region - Clear
    override fun clear(): Completable {
        return context.dataStore.updateDataAsync {
            val mutablePreferences = it.toMutablePreferences()
            mutablePreferences.clear()
            Single.just(mutablePreferences)
        }.ignoreElement()
    }
    // endregion

    private fun Flowable<Preferences>.handleException(): Flowable<Preferences> {
        return onErrorReturn { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emptyPreferences()
            } else {
                throw exception
            }
        }
    }
}