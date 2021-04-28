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
import com.example.datastore.store.UserRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserListViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> get() = _state

    sealed class State {
        class Started(val users: List<UserInfo>) : State()
    }

    init {
        viewModelScope.launch {
            userRepository.observeUsers().collect { users ->
                Log.d("ViewModel", "Collected ${users.size} items")
                val validUsers = users.mapNotNull { if (it.isValid) it else null }
                _state.postValue(State.Started(validUsers))
            }
        }
    }

    fun removeUsers() {
        viewModelScope.launch {
            userRepository.clear()
        }
    }
}