package pe.upeu.ecorecicla
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.upeu.ecorecicla.data.local.db.EcoDb
import pe.upeu.ecorecicla.data.local.BaseLocal
import pe.upeu.ecorecicla.domain.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
class BaseLocalTest {
    @Test fun idsNegativosYAislamientoDeTurnos() = runTest {
        val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        EcoDb.Schema.create(driver)
        val db=EcoDb(driver)
        assertEquals(-1L,db.ecoQueries.siguienteId("op").executeAsOne().expr)
        val l=BaseLocal(db,"op")
        l.guardar(Pesaje(-1,1,"2026-10-28","PAPEL",12.0,2.0,0.0,"op",EstadoPesaje.Registrado,true))
        assertEquals(-2L,db.ecoQueries.siguienteId("op").executeAsOne().expr)
        assertTrue(BaseLocal(db,"otro").todos().isEmpty())
        assertEquals(1L,db.ecoQueries.pendientes("op").executeAsOne())
        driver.close()
    }
}
