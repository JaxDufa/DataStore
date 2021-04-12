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

package com.example.datastore.store.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.datastore.store.Profession
import com.example.datastore.store.UserInfo

const val USER_SHARED_PREFERENCES_NAME = "shared_prefs"
const val USER_EXTRA_SHARED_PREFERENCES_NAME = "extra_shared_prefs"

typealias PreferencesListener = (user: UserInfo) -> Unit

interface UserSharedPreferences {

    fun registerListener(onPreferencesChanged: PreferencesListener)

    fun unregisterListener()

    fun readUser(): UserInfo

    fun readName(): String

    fun readEmail(): String

    fun readCode(): Int

    fun readProfession(): Profession

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
    //            putString(Keys.EMAIL_KEY, "Hacker.2000@yahoo.com")
    //        }
    //    }

    override fun registerListener(onPreferencesChanged: PreferencesListener) {
        preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            UserInfo(
                name = sharedPreferences.getString(Keys.NAME_KEY, "").orEmpty(),
                email = sharedPreferences.getString(Keys.EMAIL_KEY, "").orEmpty(),
                code = sharedPreferences.getInt(Keys.CODE_KEY, 0),
                profession = sharedPreferences.getString(Keys.PROFESSION_KEY, null)?.let {
                    Profession.valueOf(it)
                } ?: Profession.OTHER
            )
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    override fun unregisterListener() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    // region - Read
    override fun readUser(): UserInfo {
        return UserInfo(
            name = readName(),
            email = readEmail(),
            code = readCode(),
            profession = readProfession()
        )
    }

    override fun readName(): String {
        return sharedPreferences.getString(Keys.NAME_KEY, "").orEmpty()
    }

    override fun readEmail(): String {
        return sharedPreferences.getString(Keys.EMAIL_KEY, "").orEmpty()
    }

    override fun readCode(): Int {
        return sharedPreferences.getInt(Keys.CODE_KEY, 0)
    }

    override fun readProfession(): Profession {
        return sharedPreferences.getString(Keys.PROFESSION_KEY, null)?.let {
            Profession.valueOf(it)
        } ?: Profession.OTHER
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