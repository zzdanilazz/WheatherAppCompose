package com.volsib.logincompose.data

import kotlinx.coroutines.flow.Flow

class OfflineUsersRepository (private val userDao: UserDao) : UserRepository {
    override fun getAllUsersStream(): Flow<List<User>> = userDao.getAllUsers()

    override fun getUserByIdStream(id: Int): Flow<User?> = userDao.getUserById(id)

    override fun getUserByLoginStream(login: String): Flow<User?> = userDao.getUserByLogin(login)
    override fun getCurrentUserStream(): Flow<User?>  = userDao.getCurrentUser()

    override suspend fun insertUser(user: User) = userDao.insert(user)

    override suspend fun deleteUser(user: User) = userDao.delete(user)

    override suspend fun updateUser(user: User) = userDao.update(user)
}