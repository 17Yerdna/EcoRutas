package pe.upeu.ecorecicla
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pe.upeu.ecorecicla.data.local.db.EcoDb
import pe.upeu.ecorecicla.data.local.BaseLocal
import pe.upeu.ecorecicla.domain.model.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
class BaseLocalTest {
    @Test fun filtroCombinaBusquedaYPendientesLocalmente() {
        val driver=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY); EcoDb.Schema.create(driver); val q=EcoDb(driver).ecoQueries
        q.guardarPunto("op",1,"Empresa Alfa","Av.1","Norte","PAPEL",1,0)
        q.guardarPunto("op",2,"Empresa Beta","Av.2","Sur","VIDRIO",2,1)
        assertEquals(listOf(1L),q.filtrarPuntos("op","Alfa",1,"2026-10-28").executeAsList().map {it.id})
        assertTrue(q.filtrarPuntos("op","Beta",1,"2026-10-28").executeAsList().isEmpty())
        assertEquals(2,q.filtrarPuntos("op","Empresa",0,"2026-10-28").executeAsList().size)
        driver.close()
    }
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
