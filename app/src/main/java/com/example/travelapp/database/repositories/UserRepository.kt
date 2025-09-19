package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.UserDao
import com.example.travelapp.database.models.User
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {
    suspend fun register(firstname: String, lastname: String, email: String, username: String, password: String) {
        val hashedPassword = hashPassword(password)

        val user = User(
            firstname = firstname,
            lastname = lastname,
            email = email,
            username = username,
            password = hashedPassword
        )

        userDao.insert(user)
    }

    suspend fun login(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)

        return userDao.getUserForLogin(username, hashedPassword)
    }

    suspend fun getUserByUsername(username: String) =
        userDao.getUserByUsername(username)

    suspend fun getUserByEmail(email: String) =
        userDao.getUserByEmail(email)

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}