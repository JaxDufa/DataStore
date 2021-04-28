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
import com.example.datastore.store.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class EditUserViewModel(
    private val userIndex: Int,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> get() = _state

    sealed class State {
        class Started(val professionNames: List<String>) : State()
        class Loaded(val user: UserInfo) : State()
        object Completed : State()
    }

    init {
        viewModelScope.launch {
            userRepository.observeUsers().collect {
                Log.d("ViewModel", "Collected ${it.size} items")
            }
        }
        _state.postValue(State.Started(Profession.values().map { it.toString() }))
    }

    fun loadUser() {
        viewModelScope.launch {
            val user = userRepository.readUser(userIndex)
            _state.postValue(State.Loaded(user))
        }
    }

    fun removeUser() {
        viewModelScope.launch {
            userRepository.removeUser(userIndex)
            _state.postValue(State.Completed)
        }
    }

    fun editUser(name: String, email: String, professionName: String) {
        val allDataIsValid = name.isNotBlank() && email.isNotBlank() && professionName.isNotBlank()
        if (allDataIsValid) {
            viewModelScope.launch {
                userRepository.writeUser(userIndex, name, email, profession = Profession.valueOf(professionName.toUpperCase(Locale.getDefault())))
                _state.postValue(State.Completed)
            }
        }
    }
}