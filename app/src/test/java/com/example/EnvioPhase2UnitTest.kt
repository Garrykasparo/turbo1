package com.example

import com.example.data.model.Envio
import com.example.data.model.EnvioEstado
import com.example.data.model.HistorialItem
import com.example.data.model.TipoServicio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EnvioPhase2UnitTest {

    @Test
    fun testGenerateTrackingNumberFormat() {
        val tracking = Envio.generateTrackingNumber()
        assertTrue("Tracking must start with TM-", tracking.startsWith("TM-"))
        assertEquals("Tracking format must be TM-XXXXXXXX (length 11)", 11, tracking.length)
    }

    @Test
    fun testHistorialSerializationAndParsing() {
        val envio = Envio(
            proveedorId = "prov_test",
            origenDireccion = "Calle 60 Centro",
            destinoDireccion = "Altabrisa"
        )

        val entry1 = HistorialItem(
            estado = "pendiente",
            usuarioId = "user_1",
            usuarioNombre = "Roberto",
            descripcion = "Pedido creado"
        )

        val updatedEnvio1 = envio.withHistorialEntry(entry1)
        val list1 = updatedEnvio1.parseHistorial()
        assertEquals(1, list1.size)
        assertEquals("pendiente", list1[0].estado)
        assertEquals("Roberto", list1[0].usuarioNombre)

        val entry2 = HistorialItem(
            estado = "asignado",
            usuarioId = "user_coord",
            usuarioNombre = "Valeria",
            descripcion = "Asignado a Carlos"
        )
        val updatedEnvio2 = updatedEnvio1.withHistorialEntry(entry2)
        val list2 = updatedEnvio2.parseHistorial()
        assertEquals(2, list2.size)
        assertEquals("asignado", list2[1].estado)
        assertEquals("Valeria", list2[1].usuarioNombre)
    }

    @Test
    fun testTipoServicioTarifas() {
        assertEquals(35.0, TipoServicio.BASICO.tarifaBase, 0.01)
        assertEquals(50.0, TipoServicio.EXPRESS.tarifaBase, 0.01)
        assertEquals(45.0, TipoServicio.PROGRAMADO.tarifaBase, 0.01)
        assertEquals(70.0, TipoServicio.TURBO.tarifaBase, 0.01)
    }

    @Test
    fun testEnvioEstadosExist() {
        assertNotNull(EnvioEstado.fromValue("pendiente"))
        assertNotNull(EnvioEstado.fromValue("asignado"))
        assertNotNull(EnvioEstado.fromValue("en_recoleccion"))
        assertNotNull(EnvioEstado.fromValue("en_camino"))
        assertNotNull(EnvioEstado.fromValue("entregado"))
        assertNotNull(EnvioEstado.fromValue("cancelado"))
    }
}
