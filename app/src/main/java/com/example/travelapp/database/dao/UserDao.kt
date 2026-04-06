package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.database.models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUserForLogin(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE users SET firstname = :firstname, lastname = :lastname WHERE id = :userId")
    suspend fun updateName(userId: Int, firstname: String, lastname: String)

    @Query("UPDATE users SET email = :email WHERE id = :userId")
    suspend fun updateEmail(userId: Int, email: String)

    @Query("UPDATE users SET username = :username WHERE id = :userId")
    suspend fun updateUsername(userId: Int, username: String)

    @Query("UPDATE users SET password = :password WHERE id = :userId")
    suspend fun updatePassword(userId: Int, password: String)

    @Query("UPDATE users SET profile_picture_path = :path WHERE id = :userId")
    suspend fun updateProfilePicture(userId: Int, path: String?)
}