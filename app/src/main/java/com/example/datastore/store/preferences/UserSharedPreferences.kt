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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

const val USER_SHARED_PREFERENCES_NAME = "shared_prefs"
const val USER_EXTRA_SHARED_PREFERENCES_NAME = "extra_shared_prefs"

interface UserSharedPreferences {

    val userFlow: Flow<UserInfo>

    suspend fun readUser(): UserInfo

    suspend fun readName(): String

    suspend fun readEmail(): String

    suspend fun readCode(): Int

    suspend fun readProfession(): Profession

    suspend fun writeName(name: String)

    suspend fun writeEmail(email: String)

    suspend fun writeCode(code: Int)

    suspend fun writeProfession(profession: Profession)

    suspend fun clear()
}

@ExperimentalCoroutinesApi
class UserSharedPreferencesImpl(context: Context, private val coroutineContext: CoroutineContext = Dispatchers.IO) : UserSharedPreferences {

    object Keys {

        const val NAME_KEY = "name"
        const val EMAIL_KEY = "email"
        const val CODE_KEY = "code"
        const val PROFESSION_KEY = "profession"
    }

    private val sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private var preferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val clearDataFlow = MutableSharedFlow<UserInfo>()

    override val userFlow: Flow<UserInfo> = flowOf(createListener(), clearDataFlow)
        .flattenMerge()
        .shareIn(CoroutineScope(coroutineContext), SharingStarted.WhileSubscribed())

    // region - Read
    override suspend fun readUser(): UserInfo {
        return UserInfo(
            name = readName(),
            email = readEmail(),
            code = readCode(),
            profession = readProfession()
        )
    }

    override suspend fun readName(): String {
        return withContext { sharedPreferences.getString(Keys.NAME_KEY, "").orEmpty() }
    }

    override suspend fun readEmail(): String {
        return withContext { sharedPreferences.getString(Keys.EMAIL_KEY, "").orEmpty() }
    }

    override suspend fun readCode(): Int {
        return withContext { sharedPreferences.getInt(Keys.CODE_KEY, 0) }
    }

    override suspend fun readProfession(): Profession {
        return withContext {
            sharedPreferences.getString(Keys.PROFESSION_KEY, null)?.let {
                Profession.valueOf(it)
            } ?: Profession.OTHER
        }
    }
    // endregion

    // region - Write
    override suspend fun writeName(name: String) {
        withContext {
            sharedPreferences.edit {
                putString(Keys.NAME_KEY, name)
            }
        }
    }

    override suspend fun writeEmail(email: String) {
        withContext {
            sharedPreferences.edit {
                putString(Keys.EMAIL_KEY, email)
            }
        }
    }

    override suspend fun writeCode(code: Int) {
        withContext {
            sharedPreferences.edit {
                putInt(Keys.CODE_KEY, code)
            }
        }
    }

    override suspend fun writeProfession(profession: Profession) {
        withContext {
            sharedPreferences.edit {
                putString(Keys.PROFESSION_KEY, profession.name)
            }
        }
    }
    // endregion

    // region - Clear
    override suspend fun clear() {
        withContext {
            sharedPreferences.edit {
                clear()
            }
            runBlocking {
                clearDataFlow.emit(UserInfo.empty)
            }
        }
    }
    // endregion

    private suspend fun <T> withContext(block: () -> T) = withContext(coroutineContext) { block() }

    private fun createListener() = callbackFlow {
        preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
            offer(
                UserInfo(
                    name = sharedPreferences.getString(Keys.NAME_KEY, "").orEmpty(),
                    email = sharedPreferences.getString(Keys.EMAIL_KEY, "").orEmpty(),
                    code = sharedPreferences.getInt(Keys.CODE_KEY, 0),
                    profession = sharedPreferences.getString(Keys.PROFESSION_KEY, null)?.let {
                        Profession.valueOf(it)
                    } ?: Profession.OTHER
                )
            )
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
        awaitClose { sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener) }
    }
}