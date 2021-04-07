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

package com.example.datastore.user.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.store.UserInfo
import com.example.datastore.store.UserPreferencesDataStore
import com.example.datastore.store.UserSharedPreferences
import com.example.datastore.store.proto.UserProtoPreferencesDataStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserListViewModel(
    private val sharedPreferences: UserSharedPreferences,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val protoPreferencesDataStore: UserProtoPreferencesDataStore
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> get() = _state

    sealed class State {
        class Started(val users: List<UserInfo>) : State()
    }

    init {
        sharedPreferences.registerListener { key, newValue ->
            Log.d("Update SharedPrefs", "$key - $newValue")
        }

        viewModelScope.launch {
            preferencesDataStore.userFlow.collect {
                Log.d("Update PDS", "$it")
                _state.postValue(State.Started(listOf(it)))
            }
        }

        viewModelScope.launch {
            protoPreferencesDataStore.userFlow.collect {
                Log.d("Update PPDS", "$it")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterListener()
    }
}