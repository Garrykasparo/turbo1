package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Repartidor
import kotlinx.coroutines.flow.Flow

@Dao
interface RepartidorDao {
    @Query("SELECT * FROM repartidores ORDER BY createdAt DESC")
    fun getAllRepartidores(): Flow<List<Repartidor>>

    @Query("SELECT * FROM repartidores WHERE estado = :estado")
    fun getRepartidoresByEstado(estado: String): Flow<List<Repartidor>>

    @Query("SELECT * FROM repartidores WHERE userId = :userId LIMIT 1")
    suspend fun getRepartidorByUserId(userId: String): Repartidor?

    @Query("SELECT * FROM repartidores WHERE userId = :userId LIMIT 1")
    fun observeRepartidorByUserId(userId: String): Flow<Repartidor?>

    @Query("SELECT * FROM repartidores WHERE id = :id LIMIT 1")
    suspend fun getRepartidorById(id: String): Repartidor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepartidor(repartidor: Repartidor)

    @Update
    suspend fun updateRepartidor(repartidor: Repartidor)

    @Query("UPDATE repartidores SET estado = :nuevoEstado, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateEstado(userId: String, nuevoEstado: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM repartidores WHERE estado = 'disponible'")
    fun countDisponibles(): Flow<Int>

    @Query("SELECT COUNT(*) FROM repartidores WHERE estado = 'en_pedido'")
    fun countOcupados(): Flow<Int>

    @Query("SELECT COUNT(*) FROM repartidores")
    suspend fun getRepartidoresCount(): Int
}
