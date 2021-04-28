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

package com.example.datastore.store.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.datastore.store.Profession
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_table")
data class UserEntity(@PrimaryKey @ColumnInfo(name = "code") val code: Int, val name: String, val email: String, val profession: Profession)

class Converters {

    @TypeConverter
    fun toHealth(value: String) = enumValueOf<Profession>(value)

    @TypeConverter
    fun fromHealth(value: Profession) = value.name
}

@TypeConverters(Converters::class)
@Dao
interface UserDao {

    @Query("SELECT * FROM user_table ORDER BY name ASC")
    fun getAlphabetizedUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(user: UserEntity)

    @Query("DELETE FROM user_table")
    suspend fun deleteAll()

    @Query("DELETE FROM user_table WHERE code = :code")
    suspend fun delete(code: Int)
}