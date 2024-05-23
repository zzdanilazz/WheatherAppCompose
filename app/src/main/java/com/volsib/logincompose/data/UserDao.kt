package com.volsib.logincompose.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * from users WHERE id = :id")
    fun getUserById(id: Int): Flow<User>

    @Query("SELECT * from users WHERE login = :login")
    fun getUserByLogin(login: String): Flow<User>

    @Query("SELECT * from users ORDER BY login ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * from users WHERE is_signed_in = 1")
    fun getCurrentUser(): Flow<User?>
}
