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

package com.example.datastore.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.store.ExamplePreferencesDataStore
import com.example.datastore.store.ExampleRxPreferencesDataStore
import com.example.datastore.store.ExampleSharedPreferences
import com.example.datastore.store.Profession
import com.example.datastore.store.proto.ExampleProtoDataStore
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@SuppressLint("CheckResult")
class ExampleViewModel(
    private val sharedPreferences: ExampleSharedPreferences,
    private val preferencesDataStore: ExamplePreferencesDataStore,
    private val rxPreferencesDataStore: ExampleRxPreferencesDataStore,
    private val protoPreferencesDataStore: ExampleProtoDataStore
) : ViewModel() {

    init {
        sharedPreferences.registerAgeListener {
            Log.d("Update SP", "$it")
        }
//        sharedPreferences.incrementAge()
//        sharedPreferences.incrementAge()
//        sharedPreferences.incrementAge()
//        sharedPreferences.incrementAge()

        viewModelScope.launch {
            preferencesDataStore.ageFlow.collect {
                Log.d("Update PDS", "$it")
            }
        }

        rxPreferencesDataStore.ageObservable
            .subscribeOn(Schedulers.io())
            .subscribe {
                Log.d("Update RxPDS", "$it")
            }
    }

    fun addUser(name: String, nickName: String, age: String, professionName: String) {
        sharedPreferences.apply {
            writeName(name)
            writeNickName(nickName)
            writeAge(age.toInt())
            writeProfession(Profession.valueOf(professionName))
        }
    }

    fun preferencesDataStore() {
        viewModelScope.launch {
            preferencesDataStore.writeAge(10)
            preferencesDataStore.writeAge(23)
            preferencesDataStore.writeAge(49)
            preferencesDataStore.writeAge(52)
            Log.d("Update PDS", preferencesDataStore.readUser().toString())
            preferencesDataStore.writeProfession(Profession.DAY_TRADER)
            Log.d("Update PDS", preferencesDataStore.readUser().toString())
        }
    }

    fun rxPreferencesDataStore() {
        rxPreferencesDataStore.writeAge(13)
            .andThen(Completable.defer { rxPreferencesDataStore.writeAge(43) })
            .andThen(Completable.defer { rxPreferencesDataStore.writeAge(78) })
            .andThen(Completable.defer { rxPreferencesDataStore.writeAge(98) })
            .andThen(rxPreferencesDataStore.readUser())
            .subscribeOn(Schedulers.io())
            .doOnSuccess { Log.d("Update RxPDS", it.toString()) }
            .subscribe()
    }

    fun protoPreferencesDataStore() {
        viewModelScope.launch {
            protoPreferencesDataStore.userPreferencesFlow.collect {
                Log.d("Update PPDS", "$it")
            }
        }
    }
}