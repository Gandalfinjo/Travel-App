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
     * Retrieves a user for the provided email.
     *
     * @param email Email for which to retrieve a user
     * @return User object if the email is found, null otherwise
     */
    suspend fun getUserByEmail(email: String) =
        userDao.getUserByEmail(email)

    /**
     * Retrieves a user for the provided id.
     *
     * @param userId ID of the user to retrieve
     * @return User object if the email is found, null otherwise
     */
    suspend fun getUserById(userId: Int): User? =
        userDao.getUserById(userId)

    /**
     * Updates the full name of the provided user.
     *
     * @param userId ID of the user for which to update the name
     * @param firstname New first name
     * @param lastname New last name
     */
    suspend fun updateName(userId: Int, firstname: String, lastname: String) =
        userDao.updateName(userId, firstname, lastname)

    /**
     * Updates the email of the provided user.
     *
     * @param userId ID of the user for which to update the email
     * @param email New email
     */
    suspend fun updateEmail(userId: Int, email: String) =
        userDao.updateEmail(userId, email)

    /**
     * Updates the username of the provided user.
     *
     * @param userId ID of the user for which to update the username
     * @param username New username
     */
    suspend fun updateUsername(userId: Int, username: String) =
        userDao.updateUsername(userId, username)

    /**
     * Updates the password of the provided user.
     *
     * @param userId ID of the user for which to update the password
     * @param password New password
     */
    suspend fun updatePassword(userId: Int, password: String) {
        val hashed = hashPassword(password)
        userDao.updatePassword(userId, hashed)
    }

    /**
     * Updates the profile picture of the provided user.
     *
     * @param userId ID of the user for which to update the profile picture
     * @param path Path to the profile picture
     */
    suspend fun updateProfilePicture(userId: Int, path: String?) =
        userDao.updateProfilePicture(userId, path)

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