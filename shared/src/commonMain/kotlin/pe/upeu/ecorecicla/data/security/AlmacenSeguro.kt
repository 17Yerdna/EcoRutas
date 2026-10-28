package pe.upeu.ecorecicla.data.security
import pe.upeu.ecorecicla.domain.model.Sesion
interface AlmacenSeguro { fun leer(): Sesion?; fun guardar(s: Sesion); fun borrar() }
