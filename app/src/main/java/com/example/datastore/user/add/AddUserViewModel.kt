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

package com.example.datastore.user.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datastore.store.Profession
import com.example.datastore.store.UserInfo
import com.example.datastore.store.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale

class AddUserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Empty)
    val state: MutableStateFlow<State> get() = _state

    sealed class State {
        object Empty : State()
        class Started(val code: String, val professionNames: List<String>) : State()
        object Completed : State()
    }

    init {
        viewModelScope.launch {
            userRepository.observeUsers().collect {
                Log.d("ViewModel", "Collected ${it.size} items")
            }
        }
        _state.value = State.Started((1..Int.MAX_VALUE).random().toString(), Profession.values().map { it.toString() })
    }

    fun addUser(name: String, email: String, code: String, professionName: String) {
        val allDataIsValid = name.isNotBlank() && email.isNotBlank() && code.isNotBlank() && professionName.isNotBlank()
        if (allDataIsValid) {
            viewModelScope.launch {
                userRepository.addUser(
                    UserInfo(
                        name = name,
                        email = email,
                        code = code.toInt(),
                        profession = Profession.valueOf(professionName.toUpperCase(Locale.getDefault()))
                    )
                )
                _state.value = State.Completed
            }
        }
    }
}