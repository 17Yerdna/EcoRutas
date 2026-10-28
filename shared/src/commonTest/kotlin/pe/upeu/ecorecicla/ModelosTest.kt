package pe.upeu.ecorecicla
import kotlin.test.*
import pe.upeu.ecorecicla.domain.model.*
class ModelosTest {
    @Test fun estadoAnuladoConservaMotivo() { assertEquals("Error",EstadoPesaje.Anulado("Error").motivo) }
    @Test fun sesionConservaPrivilegios() { assertTrue("PUNTO_LEER" in Sesion("t","op","OPERARIO",setOf("PUNTO_LEER")).privilegios) }
}
