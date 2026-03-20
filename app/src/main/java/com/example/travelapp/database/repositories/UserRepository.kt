package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.UserDao
import com.example.travelapp.database.models.User
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user data and authentication.
 *
 * Handles user registration, login and password hashing.
 */
@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {
    /**
     * Registers a new user in the system.
     *
     * Password is hashed using SHA-256 before storage.
     *
     * @param firstname User's first name
     * @param lastname Users' last name
     * @param email User's email address
     * @param username Unique username
     * @param password Plain text password (will be hashed)
     */
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

    /**
     * Authenticates a user with username and password.
     *
     * @param username Username to authenticate
     * @param password Plain text password (will be hashed and compared)
     * @return User object if credentials are valid, null otherwise
     */
    suspend fun login(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)

        return userDao.getUserForLogin(username, hashedPassword)
    }

    /**
     * Retrieves a user for the provided username.
     *
     * @param username Username for which to retrieve a user
     * @return User object if the username is found, null otherwise
     */
    suspend fun getUserByUsername(username: String) =
        userDao.getUserByUsername(username)

    /**
     * Retrieves a user for the provided username.
     *
     * @param email Email for which to retrieve a user
     * @return User object if the email is found, null otherwise
     */
    suspend fun getUserByEmail(email: String) =
        userDao.getUserByEmail(email)

    /**
     * Hashes password using SHA-256.
     *
     * NOTE: This is a simplified implementation for demonstration purposes.
     * In production, use proper password hashing with salt (e.g., BCrypt, Argon2).
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}