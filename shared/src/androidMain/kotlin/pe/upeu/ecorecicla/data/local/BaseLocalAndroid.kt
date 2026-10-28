package pe.upeu.ecorecicla.data.local
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.upeu.ecorecicla.data.local.db.EcoDb
fun baseLocalAndroid(context: Context) = EcoDb(AndroidSqliteDriver(EcoDb.Schema,context,"eco.db"))
