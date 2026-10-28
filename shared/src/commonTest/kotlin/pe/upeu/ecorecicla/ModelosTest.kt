package pe.upeu.ecorecicla
import kotlin.test.*
import pe.upeu.ecorecicla.domain.model.*
class ModelosTest {
    @Test fun auditorEsLecturaPorPrivilegiosNoPorNombre() {
        assertTrue(Sesion("t","auditor1","AUDITOR",setOf("PUNTO_LEER","PESAJE_LEER")).soloLectura)
        assertTrue(Sesion("t","otro","CUALQUIER_NOMBRE",setOf("PUNTO_LEER")).soloLectura)
        assertFalse(Sesion("t","op","OPERARIO",setOf("PESAJE_CREAR")).soloLectura)
    }
    @Test fun estadoAnuladoConservaMotivo() { assertEquals("Error",EstadoPesaje.Anulado("Error").motivo) }
    @Test fun sesionConservaPrivilegios() { assertTrue("PUNTO_LEER" in Sesion("t","op","OPERARIO",setOf("PUNTO_LEER")).privilegios) }
}
