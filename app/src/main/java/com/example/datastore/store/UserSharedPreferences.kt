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
import android.content.SharedPreferences
import androidx.core.content.edit

const val USER_SHARED_PREFERENCES_NAME = "shared_prefs"
const val USER_EXTRA_SHARED_PREFERENCES_NAME = "extra_shared_prefs"

typealias PreferencesListener = (key: String, newValue: Any) -> Unit

interface UserSharedPreferences {

    fun registerListener(onPreferencesChanged: PreferencesListener)

    fun unregisterListener()

    fun readName(): String

    fun writeName(name: String)

    fun writeEmail(email: String)

    fun writeCode(code: Int)

    fun writeProfession(profession: Profession)

    fun clear()
}

class UserSharedPreferencesImpl(context: Context) : UserSharedPreferences {

    object Keys {

        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val CODE_KEY = "code"
        const val PROFESSION_KEY = "profession"
    }

    private val sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private var preferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

//    init {
//        sharedPreferences.edit {
//            putString(Keys.NAME_KEY, "Sidarta")
//        }
//        extraSharedPreferences.edit {
//            putString(Keys.NICK_NAME_KEY, "Hacker 2000")
//            putInt(Keys.AGE_KEY, 100)
//        }
//    }

    override fun registerListener(onPreferencesChanged: PreferencesListener) {
        preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Keys.CODE_KEY) {
                onPreferencesChanged(key, sharedPreferences.getInt(key, 0))
            } else {
                onPreferencesChanged(key, sharedPreferences.getString(key, "").orEmpty())
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun unregisterListener() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
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

    override fun writeEmail(email: String) {
        sharedPreferences.edit {
            putString(Keys.EMAIL_KEY, email)
        }
    }

    override fun writeCode(code: Int) {
        sharedPreferences.edit {
            putInt(Keys.CODE_KEY, code)
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