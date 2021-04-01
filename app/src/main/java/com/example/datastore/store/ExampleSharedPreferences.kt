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
import androidx.core.content.edit

const val USER_SHARED_PREFERENCES_NAME = "shared_prefs"
const val USER_EXTRA_SHARED_PREFERENCES_NAME = "extra_shared_prefs"

typealias StringListener = (newValue: String) -> Unit
typealias IntListener = (newValue: Int) -> Unit

interface ExampleSharedPreferences {

    fun registerNameListener(onNameChanged: StringListener)

    fun registerAgeListener(onAgeChanged: IntListener)

    fun readName(): String

    fun writeName(name: String)

    fun writeNickName(nickName: String)

    fun writeAge(age: Int)

    fun writeProfession(profession: Profession)

    fun clear()
}

class ExampleSharedPreferencesImpl(context: Context) : ExampleSharedPreferences {

    private object Keys {

        const val NAME_KEY = "name"
        const val NICK_NAME_KEY = "nick_name"
        const val AGE_KEY = "age"
        const val PROFESSION_KEY = "profession"
    }

    private val sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val extraSharedPreferences = context.getSharedPreferences(USER_EXTRA_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private var onNameChanged: StringListener? = null
    private var onAgeChanged: IntListener? = null

    init {
        sharedPreferences.edit {
            putString(Keys.NAME_KEY, "Sidarta")
        }
        extraSharedPreferences.edit {
            putString(Keys.NICK_NAME_KEY, "Hacker 2000")
            putInt(Keys.AGE_KEY, 100)
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                Keys.NAME_KEY -> onNameChanged?.invoke(sharedPreferences.getString(key, null).orEmpty())
                Keys.AGE_KEY -> onAgeChanged?.invoke(sharedPreferences.getInt(key, 0))
            }
        }
    }

    // region - Listener
    override fun registerNameListener(onNameChanged: StringListener) {
        this.onNameChanged = onNameChanged
    }

    override fun registerAgeListener(onAgeChanged: IntListener) {
        this.onAgeChanged = onAgeChanged
    }
    // endregion

    // region - Read
    override fun readName(): String {
        return sharedPreferences.getString(Keys.NAME_KEY, "").orEmpty()
    }
    // endregion

    // region - Write
    override fun writeName(name: String) {
        sharedPreferences.edit {
            putString(Keys.NAME_KEY, name)
        }
    }

    override fun writeNickName(nickName: String) {
        sharedPreferences.edit {
            putString(Keys.NICK_NAME_KEY, nickName)
        }
    }

    override fun writeAge(age: Int) {
        sharedPreferences.edit {
            putInt(Keys.AGE_KEY, age)
        }
    }

    override fun writeProfession(profession: Profession) {
        sharedPreferences.edit {
            putString(Keys.PROFESSION_KEY, profession.name)
        }
    }
    // endregion

    // region - Clear
    override fun clear() {
        sharedPreferences.edit {
            clear()
        }
    }
    // endregion
}