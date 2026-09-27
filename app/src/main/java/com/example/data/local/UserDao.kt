package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY fechaRegistro DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE aprobadoPorAdmin = 0 AND rechazado = 0 ORDER BY fechaRegistro DESC")
    fun getPendingUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun observeUserByEmail(email: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET aprobadoPorAdmin = 1, rechazado = 0, fechaAprobacion = :approvalDate, updatedAt = :approvalDate WHERE id = :id")
    suspend fun approveUser(id: String, approvalDate: Long = System.currentTimeMillis())

    @Query("UPDATE users SET aprobadoPorAdmin = 0, rechazado = 1, updatedAt = :rejectDate WHERE id = :id")
    suspend fun rejectUser(id: String, rejectDate: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
