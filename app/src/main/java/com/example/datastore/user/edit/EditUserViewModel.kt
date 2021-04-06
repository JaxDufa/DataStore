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

package com.example.datastore.user.edit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.store.Profession
import com.example.datastore.store.UserInfo
import com.example.datastore.store.UserPreferencesDataStore
import com.example.datastore.store.UserSharedPreferences
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class EditUserViewModel(
    private val sharedPreferences: UserSharedPreferences,
    private val preferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> get() = _state

    sealed class State {
        class Started(val professionNames: List<String>) : State()
        class Loaded(val user: UserInfo) : State()
        object UserEdited : State()
    }

    init {
        sharedPreferences.registerListener { key, newValue ->
            Log.d("Update SharedPrefs", "$key - $newValue")
        }

        viewModelScope.launch {
            preferencesDataStore.userFlow.collect {
                Log.d("Update PDS", "$it")
            }
        }

        _state.postValue(State.Started(Profession.values().map { it.toString() }))
    }

    fun loadUser() {
        viewModelScope.launch {
            val user = preferencesDataStore.readUser()
            _state.postValue(State.Loaded(user))
        }
    }

    fun editUser(name: String, email: String, professionName: String) {
        val allDataIsValid = name.isNotBlank() && email.isNotBlank() && professionName.isNotBlank()
        if (allDataIsValid) {
            viewModelScope.launch {
                preferencesDataStore.apply {
                    writeName(name)
                    writeEmail(email)
                    writeProfession(Profession.valueOf(professionName.toUpperCase(Locale.getDefault())))
                }
                _state.postValue(State.UserEdited)
            }
        }
    }

//    fun editUser() {
//        _state.postValue(State.Editing)
//    }
//
//    fun saveUser() {
//        _state.postValue(State.Saved)
//    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterListener()
    }
}