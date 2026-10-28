package pe.upeu.ecorecicla

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pe.upeu.ecorecicla.domain.model.*
import pe.upeu.ecorecicla.presentation.*

private val verde=Color(0xFF205C43)
@Composable fun App(vm: EcoViewModel) {
    val u by vm.ui.collectAsState()
    MaterialTheme(colorScheme=lightColorScheme(primary=verde,secondary=Color(0xFF9B6B24),background=Color(0xFFF4F6F0),surface=Color.White)) {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            if(u.sesion==null) Login(u,vm) else AreaProtegida(u,vm)
        }
    }
}
@Composable private fun Login(u: EcoUi,vm: EcoViewModel) {
    var usuario by remember { mutableStateOf("") }; var clave by remember { mutableStateOf("") }; var visible by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp),verticalArrangement=Arrangement.Center) {
        Text("ECO / RECICLA",color=verde,fontWeight=FontWeight.Bold,letterSpacing=3.sp)
        Spacer(Modifier.height(24.dp)); Text("Cada recojo\ncuenta.",fontSize=42.sp,lineHeight=46.sp,fontWeight=FontWeight.Bold,color=verde)
        Text("Tu ruta, tus pesajes y tu trabajo seguro, incluso sin señal.",Modifier.padding(vertical=16.dp),color=Color.DarkGray)
        OutlinedTextField(usuario,{usuario=it},label={Text("Usuario")},singleLine=true,modifier=Modifier.fillMaxWidth(),enabled=!u.cargando)
        OutlinedTextField(clave,{clave=it},label={Text("Contraseña")},singleLine=true,visualTransformation=if(visible) VisualTransformation.None else PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth().padding(top=12.dp),enabled=!u.cargando)
        TextButton({visible=!visible}) { Text(if(visible) "Ocultar contraseña" else "Mostrar contraseña") }
        Button({vm.login(usuario.trim(),clave)},enabled=!u.cargando && usuario.isNotBlank() && clave.isNotBlank(),modifier=Modifier.fillMaxWidth().height(52.dp)) { Text(if(u.cargando) "Ingresando…" else "Ingresar a mi turno") }
        Mensaje(u)
        Text("EcoRutas · Gestión de recolección",Modifier.padding(top=32.dp),fontSize=12.sp,color=Color.Gray)
    }
}
@Composable private fun Mensaje(u: EcoUi) {
    if(u.cargando) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical=8.dp))
    if(u.mensaje.isNotBlank()) Text(u.mensaje,Modifier.fillMaxWidth().padding(vertical=12.dp),color=if(u.campos.isEmpty()) verde else MaterialTheme.colorScheme.error,fontSize=14.sp)
}
@Composable private fun AreaProtegida(u: EcoUi,vm: EcoViewModel) {
    var tab by remember { mutableStateOf(0) }; var punto by remember { mutableStateOf<PuntoAcopio?>(null) }
    var formulario by remember { mutableStateOf(false) }; var editar by remember { mutableStateOf<Pesaje?>(null) }
    var claveDraft by remember {mutableStateOf("")}
    var anular by remember { mutableStateOf<Pesaje?>(null) }; var omision by remember { mutableStateOf(false) }
    val permisos=u.sesion!!.privilegios
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.fillMaxWidth().background(verde).padding(22.dp)) {
            Text("ECO / RECICLA",color=Color(0xFFB7D6B5),fontSize=11.sp,letterSpacing=3.sp,fontWeight=FontWeight.Bold)
            Text(when(tab){0 -> if(punto==null) "Mi ruta de hoy" else "Punto de acopio";1 -> "Sincronización";else -> "Mi turno"},fontSize=28.sp,fontWeight=FontWeight.Bold,color=Color.White)
            Text("${u.sesion.usuario} · ${u.sesion.rol.lowercase()}",color=Color.White,fontSize=13.sp)
        }
        Row(Modifier.fillMaxWidth().background(Color.White),horizontalArrangement=Arrangement.SpaceEvenly) {
            listOf("Ruta","Sincronizar","Perfil").forEachIndexed { i,t -> TextButton({tab=i;punto=null}) { Text(t,fontWeight=if(tab==i) FontWeight.Bold else FontWeight.Normal) } }
        }
        Column(Modifier.padding(horizontal=18.dp).weight(1f)) {
            Mensaje(u)
            when(tab) {
                0 -> if(punto==null) {
                    Text(u.ruta?.fecha ?: "Ruta sin descargar",Modifier.padding(top=10.dp),fontWeight=FontWeight.Bold)
                    Text("Última sincronización: ${u.ultimaSync}",fontSize=11.sp,color=Color.Gray)
                    Text("${u.pendientes} por sincronizar",Modifier.padding(vertical=4.dp),color=verde,fontWeight=FontWeight.Bold)
                    OutlinedTextField(u.busqueda,{vm.filtrar(it,u.soloPendientes)},label={Text("Buscar punto, dirección o zona")},singleLine=true,modifier=Modifier.fillMaxWidth())
                    Row {Checkbox(u.soloPendientes,{vm.filtrar(u.busqueda,it)});Text("Solo pendientes de pesar",Modifier.padding(top=12.dp))}
                    if(u.ruta==null) { Text("No hay datos locales. Sincronice cuando tenga conexión.",Modifier.padding(vertical=24.dp)); Button(vm::sincronizar,enabled=!u.cargando) {Text("Descargar mi ruta")} }
                    else {
                        if(u.ruta.cerrada) Text("RUTA CERRADA",color=MaterialTheme.colorScheme.error)
                        if(u.ruta.puntos.isEmpty()) Text("No hay puntos planificados")
                        else if(u.puntosFiltrados.isEmpty()) Text("No hay puntos que coincidan con el filtro",Modifier.padding(vertical=16.dp))
                        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=14.dp)) {
                            items(u.puntosFiltrados,key={it.id}) { p ->
                                val atendido=p.atendido || u.pesajes.any { it.puntoId==p.id && it.fechaHora.take(10)==u.ruta.fecha && it.estado is EstadoPesaje.Registrado }
                                Card(onClick={punto=p},shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White)) {
                                    Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)) {
                                        Text(p.secuencia.toString().padStart(2,'0'),fontSize=26.sp,color=verde,fontWeight=FontWeight.Bold)
                                        Column { Text(p.razonSocial,fontWeight=FontWeight.Bold); Text(p.direccion,fontSize=13.sp); Text("${p.zona} · ${p.materialHabitual}",fontSize=12.sp,color=Color.Gray); Text(if(atendido) "✓ Atendido" else "○ Pendiente de visita",color=if(atendido) verde else Color(0xFF9B6B24),fontSize=12.sp) }
                                    }
                                }
                            }
                        }
                        if("RUTA_CERRAR" in permisos && !u.ruta.cerrada) OutlinedButton(vm::cerrar,enabled=!u.cargando,modifier=Modifier.fillMaxWidth()) {Text("Cerrar ruta del día")}
                    }
                } else {
                    val p=punto!!
                    TextButton({punto=null}) { Text("← Volver a la ruta") }
                    Text(p.razonSocial,fontSize=24.sp,fontWeight=FontWeight.Bold); Text(p.direccion); Text("${p.zona} · ${p.materialHabitual}",color=Color.Gray)
                    if("PESAJE_CREAR" in permisos && u.ruta?.cerrada==false) Row {
                        Button({editar=null;claveDraft=vm.nuevaClave();formulario=true}) {Text("Registrar pesaje")}
                        TextButton({omision=true}) {Text("Omitir visita")}
                    }
                    val filas=u.pesajes.filter { it.puntoId==p.id }
                    if(filas.isEmpty()) Text("Aún no hay pesajes en este punto",Modifier.padding(vertical=24.dp))
                    LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(vertical=12.dp)) {
                        items(filas,key={it.id}) { v -> Card(colors=CardDefaults.cardColors(containerColor=Color.White)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("${v.material} · ${if(v.pendiente) "Pendiente" else "Sincronizado"}",fontWeight=FontWeight.Bold,color=verde)
                                Text("Bruto ${v.pesoBruto} kg · Tara ${v.tara} kg")
                                Text(if(v.pendiente) "Neto: por confirmar en servidor" else "Neto: ${v.pesoNeto} kg",fontSize=18.sp,fontWeight=FontWeight.Bold)
                                Text("${v.operario} · ${v.fechaHora}",fontSize=11.sp,color=Color.Gray)
                                Text("Humedad estimada: ${v.humedadEstimada} %",fontSize=13.sp,color=verde)
                                Text("Registro ${v.id}",fontSize=11.sp,color=Color.Gray)
                                if(v.estado is EstadoPesaje.Anulado) Text("Anulado: ${v.estado.motivo}",color=MaterialTheme.colorScheme.error)
                                else Row {
                                    if("PESAJE_EDITAR" in permisos) TextButton({editar=v;formulario=true}) {Text("Corregir")}
                                    if("PESAJE_ANULAR" in permisos) TextButton({anular=v}) {Text("Anular")}
                                }
                            }
                        } }
                    }
                }
                1 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(24.dp)); Text("Tu trabajo está en el equipo",fontSize=26.sp,fontWeight=FontWeight.Bold)
                    Text("${u.pesajes.count { it.pendiente }} pesajes pendientes de envío",Modifier.padding(vertical=16.dp))
                    Text("Se enviarán altas, correcciones y anulaciones; luego se descargarán los datos del servicio.")
                    Button(vm::sincronizar,enabled=!u.cargando,modifier=Modifier.fillMaxWidth().padding(vertical=24.dp).height(52.dp)) {Text("Sincronizar ahora")}
                    Text("Última sincronización\n${u.ultimaSync}",fontSize=13.sp,color=Color.Gray)
                }
                2 -> Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(u.sesion.usuario,Modifier.padding(top=24.dp),fontSize=26.sp,fontWeight=FontWeight.Bold)
                    Text(u.sesion.rol,color=verde)
                    if(u.sesion.soloLectura) Text("Acceso de lectura",Modifier.padding(top=8.dp),color=verde,fontWeight=FontWeight.Bold)
                    Text("Privilegios de este turno",Modifier.padding(top=24.dp,bottom=12.dp),fontWeight=FontWeight.Bold)
                    permisos.sorted().forEach { Text("✓ ${it.replace('_',' ').lowercase()}",Modifier.padding(vertical=4.dp)) }
                    OutlinedButton({vm.salir()},enabled=!u.cargando,modifier=Modifier.fillMaxWidth().padding(top=28.dp)) {Text("Cerrar sesión y limpiar equipo")}
                }
            }
        }
    }
    if(formulario && punto!=null) Formulario(punto!!,editar,u,{formulario=false}) {m,b,t,o,h -> vm.guardar(punto!!.id,m,b,t,o,h,editar,claveDraft,{formulario=false})}
    if(anular!=null) MotivoDialog("Anular pesaje",{anular=null}) {vm.anular(anular!!,it);anular=null}
    if(omision && punto!=null) MotivoDialog("Motivo de omisión",{omision=false}) {vm.omitir(punto!!.id,it);omision=false}
    if(u.confirmarSalida) AlertDialog(onDismissRequest=vm::cancelarSalida,title={Text("Hay trabajo sin sincronizar")},text={Text("Puede conservarlo y volver a intentar, o descartar explícitamente los datos locales de este turno.")},confirmButton={TextButton({vm.salir(true)}) {Text("Descartar y salir")}},dismissButton={TextButton(vm::cancelarSalida) {Text("Conservar trabajo")}})
}
@Composable private fun Formulario(p: PuntoAcopio,anterior: Pesaje?,u: EcoUi,cerrar: () -> Unit,guardar: (String,String,String,String,String) -> Unit) {
    var m by remember { mutableStateOf(anterior?.material ?: p.materialHabitual) }; var b by remember { mutableStateOf(anterior?.pesoBruto?.toString() ?: "") }; var t by remember { mutableStateOf(anterior?.tara?.toString() ?: "") }; var o by remember { mutableStateOf(anterior?.observacion ?: "") }
    var h by remember {mutableStateOf(anterior?.humedadEstimada?.toString() ?: "0")}
    Dialog(onDismissRequest=cerrar) { Surface(shape=RoundedCornerShape(22.dp)) { Column(Modifier.verticalScroll(rememberScrollState()).padding(22.dp)) {
        Text(if(anterior==null) "Nuevo pesaje" else "Corregir pesaje",fontSize=24.sp,fontWeight=FontWeight.Bold)
        Text(p.razonSocial,fontSize=13.sp,color=Color.Gray)
        OutlinedTextField(m,{m=it},label={Text("Material")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(b,{b=it},label={Text("Peso bruto (kg)")},isError=u.campos.containsKey("pesoBruto"),supportingText={u.campos["pesoBruto"]?.let {Text(it)}},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(t,{t=it},label={Text("Tara (kg)")},isError=u.campos.containsKey("tara"),supportingText={u.campos["tara"]?.let {Text(it)}},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(o,{o=it},label={Text("Observación")},modifier=Modifier.fillMaxWidth())
        OutlinedTextField(h,{h=it},label={Text("Humedad estimada (%)")},isError=u.campos.containsKey("humedadEstimada"),supportingText={u.campos["humedadEstimada"]?.let {Text(it)}},modifier=Modifier.fillMaxWidth())
        Mensaje(u)
        Button({guardar(m,b,t,o,h)},enabled=!u.cargando && m.isNotBlank(),modifier=Modifier.fillMaxWidth().padding(top=16.dp)) {Text("Guardar pesaje")}
        TextButton(cerrar,modifier=Modifier.fillMaxWidth()) {Text("Volver al punto")}
    } } }
}
@Composable private fun MotivoDialog(titulo: String,cerrar: () -> Unit,confirmar: (String) -> Unit) {
    var motivo by remember {mutableStateOf("")}
    AlertDialog(onDismissRequest=cerrar,title={Text(titulo)},text={OutlinedTextField(motivo,{motivo=it},label={Text("Motivo obligatorio")})},confirmButton={TextButton({confirmar(motivo)},enabled=motivo.isNotBlank()) {Text("Confirmar")}},dismissButton={TextButton(cerrar) {Text("Cancelar")}})
}
