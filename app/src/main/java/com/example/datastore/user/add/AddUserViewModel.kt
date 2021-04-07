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
import com.example.datastore.store.Profession
import com.example.datastore.store.UserSharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

class AddUserViewModel(
    private val sharedPreferences: UserSharedPreferences
//    private val preferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Empty)
    val state: MutableStateFlow<State> get() = _state

    sealed class State {
        object Empty : State()
        class Started(val professionNames: List<String>) : State()
        object Completed : State()
    }

    init {
        sharedPreferences.registerListener { key, newValue ->
            Log.d("Update SharedPrefs", "$key - $newValue")
        }
        _state.value = State.Started(Profession.values().map { it.toString() })
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterListener()
    }

    fun addUser(name: String, email: String, code: String, professionName: String) {
        val allDataIsValid = name.isNotBlank() && email.isNotBlank() && code.isNotBlank() && professionName.isNotBlank()
        if (allDataIsValid) {
            sharedPreferences.apply {
                writeName(name)
                writeEmail(email)
                writeCode(code.toInt())
                writeProfession(Profession.valueOf(professionName.toUpperCase(Locale.getDefault())))
            }
            _state.value = State.Completed
        }
    }
}