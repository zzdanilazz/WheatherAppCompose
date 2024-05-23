package com.volsib.logincompose.data

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /**
     * Retrieve all the items from the given data source.
     */
    fun getAllUsersStream(): Flow<List<User>>

    fun getUserByIdStream(id: Int): Flow<User?>

    fun getUserByLoginStream(login: String): Flow<User?>

    fun getCurrentUserStream(): Flow<User?>

    /**
     * Insert item in the data source
     */
    suspend fun insertUser(user: User)

    /**
     * Delete item from the data source
     */
    suspend fun deleteUser(user: User)

    /**
     * Update item in the data source
     */
    suspend fun updateUser(user: User)
}