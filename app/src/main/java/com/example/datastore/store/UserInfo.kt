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

import java.util.Locale

const val USER_KEY = "user_id"

data class UserInfo(val name: String, val email: String, val code: Int, val profession: Profession) {

    companion object {
        val empty = UserInfo("", "", -1, Profession.OTHER)
    }

    val isValid: Boolean
        get() = code > 0 && name.isNotBlank()
}

enum class Profession {
    DEVELOPER, ENGINEER, HITMAN, MEDIC, WRITER, OTHER;

    override fun toString(): String {
        return name.toLowerCase(Locale.getDefault()).capitalize(Locale.getDefault())
    }
}