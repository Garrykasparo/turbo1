package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Envio
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvioDao {
    @Query("SELECT * FROM envios ORDER BY createdAt DESC")
    fun getAllEnvios(): Flow<List<Envio>>

    @Query("SELECT * FROM envios WHERE proveedorId = :proveedorId ORDER BY createdAt DESC")
    fun getEnviosByProveedor(proveedorId: String): Flow<List<Envio>>

    @Query("SELECT * FROM envios WHERE repartidorId = :repartidorId ORDER BY createdAt DESC")
    fun getEnviosByRepartidor(repartidorId: String): Flow<List<Envio>>

    @Query("SELECT * FROM envios WHERE repartidorId = :repartidorId AND estado IN ('asignado', 'recogiendo', 'en_camino') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveEnvioForRepartidor(repartidorId: String): Flow<Envio?>

    @Query("SELECT * FROM envios WHERE estado = 'pendiente' ORDER BY createdAt DESC")
    fun getPendientes(): Flow<List<Envio>>

    @Query("SELECT * FROM envios WHERE estado IN ('asignado', 'recogiendo', 'en_camino') ORDER BY createdAt DESC")
    fun getActivos(): Flow<List<Envio>>

    @Query("SELECT * FROM envios WHERE id = :id LIMIT 1")
    suspend fun getEnvioById(id: String): Envio?

    @Query("SELECT * FROM envios WHERE id = :id LIMIT 1")
    fun getEnvioByIdFlow(id: String): Flow<Envio?>

    @Query("SELECT * FROM envios WHERE numeroSeguimiento = :tracking LIMIT 1")
    suspend fun getEnvioByTracking(tracking: String): Envio?

    @Query("SELECT * FROM envios WHERE numeroSeguimiento LIKE '%' || :query || '%' OR destinatarioNombre LIKE '%' || :query || '%' OR destinoDireccion LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchEnvios(query: String): Flow<List<Envio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvio(envio: Envio)

    @Update
    suspend fun updateEnvio(envio: Envio)

    @Query("UPDATE envios SET estado = :nuevoEstado, historialJson = :historialJson, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEstadoWithHistorial(id: String, nuevoEstado: String, historialJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE envios SET estado = 'cancelado', motivoCancelacion = :motivo, canceladoPor = :canceladoPor, fechaCancelacion = :fechaCancelacion, historialJson = :historialJson, updatedAt = :updatedAt WHERE id = :id")
    suspend fun cancelEnvio(id: String, motivo: String, canceladoPor: String, fechaCancelacion: Long, historialJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE envios SET estado = :nuevoEstado, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEstado(id: String, nuevoEstado: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE envios SET repartidorId = :repartidorId, repartidorNombre = :repartidorNombre, estado = 'asignado', fechaAsignacion = :updatedAt, historialJson = :historialJson, updatedAt = :updatedAt WHERE id = :id")
    suspend fun assignRepartidorWithHistorial(id: String, repartidorId: String, repartidorNombre: String, historialJson: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE envios SET repartidorId = :repartidorId, repartidorNombre = :repartidorNombre, estado = 'asignado', updatedAt = :updatedAt WHERE id = :id")
    suspend fun assignRepartidor(id: String, repartidorId: String, repartidorNombre: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM envios WHERE estado = 'pendiente'")
    fun countPendientes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM envios WHERE estado IN ('asignado', 'recogiendo', 'en_camino')")
    fun countActivos(): Flow<Int>

    @Query("SELECT COUNT(*) FROM envios WHERE estado = 'entregado'")
    fun countEntregados(): Flow<Int>

    @Query("SELECT COUNT(*) FROM envios")
    suspend fun getEnviosCount(): Int
}
