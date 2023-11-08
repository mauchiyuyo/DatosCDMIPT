package ec.gob.celec.datoscdmipt.database.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import ec.gob.celec.datoscdmipt.database.models.*
import ec.gob.celec.datoscdmipt.sincronizador.MySingleton
import ec.gob.celec.datoscdmipt.sincronizador.NetworkStateChecker
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*

class DatabaseOpenHelper(
 context: Context,
 DATABASE_NAME: String?,
 factory: SQLiteDatabase.CursorFactory?,
 DATABASE_VERSION: Int
) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

 //val dbPath = "/data/data/" + context.packageName + "/databases/"
 val dbPath = "/data/data/ec.gob.celec.datoscdmipt/databases/"
 //val dbPath = context.filesDir.path + "/data/data/ec.gob.celec.datoscdmipt/databases/"
 //"/data/data/ec.gob.celec.datoscdmipt/databases/"

 //val DBPath = Environment.getExternalStorageDirectory().getAbsolutePath() + context.packageName + "/databases/"
 val nameDatabase = "datoscentral.db"
 //val DATABASE_VERSION = 1
 private var mContext: Context? = context
 private var mDatabase: SQLiteDatabase? = null

 companion object{
  // nombres de las tablas
  const val Tabla_Analog = "analog"
  const val Tabla_AnalogValue = "analogvalue_enj"
  const val Tabla_String = "string"
  const val Tabla_StringValue = "stringvalue_enj"
  const val Tabla_Time = "time"
  const val Tabla_TimeValue = "timevalue_enj"
  const val Tabla_Usuario = "usuarios"
  const val Tabla_Centrales = "centrales"
  const val Tabla_Estados = "estados"
  const val Tabla_Negocios = "negocios"
  const val Tabla_Perfiles = "perfiles"
  const val Tabla_Sistemas = "sistemas"
  const val Tabla_SubSistemas = "subsistemas"
  const val Tabla_Tipos = "tipos"
  const val Tabla_Actividades = "actividades"
  const val Tabla_UbicacionIPT = "ubicacion_ipt"

  // columnas analog
  // val Analog_MRID = "mrid"
  // val Analog_Name = "name"
  // val Analog_Pathname = "pathname"
  // val Analog_MaxValue = "maxvalue"
  // val Analog_MinValue = "minvalue"
  // val Analog_OrdenMedicion = "orden_medicion"
  // val Analog_IdNFC = "id_nfc"
  // val Analog_IdSistema = "id_sistema"
  // val Analog_IdTipo = "id_tipo"
  // val Analog_IdEstado = "id_estado"

  // columnas analogvalue_enj
  // val AnalogValue_AMRID = "analog_mrid"
  // val AnalogValue_Loctimestamp = "loctimestamp"
  // val AnalogValue_ValueOrig = "valueorig"
  // val AnalogValue_InsertUser = "insert_user"
  // val AnalogValue_InsertTimestamp = "insert_timestamp"

  // columnas string
  // columnas stringvalue_enj
  // columnas time
  // columnas timevalue_enj

  // columnas usuario
  // val Usuario_usuario = "usuario"
  // val Usuario_password = "password"
  // val Usuario_IdPerfil = "id_perfil"
  // val Usuario_IdCentral = "id_central"

  // columnas estados
  // val Estado_Nombre = "nombre"
  // val Estado_Descripcion = "descripcion"

  // columnas tipos
  // val Tipos_Nombre = "nombre"

  // columnas centrales
  // val Centrales_Codigo = "codigo"
  // val Centrales_Nombre = "nombre"
  // val Centrales_IdNegocio = "id_negocio"

  // columnas actividades
  // columnas negocios
  // columnas perfiles
  // columnas sistemas

  //val IP_SERVER = "172.16.231.6"
  private const val WEB_SERVER = "http://172.16.231.6"

  val UI_SYNCHRONIZE_SQLITE = "ec.gob.celec.datoscdmipt.UI_SYNCHRONIZE_SQLITE"
  //var SYNCHRONIZE_URL =    "http://" + IP_SERVER + ":81/DatosCdM/synchronizemessages.php"
  var URL_SENDANALOGVALUE =   "$WEB_SERVER/DatosCdM/sendanalogvalue.php"
  var URL_SENDSTRINGVALUE =   "$WEB_SERVER/DatosCdM/sendstringvalue.php"
  var URL_SENDTIMEVALUE =     "$WEB_SERVER/DatosCdM/sendtimevalue.php"
  var URL_SENDANALOG =        "$WEB_SERVER/DatosCdM/sendanalog.php"
  var URL_SENDSTRING =        "$WEB_SERVER/DatosCdM/sendstring.php"
  // var URL_SENDTIME =          "$WEB_SERVER/DatosCdM/sendtime.php"
  // var URL_SENDUSUARIOS =      "$WEB_SERVER/DatosCdM/sendusuarios.php"
  // var URL_SENDACTIVIDADES =   "$WEB_SERVER/DatosCdM/sendactividades.php"
  var URL_SENDTIPOS =         "$WEB_SERVER/DatosCdM/sendtipos.php"

  //var URL_SENDANALOGVALUE_ARRAY = "$WEB_SERVER/DatosCdM/sendanalogvaluearray.php"
  //var URL_SENDSTRINGVALUE_ARRAY = "$WEB_SERVER/DatosCdM/sendstringvaluearray.php"
 // var URL_SENDTIMEVALUE_ARRAY =   "$WEB_SERVER/DatosCdM/sendtimevaluearray.php"

 }

 override fun onCreate(db: SQLiteDatabase?){
  Log.d("Camino DB", (mContext?.filesDir?.path ?: "vacio" ) + "/data/ec.gob.celec.datoscdmipt/databases/")
 }

 override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
  //onCreate(db)
 }

 fun openDatabase() : Boolean {
  val dbPath: String = mContext?.getDatabasePath(nameDatabase)!!.path
  //var abierto : Boolean

  Log.d("#DB", dbPath)
//  if(!mDatabase!!.isOpen()) mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)

  if (mDatabase == null ) {
   //&& !mDatabase!!.isOpen()
   //Log.d("#DB", "mDatabase: Problemas")
   mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
   //abierto = true
  } else {
   try {
    if( !mDatabase!!.isOpen() ) mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
    //abierto = true
   } catch (e : SQLException){
    //Log.d("SQL Exception openDatabase", e.stackTrace.toString())
    //abierto = false
   }
  }
  //Log.d("#DB", "BD abierta")
  //Log.d("#DB", mDatabase!!.isOpen().toString() )
  // TODO: finally
  return mDatabase!!.isOpen()
 }

 fun closeDatabase() {
  // TODO: try and catch
  mDatabase?.close()
 }

 fun getAnalog(mCtx: Context, MRID: String) : analog {
  val qry = "select * from $Tabla_Analog where MRID = $MRID"
  val cursor : Cursor
  val db : SQLiteDatabase
  val analog = analog()

  if(openDatabase()){
   db = mDatabase!!

   cursor = db.rawQuery(qry, null)

    if (cursor.count == 0) {
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    } else{
     cursor.moveToFirst()
     analog.MRID = cursor.getInt(cursor.getColumnIndexOrThrow ("mrid"))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     analog.Name = cursor.getString(cursor.getColumnIndexOrThrow ("name"))//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
     analog.AliasName = cursor.getString(cursor.getColumnIndexOrThrow ("aliasname"))//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
     analog.PathName = cursor.getString(cursor.getColumnIndexOrThrow ("pathname"))
     analog.Description = cursor.getString(cursor.getColumnIndexOrThrow ("description"))//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
     analog.MaxValue = cursor.getDouble(cursor.getColumnIndexOrThrow ("maxvalue"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     analog.MinValue = cursor.getDouble(cursor.getColumnIndexOrThrow ("minvalue"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     analog.IdNFC = cursor.getString(cursor.getColumnIndexOrThrow ("id_nfc"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     analog.IdSistema = cursor.getInt(cursor.getColumnIndexOrThrow ("id_sistema"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     analog.IdEstado = cursor.getInt(cursor.getColumnIndexOrThrow ("id_estado"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     analog.Unidades = cursor.getString(cursor.getColumnIndexOrThrow ("unidades"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))

     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()

   //closeDatabase()
  }
  return analog
 }

 fun getString(mCtx: Context, MRID: String) : string {
  val qry = "select * from $Tabla_String where MRID = $MRID"
  val cursor : Cursor
  val db : SQLiteDatabase
  val string = string()

  if(openDatabase()){
   db = mDatabase!!

   cursor = db.rawQuery(qry, null)

    if (cursor.count == 0) {
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     string.MRID          = cursor.getInt(cursor.getColumnIndexOrThrow ("MRID"))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     string.Name          = cursor.getString(cursor.getColumnIndexOrThrow ("NAME"))//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
     string.AliasName     = cursor.getString(cursor.getColumnIndexOrThrow ("ALIASNAME"))//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
     string.PathName      = cursor.getString(cursor.getColumnIndexOrThrow ("PATHNAME"))//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
     string.Description   = cursor.getString(cursor.getColumnIndexOrThrow ("DESCRIPTION"))//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
     string.SELCBO        = cursor.getInt(cursor.getColumnIndexOrThrow ("SELCBO"))
     string.IdNFC         = cursor.getString(cursor.getColumnIndexOrThrow ("id_nfc"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     string.IdSistema     = cursor.getInt(cursor.getColumnIndexOrThrow ("id_sistema"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     string.IdEstado      = cursor.getInt(cursor.getColumnIndexOrThrow ("id_estado"))//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
     string.OrdenMedicion = cursor.getInt(cursor.getColumnIndexOrThrow ("orden_medicion"))
     string.Unidades      = cursor.getString(cursor.getColumnIndexOrThrow ("unidades"))
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()

   //closeDatabase()
  }
  return string
 }

 fun getActividad(mCtx: Context, ahora: String) : actividades{
  val actividad = actividades()
  val fecha = ahora.substringBefore(" ").trim()
  val hora = ahora.substringAfter(" ").trim()
  val cursor : Cursor

  //Log.d("#Obtener actividad fecha ", fecha)
  //Log.d("#Obtener actividad hora ", hora)
  val qry = "select * from actividades a where '$fecha' = a.fecha and '$hora' BETWEEN a.horainicio and a.horafin"
  //Log.d("#Obtener actividad qry ", qry)
  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
    if(cursor.count > 0) {
     cursor.moveToFirst()
     actividad.id = cursor.getInt(0)
     actividad.fecha = cursor.getString(1)
     actividad.horainicio = cursor.getString(2)
     actividad.horafin = cursor.getString(3)
     actividad.id_sistema = cursor.getInt(4)
     actividad.idplanificacion_ipt = cursor.getInt(5)
    }else{
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
  } else {
   //Log.d("getActividad ", "BD no abierta" )
  }
  //closeDatabase()
  return  actividad
 }

 /*fun getSistema(id: Int) : sistemas{
  val sistema = sistemas()
  val cursor : Cursor

  val qry = "select * from sistemas s where '$id' = s.id "

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
    if(cursor.count > 0) {
     cursor.moveToFirst()
     sistema.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
     sistema.nombre = cursor.getString(cursor.getColumnIndexOrThrow( "nombre"))
     sistema.id_central = cursor.getInt(cursor.getColumnIndexOrThrow( "id_central"))
     sistema.formulario = cursor.getString(cursor.getColumnIndexOrThrow( "formulario"))
     sistema.descripcion = cursor.getString(cursor.getColumnIndexOrThrow( "descripcion"))
    }
    cursor.close()
   //}
  }
  //closeDatabase()
  return  sistema
 }*/

 fun getSistemaXIDTipo(mrid: Int, tipo: Int) : sistemas {
  val sistema = sistemas()
  val qry : String
  val cursor : Cursor

  when(tipo){
   1 -> {
    qry = "select * from sistemas s where s.id = (select id_sistema from 'analog' where mrid = $mrid)"
   }
   4 -> {
    qry = "select * from sistemas s where s.id = (select id_sistema from 'string' where mrid = $mrid)"
   }
   else -> {
    qry = ""
   }
  }

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
    if(cursor.count > 0) {
     cursor.moveToFirst()
     sistema.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
     sistema.subsistema = cursor.getString(cursor.getColumnIndexOrThrow( "subsistema"))
     sistema.id_central = cursor.getInt(cursor.getColumnIndexOrThrow( "id_central"))
     sistema.formulario = cursor.getString(cursor.getColumnIndexOrThrow( "formulario"))
     sistema.descripcion = cursor.getString(cursor.getColumnIndexOrThrow( "descripcion"))
    }
    cursor.close()
   //}
  }
  //closeDatabase()
  return  sistema
 }

 fun getListCentrales() : ArrayList<centrales>{
  val centrales = ArrayList<centrales>()
  val cursor : Cursor

  val qry = "select * from $Tabla_Centrales s order by codigo "

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
   if(cursor.count > 0) {
    cursor.moveToFirst()
    do {
     val central = centrales()
     central.codigo = cursor.getInt(cursor.getColumnIndexOrThrow( "codigo"))
     central.nombre = cursor.getString(cursor.getColumnIndexOrThrow( "nombre"))
     central.id_negocio = cursor.getInt(cursor.getColumnIndexOrThrow( "id_negocio"))
     central.sncronizado = cursor.getInt(cursor.getColumnIndexOrThrow( "sincronizado"))
     //central.descripcion = cursor.getString(cursor.getColumnIndexOrThrow( "descripcion"))
     centrales.add(central)
    } while (cursor.moveToNext())
   }else{
    //TODO: cargar mensajes en barra de estado en parte inferior de la pantalla
    //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
   cursor.close()
   //}
  }
  //closeDatabase()

  return  centrales
 }

 fun getListUbicaciones(idCentral : Int) : ArrayList<ubicacion_ipt> {
  val ubicaciones = ArrayList<ubicacion_ipt>()
  val cursor : Cursor

  val qry = "select * from $Tabla_UbicacionIPT s where idCentral = $idCentral order by id "
  Log.d("### getlistubicaciones", idCentral.toString())
  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
   Log.d("### getlistubicaciones curson countd", cursor.count.toString())
   if(cursor.count > 0) {
    cursor.moveToFirst()
    do {
     val ubicacion = ubicacion_ipt()
     ubicacion.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
     ubicacion.nombre = cursor.getString(cursor.getColumnIndexOrThrow( "nombre"))
     ubicacion.idCentral = cursor.getInt(cursor.getColumnIndexOrThrow( "idCentral"))
     ubicacion.sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow( "sincronizado"))
     //central.descripcion = cursor.getString(cursor.getColumnIndexOrThrow( "descripcion"))
     ubicaciones.add(ubicacion)
    } while (cursor.moveToNext())
   }else{
    //TODO: cargar mensajes en barra de estado en parte inferior de la pantalla
    //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
   cursor.close()
   //}
  }
  //closeDatabase()

  return  ubicaciones
 }

 fun getListSistemas(id_ubicacion_ipt : Int) : ArrayList<sistemas>{
  val sistemas = ArrayList<sistemas>()
  val cursor : Cursor

  val qry = "select * from $Tabla_Sistemas s where id <> 8 and id_ubicacion_ipt = $id_ubicacion_ipt order by id "

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
   if(cursor.count > 0) {
    cursor.moveToFirst()
    do {
     val sistema = sistemas()
     sistema.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
     sistema.subsistema = cursor.getString(cursor.getColumnIndexOrThrow( "nombre"))
     sistema.id_central = cursor.getInt(cursor.getColumnIndexOrThrow( "id_central"))
     //sistema.formulario = cursor.getString(cursor.getColumnIndexOrThrow( "formulario"))
     sistema.descripcion = cursor.getString(cursor.getColumnIndexOrThrow( "descripcion"))
     sistema.id_ubicacion_ipt = cursor.getInt(cursor.getColumnIndexOrThrow("id_ubicacion_ipt"))
     sistemas.add(sistema)
    } while (cursor.moveToNext())
   }else{
    //TODO: cargar mensajes en barra de estado en parte inferior de la pantalla
    //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
   cursor.close()
   //}
  }
  //closeDatabase()

  return  sistemas
 }

 fun getListSubSistemas(id_sistema : Int) : ArrayList<subsistemas>{
  val subSistemas = ArrayList<subsistemas>()
  val cursor : Cursor

  val qry = "select * from $Tabla_SubSistemas s where id_sistema = $id_sistema order by id "

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
   if(cursor.count > 0) {
    cursor.moveToFirst()
    do {
     val subsistema = subsistemas()
     subsistema.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
     subsistema.id_sistema = cursor.getInt(cursor.getColumnIndexOrThrow( "id_sistema"))
     subsistema.subsistema = cursor.getString(cursor.getColumnIndexOrThrow( "subsistema"))
     subsistema.sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow( "sincronizado"))
     subSistemas.add(subsistema)
    } while (cursor.moveToNext())
   }else{
    //TODO: cargar mensajes en barra de estado en parte inferior de la pantalla
    //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
   cursor.close()
   //}
  }
  //closeDatabase()

  return  subSistemas
 }

 fun getListSelCBO(mCtx: Context, selcbo: Int) : ArrayList<stringcbo>{
  val selcbolist = ArrayList<stringcbo>()
  val cursor : Cursor

  val qry = "select * from stringcbo s where s.STRING_SELCBO = $selcbo" //order by VALUEORG "

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if(cursor != null){
    if(cursor.count > 0) {
     cursor.moveToFirst()
     do {
      val stringcbo = stringcbo()
      stringcbo.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
      stringcbo.STRING_SELCBO = cursor.getInt(cursor.getColumnIndexOrThrow( "STRING_SELCBO"))
      stringcbo.VALUEORIG = cursor.getString(cursor.getColumnIndexOrThrow( "VALUEORIG"))
      stringcbo.sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow( "sincronizado"))
      selcbolist.add(stringcbo)
     } while (cursor.moveToNext())
    } else {
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
  }
  return  selcbolist
 }

 fun getElementosXEtiqueta(mCtx: Context, idEtiqueta: String): ArrayList<JSONObject> {
    val qry = "SELECT * FROM elementosxetiqueta WHERE idEtiqueta = '$idEtiqueta'"
    // TODO: obtener los elementos por etiqueta
    val cursor : Cursor
    val db : SQLiteDatabase
    val elementosJSON = ArrayList<JSONObject>()

    if(openDatabase()){
        db = mDatabase!!
        cursor = db.rawQuery(qry, null)
        // obtiene el listado de todos los datos analógicos
        //if (cursor != null) {
            if (cursor.count == 0) {
                Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
            }
            else{
                cursor.moveToFirst()
                do{
                 val elementoJson = JSONObject()
                 elementoJson.put("ID", cursor.getInt(cursor.getColumnIndexOrThrow( "mrid")))
                 if(cursor.getInt(cursor.getColumnIndexOrThrow( "mrid")) == 1){
                     elementoJson.put("tp", 1)
                 } else {
                     elementoJson.put("tp", 4)
                 }
                 elementosJSON.add(elementoJson)
                }while(cursor.moveToNext())
             ////Log.d("#DB - Registros: ", cursor.count.toString())
             //Toast.makeText(mCtx,"${cursor.count.toString()} Registros Analógicos encontrados", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
       // }
        //else{
       //     Toast.makeText(mCtx, "Sin datos Analógicos", Toast.LENGTH_SHORT).show()
        //}
     }
     //return analoglist
     return elementosJSON
 }

 //fun getAnalogList(mCtx : Context) : ArrayList<analog> {
  fun getElementosList(mCtx: Context, idsistema: Int) : ArrayList<JSONObject> {
  //val qry = "select mrid, name, aliasname, pathname, id_sistema, id_nfc, id_estado, orden_medicion from $Tabla_Analog where id_nfc <> 'Si' order by id_sistema, orden_medicion"
  //val qrys = "select mrid, name, aliasname, pathname, id_sistema, id_nfc, id_estado, orden_medicion from $Tabla_String where id_nfc <> 'Si' order by id_sistema, orden_medicion"
  val qry = "select mrid, name, aliasname, pathname, description, maxvalue, minvalue, id_sistema, id_subsistema, orden_medicion, ifnull(id_nfc,'') as id_nfc, 1 as id_tipo, id_estado \n" +
    "from analog  \n" +
    "where id_sistema = $idsistema \n" +
    "union  \n" +
    "select mrid, name, aliasname, pathname, description, 0, 0, id_sistema, id_subsistema, orden_medicion, ifnull(id_nfc,'') as id_nfc, 4 as id_tipo, id_estado \n" +
    "from string  \n" +
    "where id_sistema = $idsistema \n" +
    "order by mrid, orden_medicion "

  //"where (id_nfc is null or id_nfc not like \"%Si%\") \n" +
  //"where (id_nfc is null or id_nfc not like \"%Si%\") \n" +

  //Log.d("### getElementosList", idsistema.toString())
  val cursor : Cursor
  val db : SQLiteDatabase
  val elementosJSON = ArrayList<JSONObject>()

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   // obtiene el listado de todos los datos analógicos
   //if (cursor != null) {
    if (cursor.count == 0) {
     Toast.makeText(mCtx, "Sin registros Analógicos almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
//      val analogvalue = analog()
/*      analogvalue.AMRID = cursor.getInt(0)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      analogvalue.Name = cursor.getString(1)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      analogvalue.AliasName = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      analogvalue.PathName = cursor.getString(3)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      analogvalue.IdSistema = cursor.getInt(4)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalue.IdNFC = cursor.getString(5)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalue.IdEstado = cursor.getInt(6)
      analoglist.add(analogvalue)*/
      val elementoJson = JSONObject()

      elementoJson.put("AMRID", cursor.getInt(cursor.getColumnIndexOrThrow( "mrid")))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("Name", cursor.getString(cursor.getColumnIndexOrThrow( "name")))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put(
       "AliasName",
       cursor.getString(cursor.getColumnIndexOrThrow( "aliasname"))
      )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put(
       "PathName",
       cursor.getString(cursor.getColumnIndexOrThrow( "pathname"))
      )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put(
       "IdSistema",
       cursor.getInt(cursor.getColumnIndexOrThrow( "id_sistema"))
      )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put(
       "IdSubSistema",
       cursor.getInt(cursor.getColumnIndexOrThrow( "id_subsistema"))
      )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put(
       "IdNFC",
       cursor.getString(cursor.getColumnIndexOrThrow( "id_nfc"))
      )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put(
       "IdEstado",
       cursor.getInt(cursor.getColumnIndexOrThrow( "id_estado"))
      )
      elementoJson.put(
       "OrdenMedicion",
       cursor.getInt(cursor.getColumnIndexOrThrow( "orden_medicion"))
      )
      elementoJson.put("id_tipo", cursor.getInt(cursor.getColumnIndexOrThrow( "id_tipo")))

      elementosJSON.add(elementoJson)

     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros Analógicos encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //else{
   // Toast.makeText(mCtx, "Sin datos Analógicos", Toast.LENGTH_SHORT).show()
   //}
   // obtiene listado de datos de tipo String
   /*cursors = db.rawQuery(qrys, null)
   if (cursors != null) {
    if (cursors.count == 0) {
     Toast.makeText(mCtx, "Sin registros String almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursors.moveToFirst()
     do{
      val analogvalue = analog()
/*      analogvalue.AMRID = cursor.getInt(0)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      analogvalue.Name = cursor.getString(1)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      analogvalue.AliasName = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      analogvalue.PathName = cursor.getString(3)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      analogvalue.IdSistema = cursor.getInt(4)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalue.IdNFC = cursor.getString(5)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalue.IdEstado = cursor.getInt(6)
      analoglist.add(analogvalue)*/
      var elementoJson = JSONObject()

      elementoJson.put("AMRID", cursors.getInt(0))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("Name", cursors.getString(1))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("AliasName", cursors.getString(2))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("PathName", cursors.getString(3))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("IdSistema", cursors.getInt(4))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("IdNFC", cursors.getString(5))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("IdEstado", cursors.getInt(6))
      elementoJson.put("OrdenMedicion", cursor.getInt(7))
      elementoJson.put("TipoDato", Tabla_String)

      elementosJSON.add(elementoJson)

     }while(cursors.moveToNext())
     ////Log.d("#DB - Registros: ", cursors.count.toString())
     //Toast.makeText(mCtx,"${cursors.count.toString()} Registros String encontrados", Toast.LENGTH_SHORT).show()
    }
    cursors.close()
   }
   else{
    Toast.makeText(mCtx,"Sin datos String", Toast.LENGTH_SHORT).show()
   }*/
   //closeDatabase()
  }
  //return analoglist
  return elementosJSON
 }

 fun getElementosListSub(mCtx: Context, id_subsistema: Int) : ArrayList<JSONObject> {
  //val qry = "select mrid, name, aliasname, pathname, id_sistema, id_nfc, id_estado, orden_medicion from $Tabla_Analog where id_nfc <> 'Si' order by id_sistema, orden_medicion"
  //val qrys = "select mrid, name, aliasname, pathname, id_sistema, id_nfc, id_estado, orden_medicion from $Tabla_String where id_nfc <> 'Si' order by id_sistema, orden_medicion"
  val qry = "select mrid, name, aliasname, pathname, description, maxvalue, minvalue, id_sistema, id_subsistema, orden_medicion, ifnull(id_nfc,'') as id_nfc, 1 as id_tipo, id_estado \n" +
          "from analog  \n" +
          "where id_subsistema = $id_subsistema \n" +
          "union  \n" +
          "select mrid, name, aliasname, pathname, description, 0, 0, id_sistema, id_subsistema, orden_medicion, ifnull(id_nfc,'') as id_nfc, 4 as id_tipo, id_estado \n" +
          "from string  \n" +
          "where id_subsistema = $id_subsistema \n" +
          "order by mrid, orden_medicion "

  //"where (id_nfc is null or id_nfc not like \"%Si%\") \n" +
  //"where (id_nfc is null or id_nfc not like \"%Si%\") \n" +

  //Log.d("### getElementosList", idsistema.toString())
  val cursor : Cursor
  val db : SQLiteDatabase
  val elementosJSON = ArrayList<JSONObject>()

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   // obtiene el listado de todos los datos analógicos
   //if (cursor != null) {
   if (cursor.count == 0) {
    Toast.makeText(mCtx, "Sin registros Analógicos almacenados", Toast.LENGTH_SHORT).show()
   }
   else{
    cursor.moveToFirst()
    do{
//      val analogvalue = analog()
     /*      analogvalue.AMRID = cursor.getInt(0)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
           analogvalue.Name = cursor.getString(1)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
           analogvalue.AliasName = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
           analogvalue.PathName = cursor.getString(3)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
           analogvalue.IdSistema = cursor.getInt(4)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
           analogvalue.IdNFC = cursor.getString(5)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
           analogvalue.IdEstado = cursor.getInt(6)
           analoglist.add(analogvalue)*/
     val elementoJson = JSONObject()

     elementoJson.put("AMRID", cursor.getInt(cursor.getColumnIndexOrThrow( "mrid")))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put("Name", cursor.getString(cursor.getColumnIndexOrThrow( "name")))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put(
      "AliasName",
      cursor.getString(cursor.getColumnIndexOrThrow( "aliasname"))
     )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put(
      "PathName",
      cursor.getString(cursor.getColumnIndexOrThrow( "pathname"))
     )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put(
      "IdSistema",
      cursor.getInt(cursor.getColumnIndexOrThrow( "id_sistema"))
     )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put(
      "IdSubSistema",
      cursor.getInt(cursor.getColumnIndexOrThrow( "id_subsistema"))
     )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put(
      "IdNFC",
      cursor.getString(cursor.getColumnIndexOrThrow( "id_nfc"))
     )//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
     elementoJson.put(
      "IdEstado",
      cursor.getInt(cursor.getColumnIndexOrThrow( "id_estado"))
     )
     elementoJson.put(
      "OrdenMedicion",
      cursor.getInt(cursor.getColumnIndexOrThrow( "orden_medicion"))
     )
     elementoJson.put("id_tipo", cursor.getInt(cursor.getColumnIndexOrThrow( "id_tipo")))

     elementosJSON.add(elementoJson)

    }while(cursor.moveToNext())
    ////Log.d("#DB - Registros: ", cursor.count.toString())
    //Toast.makeText(mCtx,"${cursor.count.toString()} Registros Analógicos encontrados", Toast.LENGTH_SHORT).show()
   }
   cursor.close()
   //}
   //else{
   // Toast.makeText(mCtx, "Sin datos Analógicos", Toast.LENGTH_SHORT).show()
   //}
   // obtiene listado de datos de tipo String
   /*cursors = db.rawQuery(qrys, null)
   if (cursors != null) {
    if (cursors.count == 0) {
     Toast.makeText(mCtx, "Sin registros String almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursors.moveToFirst()
     do{
      val analogvalue = analog()
/*      analogvalue.AMRID = cursor.getInt(0)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      analogvalue.Name = cursor.getString(1)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      analogvalue.AliasName = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      analogvalue.PathName = cursor.getString(3)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      analogvalue.IdSistema = cursor.getInt(4)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalue.IdNFC = cursor.getString(5)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalue.IdEstado = cursor.getInt(6)
      analoglist.add(analogvalue)*/
      var elementoJson = JSONObject()

      elementoJson.put("AMRID", cursors.getInt(0))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("Name", cursors.getString(1))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("AliasName", cursors.getString(2))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("PathName", cursors.getString(3))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("IdSistema", cursors.getInt(4))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("IdNFC", cursors.getString(5))//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      elementoJson.put("IdEstado", cursors.getInt(6))
      elementoJson.put("OrdenMedicion", cursor.getInt(7))
      elementoJson.put("TipoDato", Tabla_String)

      elementosJSON.add(elementoJson)

     }while(cursors.moveToNext())
     ////Log.d("#DB - Registros: ", cursors.count.toString())
     //Toast.makeText(mCtx,"${cursors.count.toString()} Registros String encontrados", Toast.LENGTH_SHORT).show()
    }
    cursors.close()
   }
   else{
    Toast.makeText(mCtx,"Sin datos String", Toast.LENGTH_SHORT).show()
   }*/
   //closeDatabase()
  }
  //return analoglist
  return elementosJSON
 }

 fun getAnalogValue(mCtx: Context, IdActividad: Int, MRID: Int) : Int {
  var existe = 0

  val qry = "select * from $Tabla_AnalogValue where ANALOG_MRID = $MRID and id_actividad = $IdActividad order by LOCTIMESTAMP DESC"
  val db : SQLiteDatabase
  val cursor : Cursor

  if (openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
   existe = if (cursor.count == 0) {

    0
   }
   else{
    cursor.count
   }
   if(existe == 0 ){
    Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
    cursor.close()
   //}
   //else{
    //existe = 0
   //}
   //closeDatabase()
  }
  else{
   //Log.d("#DB", "La BD no abierta")
  }

  return  existe
 }

 fun getStringValue(mCtx: Context, IdActividad: Int, MRID: Int) : Int {
  var existe = 0
  val qry = "select * from $Tabla_StringValue where STRING_MRID = $MRID and id_actividad = $IdActividad order by LOCTIMESTAMP DESC"
  val db : SQLiteDatabase
  val cursor : Cursor

  if (openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     existe = 0
    }
    else{
     existe = cursor.count
    }
   if(existe == 0){
    Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
    cursor.close()
   //}
   //else{
    //existe = 0
   //}
   //closeDatabase()
  }
  else{
   //Log.d("#DB", "La BD no abierta")
  }

  return  existe
 }

 fun getTimeValue(mCtx: Context, IdActividad : Int, MRID: Int) : Int{
  var existe = 0
  val qry = "select * from $Tabla_TimeValue where TIME_MRID = $MRID and id_actividad = $IdActividad order by LOCTIMESTAMP DESC"
  val db : SQLiteDatabase
  val cursor : Cursor

  if (openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     existe = 0
      Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
     }
    else{
     existe = cursor.count
    }
    cursor.close()
   //}
   //else{
   // existe = 0
   //}
   //closeDatabase()
  }

  return  existe
 }

 fun getAnalogValues(mCtx: Context, MRID: String, periodo: Int) : ArrayList<analogvalue_enj>{
  val qry : String
  val db : SQLiteDatabase
  val cursor : Cursor
  val analogvalues = ArrayList<analogvalue_enj>()

  when(periodo){
   0 -> {
    qry =
     "select * from $Tabla_AnalogValue where ANALOG_MRID = $MRID order by LOCTIMESTAMP"// DESC"
   }
   24 -> {
    qry =
     "select * from $Tabla_AnalogValue where ANALOG_MRID = $MRID and LOCTIMESTAMP >= datetime('now', '-24 hours') order by LOCTIMESTAMP"// DESC"
   }
   30 -> {
    qry =
     "select * from $Tabla_AnalogValue where ANALOG_MRID = $MRID and LOCTIMESTAMP >= datetime('now', '-720 hours') order by LOCTIMESTAMP"// DESC"
   }
   72 -> {
    qry =
     "select * from $Tabla_AnalogValue where ANALOG_MRID = $MRID and LOCTIMESTAMP >= datetime('now', '-72 hours') order by LOCTIMESTAMP"// DESC"
   }
   else -> {
    qry = "select * from $Tabla_AnalogValue where ANALOG_MRID = $MRID order by LOCTIMESTAMP"// DESC"
   }
  }

  if (openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val analogvalue = analogvalue_enj()
      analogvalue.ANALOG_MRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      analogvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      analogvalue.ValueOrig = cursor.getDouble(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      analogvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      analogvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalues.add(analogvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   //}
   //closeDatabase()
  }
  else{
   //Log.d("#DB", "La BD no abierta")
  }
  return analogvalues
 }

 fun obtenerPorSincronizarAnalogValue(): Cursor? {
  val qry = "select * from $Tabla_AnalogValue where sincronizado = 1 order by LOCTIMESTAMP DESC"
  var cursor : Cursor? = null
  //val analogvalues = ArrayList<analogvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
/*   if (cursor != null) {
    if (cursor.count == 0) {
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val analogvalue = analogvalue_enj()
      analogvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      analogvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      analogvalue.ValueOrig = cursor.getDouble(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      analogvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      analogvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      analogvalues.add(analogvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }

 fun getStringValues(mCtx: Context, MRID: String, periodo: Int) : ArrayList<stringvalue_enj>{
  val qry: String
  val db : SQLiteDatabase
  val cursor : Cursor
  val stringvalues = ArrayList<stringvalue_enj>()

  when(periodo){
   0 -> {
    qry =
     "select * from $Tabla_StringValue where STRING_MRID = $MRID order by LOCTIMESTAMP"// DESC"
   }
   24 -> {
    qry =
     "select * from $Tabla_StringValue where STRING_MRID = $MRID and LOCTIMESTAMP >= datetime('now', '-24 hours') order by LOCTIMESTAMP"// DESC"
   }
   30 -> {
    qry =
     "select * from $Tabla_StringValue where STRING_MRID = $MRID and LOCTIMESTAMP >= datetime('now', '-720 hours') order by LOCTIMESTAMP"// DESC"
   }
   72 -> {
    qry =
     "select * from $Tabla_StringValue where STRING_MRID = $MRID and LOCTIMESTAMP >= datetime('now', '-72 hours') order by LOCTIMESTAMP"// DESC"
   }
   else -> {
    qry = "select * from $Tabla_StringValue where STRING_MRID = $MRID order by LOCTIMESTAMP"// DESC"
   }
  }

  if (openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.STRING_MRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //else{
   // Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   //}
   //closeDatabase()
  }
  else{
   //Log.d("#DB", "La BD no abierta")
  }
  return stringvalues
 }

 fun obtenerPorSincronizarStringValue() : Cursor? {
  val qry = "select * from $Tabla_StringValue where sincronizado = 1 order by LOCTIMESTAMP DESC"
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }

 fun obtenerPorSincronizarTimeValue() : Cursor? {
  val qry = "select * from $Tabla_TimeValue where sincronizado = 1 order by LOCTIMESTAMP DESC"
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }

 fun obtenerPorSincronizarAnalog() : Cursor? {
  val qry = "select * from $Tabla_Analog where sincronizado = 1 order by INSERT_TIMESTAMP DESC"
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }

 fun obtenerPorSincronizarString() : Cursor? {
  val qry = "select * from $Tabla_String where sincronizado = 1 order by INSERT_TIMESTAMP DESC"
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }

 /*fun obtenerPorSincronizarTime() : Cursor? {
  val qry = "select * from $Tabla_Time where sincronizado = 1 order by INSERT_TIMESTAMP DESC"
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }*/

 /*fun obtenerPorSincronizarActividades() : Cursor? {
  val qry = "select * from $Tabla_Actividades where sincronizado = 1 order by fecha DESC, horainicio DESC"
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }*/

 fun obtenerPorSincronizarTipos() : Cursor? {
  val qry = "select * from $Tabla_Tipos where sincronizado = 1 "
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }

 /*fun obtenerPorSincronizarUsuarios() : Cursor? {
  val qry = "select * from $Tabla_Usuario where sincronizado = 1 "
  var cursor : Cursor? = null
  //val stringvalues = ArrayList<stringvalue_enj>()

  if(openDatabase()){
   val db : SQLiteDatabase = mDatabase!!
   cursor = db.rawQuery(qry, null)
   /*if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     do{
      val stringvalue = stringvalue_enj()
      stringvalue.AMRID = cursor.getInt(1)//cursor.getColumnIndexOrThrow( AnalogValue_AMRID))
      stringvalue.Loctimestamp = cursor.getString(2)//cursor.getColumnIndexOrThrow( AnalogValue_Loctimestamp))
      stringvalue.ValueOrig = cursor.getString(4)//cursor.getColumnIndexOrThrow( AnalogValue_ValueOrig))
      stringvalue.InsertUser = cursor.getString(8)//cursor.getColumnIndexOrThrow( AnalogValue_InsertUser))
      stringvalue.InsertTimestamp = cursor.getString(9)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalue.IdActividad = cursor.getInt(12)//cursor.getColumnIndexOrThrow( AnalogValue_InsertTimestamp))
      stringvalues.add(stringvalue)
     }while(cursor.moveToNext())
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   }
   else{
    //Toast.makeText(mCtx, "Sin datos", Toast.LENGTH_SHORT).show()
   }*/
  }
  //closeDatabase()
  return cursor
 }*/

 fun getUsuario(Usuario: String) : usuario {
  val qry = "select id, usuario, id_perfil, id_central from $Tabla_Usuario where usuario = '$Usuario'"
  val cursor : Cursor
  val db : SQLiteDatabase
  val usuario = usuario()

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     usuario.usuario = cursor.getString(1)
     usuario.idPerfil = cursor.getInt(2)
     usuario.idCentral = cursor.getInt(3)
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()

   //closeDatabase()
  }
  return usuario
 }

 fun getUsuarioByID(ID: String) : usuario {
  val qry = "select id, usuario, id_perfil, id_central from $Tabla_Usuario where id = '$ID'"
  val cursor : Cursor
  val db : SQLiteDatabase
  val usuario = usuario()

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   if (cursor.count == 0) {
    //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
   }
   else{
    cursor.moveToFirst()
    usuario.usuario = cursor.getString(1)
    usuario.idPerfil = cursor.getInt(2)
    usuario.idCentral = cursor.getInt(3)
    ////Log.d("#DB - Registros: ", cursor.count.toString())
    //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
   }
   cursor.close()

   //closeDatabase()
  }
  return usuario
 }
 /*fun loginAD(mCtx: Context, Usuario: String, Password: String) : Boolean{
  val correcto : Boolean// = false

  // llama a servicio web y envía datos usuario y password

  correcto = true

  return correcto
 }*/

 fun updateIdNFC(id_nfc: String, mrid: Int, idtipo: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  //updateValue.put("id_nfc", id_nfc)
  if(openDatabase()){
   when (idtipo){
    1 -> {
     updateValue.put("id_nfc", "Si - $id_nfc")
     actualizado = mDatabase?.update(
      Tabla_Analog,
      updateValue,
      "mrid = ?",
      arrayOf(mrid.toString())
     )!!
    }
    4 -> {
     updateValue.put("id_nfc", "Si - $id_nfc")
     actualizado = mDatabase?.update(
      Tabla_String,
      updateValue,
      "mrid = ?",
      arrayOf(mrid.toString())
     )!!
    }
   }
  }
  //closeDatabase()
  return actualizado
 }

 /*fun updateSincronizado(sincronizado: Int, mrid: Long, idtipo: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  updateValue.put("sincronizado", sincronizado)
  if(openDatabase()){
   when (idtipo){
    1 -> {
     updateValue.put("sincronizado", sincronizado)
     actualizado = mDatabase?.update(
      Tabla_AnalogValue,
      updateValue,
      "id = ?",
      arrayOf(mrid.toString())
     )!!
    }
    2 -> {
     updateValue.put("sincronizado", sincronizado)
     actualizado = mDatabase?.update(
      Tabla_StringValue,
      updateValue,
      "id = ?",
      arrayOf(mrid.toString())
     )!!
    }
    5 -> {
     updateValue.put("sincronizado", sincronizado)
     actualizado = mDatabase?.update(
      Tabla_TimeValue,
      updateValue,
      "id = ?",
      arrayOf(mrid.toString())
     )!!
    }
   }
  }
  //closeDatabase()
  return actualizado
 }*/

 fun updateSincronizadoAnalog(sincronizado: Int, mrid: Long) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    Tabla_Analog,
    updateValue,
    "mrid = ?",
    arrayOf(mrid.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }

 fun updateSincronizadoString(sincronizado: Int, mrid: Long) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    Tabla_String,
    updateValue,
    "mrid = ?",
    arrayOf(mrid.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }

 /*fun updateSincronizadoTime(sincronizado: Int, mrid: Long, idtipo: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    Tabla_Time,
    updateValue,
    "mrid = ?",
    arrayOf(mrid.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }*/

 fun updateSincronizadoAnalogValue(sincronizado: Int, mrid: Int, id_actividad: Int) : Int {
  val actualizado: Int
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    Tabla_AnalogValue,
    updateValue,
    "analog_mrid = ? and id_actividad = ?",
    arrayOf(mrid.toString(), id_actividad.toString())
   )!!
  } else { actualizado = 0 }
  //closeDatabase()
  return actualizado
 }

 fun updateSincronizadoStringValue(sincronizado: Int, mrid: Int, id_actividad: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    Tabla_StringValue,
    updateValue,
    "string_mrid = ? and id_actividad = ?",
    arrayOf(mrid.toString(), id_actividad.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }

 fun updateSincronizadoTimeValue(sincronizado: Int, mrid: Int, id_actividad: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    Tabla_TimeValue,
    updateValue,
    "time_mrid = ? and id_actividad = ? ",
    arrayOf(mrid.toString(), id_actividad.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }

 /*fun updateSincronizadoActividad(sincronizado: Int, mrid: Long, idtipo: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    "actividades",
    updateValue,
    "id = ?",
    arrayOf(mrid.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }*/

 fun updateSincronizadoTipos(sincronizado: Int, id: Long) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    "tipos",
    updateValue,
    "id = ?",
    arrayOf(id.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }

 /*fun updateSincronizadoUsuario(sincronizado: Int, mrid: Long, idtipo: Int) : Int {
  var actualizado = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("sincronizado", sincronizado)
   actualizado = mDatabase?.update(
    "usuarios",
    updateValue,
    "id = ?",
    arrayOf(mrid.toString())
   )!!
  }
  //closeDatabase()
  return actualizado
 }*/

 fun insertarAnalogValue(elanalogvalue: analogvalue_enj) : Long {
  var insertado : Long = 0
  try {

   //var laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertAnalogvalue = ContentValues()
   insertAnalogvalue.put("ANALOG_MRID", elanalogvalue.ANALOG_MRID)
   insertAnalogvalue.put("LOCTIMESTAMP", elanalogvalue.Loctimestamp) // laFechaT.format(fecha))//elanalogvalue.Loctimestamp)
   insertAnalogvalue.put("VALUEORIG", elanalogvalue.ValueOrig)
   insertAnalogvalue.put("VALUEEDIT", elanalogvalue.ValueOrig)
   insertAnalogvalue.put("INSERT_USER", elanalogvalue.InsertUser)
   insertAnalogvalue.put("INSERT_TIMESTAMP", elanalogvalue.InsertTimestamp) // laFechaT.format(fecha))
   insertAnalogvalue.put("id_actividad", elanalogvalue.IdActividad)
   insertAnalogvalue.put("origen", Build.ID)
   insertAnalogvalue.put("observacion", elanalogvalue.Observacion)
   insertAnalogvalue.put("idobservaciones", elanalogvalue.IdObservaciones)
   if(openDatabase()){
    //mDatabase?.beginTransaction()
    insertado = mDatabase?.insert(Tabla_AnalogValue, null, insertAnalogvalue)!!
    //mDatabase?.setTransactionSuccessful()
   }
  } catch (e: Exception){
   Log.w("Error inserción AnalogValue", e.toString())
  } finally {
   //mDatabase?.endTransaction()
   //closeDatabase()

  }
  return insertado
 }

 fun insertarStringValue(elstringvalue: stringvalue_enj) : Long {
  var insertado : Long = 0
  try {

   //var laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertStringValue = ContentValues()
   insertStringValue.put("STRING_MRID", elstringvalue.STRING_MRID)
   insertStringValue.put("LOCTIMESTAMP", elstringvalue.Loctimestamp)
   insertStringValue.put("VALUEORIG", elstringvalue.ValueOrig)
   insertStringValue.put("VALUEEDIT", elstringvalue.ValueOrig)
   insertStringValue.put("INSERT_USER", elstringvalue.InsertUser)
   insertStringValue.put("INSERT_TIMESTAMP", elstringvalue.InsertTimestamp)
   insertStringValue.put("id_actividad", elstringvalue.IdActividad)
   insertStringValue.put("origen", Build.ID)
   insertStringValue.put("observacion", elstringvalue.Observacion)
   insertStringValue.put("idobservaciones", elstringvalue.IdObservaciones)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_StringValue, null, insertStringValue)!!
   }
   //closeDatabase()
  } catch (e: Exception) {
   Log.w("Error inserción StringValue", e.toString())
  } finally {
   //mDatabase?.endTransaction()
   //closeDatabase()
  }
  return insertado
 }

 fun getTimeXSistema(idSistema : Int, horaOfecha : String) : time{
  val elTime = time()
  val qry = "select * from $Tabla_Time where id_sistema = $idSistema and unidades = '$horaOfecha'"
  val cursor : Cursor
  val db : SQLiteDatabase
  //Log.d("### getTimeXSistema - ",qry)

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     elTime.MRID = cursor.getInt(cursor.getColumnIndexOrThrow( "MRID"))
     elTime.IdSistema = cursor.getInt(cursor.getColumnIndexOrThrow( "id_sistema"))
     elTime.IdTipo = cursor.getInt(cursor.getColumnIndexOrThrow( "id_tipo"))
     elTime.Unidades = cursor.getString(cursor.getColumnIndexOrThrow( "unidades"))

     //elTime.Name = cursor.getInt(2)

     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //closeDatabase()
  }
  return elTime
 }

 fun getTimeValueObj(timermid : Int, actividad : Int) : timevalue_enj{
  val elTimeValue = timevalue_enj()
  val qry = "select * from $Tabla_TimeValue where time_mrid = $timermid and id_actividad = $actividad"
  val cursor : Cursor
  val db : SQLiteDatabase

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     elTimeValue.TIME_MRID = cursor.getInt(cursor.getColumnIndexOrThrow( "TIME_MRID"))
     elTimeValue.IdActividad = cursor.getInt(cursor.getColumnIndexOrThrow( "id_actividad"))
     elTimeValue.IdObservaciones = cursor.getInt(cursor.getColumnIndexOrThrow( "idobservaciones"))
     elTimeValue.InsertTimestamp = cursor.getString(cursor.getColumnIndexOrThrow( "INSERT_TIMESTAMP"))
     elTimeValue.Loctimestamp = cursor.getString(cursor.getColumnIndexOrThrow( "LOCTIMESTAMP"))
     elTimeValue.InsertUser = cursor.getString(cursor.getColumnIndexOrThrow( "INSERT_USER"))
     elTimeValue.Observacion = cursor.getString(cursor.getColumnIndexOrThrow( "observacion"))
     elTimeValue.Origen = cursor.getString(cursor.getColumnIndexOrThrow( "origen"))
     elTimeValue.ValueEdit = cursor.getString(cursor.getColumnIndexOrThrow( "VALUEEDIT"))
     elTimeValue.ValueOrig = cursor.getString(cursor.getColumnIndexOrThrow( "VALUEORIG"))
     //elTime.Name = cursor.getInt(2)
     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //closeDatabase()
  }
  return elTimeValue
 }

 fun insertarTimeValue(timevalue : timevalue_enj) : Long {
  var insertado : Long = 0
  try {
   //var laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertTimeValue = ContentValues()
   insertTimeValue.put("TIME_MRID", timevalue.TIME_MRID)
   insertTimeValue.put("LOCTIMESTAMP", timevalue.Loctimestamp) //laFechaT.format(fecha))//elanalogvalue.Loctimestamp)
   insertTimeValue.put("VALUEORIG", timevalue.ValueOrig)
   insertTimeValue.put("VALUEEDIT", timevalue.ValueOrig)
   insertTimeValue.put("INSERT_USER", timevalue.InsertUser)
   insertTimeValue.put("INSERT_TIMESTAMP", timevalue.InsertTimestamp) // laFechaT.format(fecha))
   insertTimeValue.put("id_actividad", timevalue.IdActividad)
   insertTimeValue.put("origen", Build.ID)
   insertTimeValue.put("observacion", timevalue.Observacion)
   insertTimeValue.put("idobservaciones", timevalue.IdObservaciones)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_TimeValue, null, insertTimeValue)!!
   }
   //closeDatabase()
  }catch (e: Exception){
   Log.w("Error inserción TimeValue", e.toString())
  }
  return insertado
 }

 fun getRptSistema(Sistema : Int, periodo : Int) : Cursor {
  val qry : String

  when(Sistema){
   1 -> { // AireComprimidaSF6 - fragment_sistema_aire_comprimida_s_f6
    qry = "SELECT Horario.fecha, Horario.horainicio, \n" +
      "  str.AirComp_Principal, ana.AirComp_HMI_1, ana.AirComp_HMI_2, ana.AirComp_Acum_1, ana.AirComp_Acum_2, ana.AirComp_Acum_3, \n" +
      "  str.U1_SF6_FA, str.U1_SF6_FB, str.U1_SF6_FC, str.U1_TAP, str.U2_SF6_FA, str.U2_SF6_FB, str.U2_SF6_FC, str.U2_TAP, \n" +
      "  str.U3_SF6_FA, str.U3_SF6_FB, str.U3_SF6_FC, str.U3_TAP \n" +
      "  from (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
      " FROM timevalue_enj b  \n" +
      " WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
      " AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema)  \n" +
      " GROUP BY loctimestamp) Horario \n" +
      " INNER JOIN (select sv.LOCTIMESTAMP, MAX(case when sv.STRING_MRID = 650015 then sv.VALUEORIG end) as AirComp_Principal, \n" +
      "   MAX(case when sv.STRING_MRID = 651011 then sv.VALUEORIG end) as U1_SF6_FA, \n" +
      "   MAX(case when sv.STRING_MRID = 651012 then sv.VALUEORIG end) as U1_SF6_FB, \n" +
      "   MAX(case when sv.STRING_MRID = 651013 then sv.VALUEORIG end) as U1_SF6_FC, \n" +
      "   MAX(case when sv.STRING_MRID = 650016 then sv.VALUEORIG end) as U1_TAP, \n" +
      "   MAX(case when sv.STRING_MRID = 652011 then sv.VALUEORIG end) as U2_SF6_FA, \n" +
      "   MAX(case when sv.STRING_MRID = 652012 then sv.VALUEORIG end) as U2_SF6_FB, \n" +
      "   MAX(case when sv.STRING_MRID = 652013 then sv.VALUEORIG end) as U2_SF6_FC, \n" +
      "   MAX(case when sv.STRING_MRID = 650017 then sv.VALUEORIG end) as U2_TAP, \n" +
      "   MAX(case when sv.STRING_MRID = 653011 then sv.VALUEORIG end) as U3_SF6_FA, \n" +
      "   MAX(case when sv.STRING_MRID = 653012 then sv.VALUEORIG end) as U3_SF6_FB, \n" +
      "   MAX(case when sv.STRING_MRID = 653013 then sv.VALUEORIG end) as U3_SF6_FC, \n" +
      "   MAX(case when sv.STRING_MRID = 650018 then sv.VALUEORIG end) as U3_TAP, \n" +
      "   sv.id_actividad as id_actividad \n" +
      "   from stringvalue_enj sv \n" +
      "   where sv.STRING_MRID in (select aa.mrid from 'string' aa where aa.id_sistema = $Sistema) \n" +
      "   GROUP by sv.LOCTIMESTAMP) str \n" +
      "   on str.loctimestamp = Horario.fecha  \n" +
      " INNER JOIN (select av.LOCTIMESTAMP, \n" +
      "    MAX(case when av.ANALOG_MRID = 650066 then av.VALUEORIG end) as AirComp_HMI_1, \n" +
       "   MAX(case when av.ANALOG_MRID = 650067 then av.VALUEORIG end) as AirComp_HMI_2, \n" +
       "   MAX(case when av.ANALOG_MRID = 650068 then av.VALUEORIG end) as AirComp_Acum_1, \n" +
       "   MAX(case when av.ANALOG_MRID = 650069 then av.VALUEORIG end) as AirComp_Acum_2, \n" +
       "   MAX(case when av.ANALOG_MRID = 650070 then av.VALUEORIG end) as AirComp_Acum_3, \n" +
       "   av.id_actividad as id_actividad \n" +
      "   from 'analogvalue_enj' av \n" +
      "    where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "   GROUP by av.LOCTIMESTAMP) ana \n" +
      "   on ana.loctimestamp = Horario.fecha"
   }
   2 -> { //
    qry = ""
   }
   3 -> { // SistemaOleohidraulico - SistemaOleohidraulico
    qry = "SELECT Horario.fecha, Horario.horainicio,  \n" +
      "ana.U1_VE_PresSist, ana.U1_VE_PresNitro, ana.U1_VE_NivAceiteCuba, ana.U1_VE_NivAceiteAcum,  \n" +
      "ana.U1_VE_PresAperVE, ana.U1_VE_PresRetSelloTraba, ana.U1_VE_PresRetSelloMtto, ana.U1_RV_PresSist,  \n" +
      "ana.U1_RV_PresNitro, ana.U1_RV_NivAceiteAcum, ana.U1_RV_TempCubaAceite, ana.U1_RV_PresEntDistri,  \n" +
      "ana.U1_RV_PresFosoTurbi, ana.U1_ApeIny1, ana.U1_ApeIny2, ana.U1_ApeIny3, ana.U1_ApeIny4,  \n" +
      "ana.U1_ApeIny5, ana.U1_ApeIny6, ana.U1_ApeDef1, ana.U1_ApeDef2, ana.U1_ApeDef3, ana.U1_ApeDef4,  \n" +
      "ana.U1_ApeDef5, ana.U1_ApeDef6, ana.U1_Def_PotUni, ana.U1_Freno_PresTanq, ana.U1_Freno_PresSist,  \n" +
      "ana.U2_VE_PresSist, ana.U2_VE_PresNitro, ana.U2_VE_NivAceiteCuba, ana.U2_VE_NivAceiteAcum, ana.U2_VE_PresAperVE,  \n" +
      "ana.U2_VE_PresRetSelloTraba, ana.U2_VE_PresRetSelloMtto, ana.U2_RV_PresSist, ana.U2_RV_PresNitro,  \n" +
      "ana.U2_RV_NivAceiteAcum, ana.U2_RV_TempCubaAceite, ana.U2_RV_PresEntDistri, ana.U2_RV_PresFosoTurbi,  \n" +
      "ana.U2_ApeIny1, ana.U2_ApeIny2, ana.U2_ApeIny3, ana.U2_ApeIny4, ana.U2_ApeIny5, ana.U2_ApeIny6,  \n" +
      "ana.U2_ApeDef1, ana.U2_ApeDef2, ana.U2_ApeDef3, ana.U2_ApeDef4, ana.U2_ApeDef5, ana.U2_ApeDef6,  \n" +
      "ana.U2_Def_PotUni, ana.U2_Freno_PresTanq, ana.U2_Freno_PresSist, ana.U3_VE_PresSist, ana.U3_VE_PresNitro,  \n" +
      "ana.U3_VE_NivAceiteCuba, ana.U3_VE_NivAceiteAcum, ana.U3_VE_PresAperVE, ana.U3_VE_PresRetSelloTraba,  \n" +
      "ana.U3_VE_PresRetSelloMtto, ana.U3_RV_PresSist, ana.U3_RV_PresNitro, ana.U3_RV_NivAceiteAcum,  \n" +
      "ana.U3_RV_TempCubaAceite, ana.U3_RV_PresEntDistri, ana.U3_RV_PresFosoTurbi, ana.U3_ApeIny1,  \n" +
      "ana.U3_ApeIny2, ana.U3_ApeIny3, ana.U3_ApeIny4, ana.U3_ApeIny5, ana.U3_ApeIny6, ana.U3_ApeDef1,  \n" +
      "ana.U3_ApeDef2, ana.U3_ApeDef3, ana.U3_ApeDef4, ana.U3_ApeDef5, ana.U3_ApeDef6, ana.U3_Def_PotUni,  \n" +
      "ana.U3_Freno_PresTanq, ana.U3_Freno_PresSist  \n" +
      "from (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
      "    FROM timevalue_enj b  \n" +
      "    WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
      "    AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema) \n" +
      "    GROUP BY loctimestamp) Horario \n" +
      "INNER JOIN (select av.LOCTIMESTAMP, MAX(case when av.ANALOG_MRID = 651917 then av.VALUEORIG end) as U1_VE_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651918 then av.VALUEORIG end) as U1_VE_PresNitro,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651919 then av.VALUEORIG end) as U1_VE_NivAceiteCuba,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651920 then av.VALUEORIG end) as U1_VE_NivAceiteAcum,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651921 then av.VALUEORIG end) as U1_VE_PresAperVE,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651922 then av.VALUEORIG end) as U1_VE_PresRetSelloTraba,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651923 then av.VALUEORIG end) as U1_VE_PresRetSelloMtto,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651924 then av.VALUEORIG end) as U1_RV_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651925 then av.VALUEORIG end) as U1_RV_PresNitro,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651926 then av.VALUEORIG end) as U1_RV_NivAceiteAcum,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651927 then av.VALUEORIG end) as U1_RV_TempCubaAceite,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651928 then av.VALUEORIG end) as U1_RV_PresEntDistri,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651929 then av.VALUEORIG end) as U1_RV_PresFosoTurbi,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651930 then av.VALUEORIG end) as U1_ApeIny1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651931 then av.VALUEORIG end) as U1_ApeIny2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651932 then av.VALUEORIG end) as U1_ApeIny3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651933 then av.VALUEORIG end) as U1_ApeIny4,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651934 then av.VALUEORIG end) as U1_ApeIny5,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651935 then av.VALUEORIG end) as U1_ApeIny6,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651936 then av.VALUEORIG end) as U1_ApeDef1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651937 then av.VALUEORIG end) as U1_ApeDef2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651938 then av.VALUEORIG end) as U1_ApeDef3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651939 then av.VALUEORIG end) as U1_ApeDef4,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651940 then av.VALUEORIG end) as U1_ApeDef5,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651941 then av.VALUEORIG end) as U1_ApeDef6,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651942 then av.VALUEORIG end) as U1_Def_PotUni,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651943 then av.VALUEORIG end) as U1_Freno_PresTanq,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651944 then av.VALUEORIG end) as U1_Freno_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652917 then av.VALUEORIG end) as U2_VE_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652918 then av.VALUEORIG end) as U2_VE_PresNitro,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652919 then av.VALUEORIG end) as U2_VE_NivAceiteCuba,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652920 then av.VALUEORIG end) as U2_VE_NivAceiteAcum,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652921 then av.VALUEORIG end) as U2_VE_PresAperVE,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652922 then av.VALUEORIG end) as U2_VE_PresRetSelloTraba,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652923 then av.VALUEORIG end) as U2_VE_PresRetSelloMtto,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652924 then av.VALUEORIG end) as U2_RV_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652925 then av.VALUEORIG end) as U2_RV_PresNitro,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652926 then av.VALUEORIG end) as U2_RV_NivAceiteAcum,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652927 then av.VALUEORIG end) as U2_RV_TempCubaAceite,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652928 then av.VALUEORIG end) as U2_RV_PresEntDistri,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652929 then av.VALUEORIG end) as U2_RV_PresFosoTurbi,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652930 then av.VALUEORIG end) as U2_ApeIny1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652931 then av.VALUEORIG end) as U2_ApeIny2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652932 then av.VALUEORIG end) as U2_ApeIny3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652933 then av.VALUEORIG end) as U2_ApeIny4,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652934 then av.VALUEORIG end) as U2_ApeIny5,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652935 then av.VALUEORIG end) as U2_ApeIny6,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652936 then av.VALUEORIG end) as U2_ApeDef1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652937 then av.VALUEORIG end) as U2_ApeDef2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652938 then av.VALUEORIG end) as U2_ApeDef3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652939 then av.VALUEORIG end) as U2_ApeDef4,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652940 then av.VALUEORIG end) as U2_ApeDef5,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652941 then av.VALUEORIG end) as U2_ApeDef6,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652942 then av.VALUEORIG end) as U2_Def_PotUni,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652943 then av.VALUEORIG end) as U2_Freno_PresTanq,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652944 then av.VALUEORIG end) as U2_Freno_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653917 then av.VALUEORIG end) as U3_VE_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653918 then av.VALUEORIG end) as U3_VE_PresNitro,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653919 then av.VALUEORIG end) as U3_VE_NivAceiteCuba,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653920 then av.VALUEORIG end) as U3_VE_NivAceiteAcum,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653921 then av.VALUEORIG end) as U3_VE_PresAperVE,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653922 then av.VALUEORIG end) as U3_VE_PresRetSelloTraba,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653923 then av.VALUEORIG end) as U3_VE_PresRetSelloMtto,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653924 then av.VALUEORIG end) as U3_RV_PresSist,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653925 then av.VALUEORIG end) as U3_RV_PresNitro,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653926 then av.VALUEORIG end) as U3_RV_NivAceiteAcum,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653927 then av.VALUEORIG end) as U3_RV_TempCubaAceite,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653928 then av.VALUEORIG end) as U3_RV_PresEntDistri,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653929 then av.VALUEORIG end) as U3_RV_PresFosoTurbi,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653930 then av.VALUEORIG end) as U3_ApeIny1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653931 then av.VALUEORIG end) as U3_ApeIny2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653932 then av.VALUEORIG end) as U3_ApeIny3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653933 then av.VALUEORIG end) as U3_ApeIny4,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653934 then av.VALUEORIG end) as U3_ApeIny5,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653935 then av.VALUEORIG end) as U3_ApeIny6,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653936 then av.VALUEORIG end) as U3_ApeDef1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653937 then av.VALUEORIG end) as U3_ApeDef2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653938 then av.VALUEORIG end) as U3_ApeDef3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653939 then av.VALUEORIG end) as U3_ApeDef4,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653940 then av.VALUEORIG end) as U3_ApeDef5,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653941 then av.VALUEORIG end) as U3_ApeDef6,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653942 then av.VALUEORIG end) as U3_Def_PotUni,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653943 then av.VALUEORIG end) as U3_Freno_PresTanq,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653944 then av.VALUEORIG end) as U3_Freno_PresSist, \n" +
      "            av.id_actividad as id_actividad \n" +
      "         from 'analogvalue_enj' av \n" +
      "         where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "          GROUP by av.LOCTIMESTAMP) ana  \n" +
      "      on ana.loctimestamp = Horario.fecha    "
   }
   4 -> { // NivelesAceiteCubaCojinetes
    qry = "SELECT Horario.fecha, Horario.horainicio,  \n" +
      "ana.Niv_Aceite_Coj_Emp_U1, ana.Niv_Aceite_Coj_Emp_U2, ana.Niv_Aceite_Coj_Emp_U3, \n" +
      "ana.Niv_Aceite_Coj_Inf_U1, ana.Niv_Aceite_Coj_Inf_U2, ana.Niv_Aceite_Coj_Inf_U3, \n" +
      "ana.Niv_Aceite_Coj_Sup_U1, ana.Niv_Aceite_Coj_Sup_U2, ana.Niv_Aceite_Coj_Sup_U3, \n" +
      "ana.Niv_Aceite_Coj_Turb_U1, ana.Niv_Aceite_Coj_Turb_U2, ana.Niv_Aceite_Coj_Turb_U3 \n" +
      "from (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
      "    FROM timevalue_enj b  \n" +
      "    WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
      "    AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema) \n" +
      "    GROUP BY loctimestamp) Horario \n" +
       "INNER JOIN (select av.LOCTIMESTAMP, MAX(case when av.ANALOG_MRID = 651913 then av.VALUEORIG end) as Niv_Aceite_Coj_Emp_U1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651914 then av.VALUEORIG end) as Niv_Aceite_Coj_Sup_U1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651915 then av.VALUEORIG end) as Niv_Aceite_Coj_Inf_U1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651916 then av.VALUEORIG end) as Niv_Aceite_Coj_Turb_U1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652913 then av.VALUEORIG end) as Niv_Aceite_Coj_Emp_U2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652914 then av.VALUEORIG end) as Niv_Aceite_Coj_Sup_U2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652915 then av.VALUEORIG end) as Niv_Aceite_Coj_Inf_U2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652916 then av.VALUEORIG end) as Niv_Aceite_Coj_Turb_U2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653913 then av.VALUEORIG end) as Niv_Aceite_Coj_Emp_U3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653914 then av.VALUEORIG end) as Niv_Aceite_Coj_Sup_U3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653915 then av.VALUEORIG end) as Niv_Aceite_Coj_Inf_U3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653916 then av.VALUEORIG end) as Niv_Aceite_Coj_Turb_U3, \n" +
      "   av.id_actividad as id_actividad \n" +
      "         from 'analogvalue_enj' av \n" +
      "         where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "         GROUP by av.LOCTIMESTAMP) ana  \n" +
      "     on ana.loctimestamp = Horario.fecha"
   }
   5 -> { // TrafoPrincipal
    qry = "SELECT Horario.fecha, Horario.horainicio,  \n" +
      "ana.U1_FAceite_Bomba1, ana.U1_FAceite_Bomba2, ana.U1_FAceite_Bomba3, \n" +
      "ana.U1_FlujoAguaEntrada, ana.U1_PresAguaEntrada, ana.U1_TemAceiteInterc, ana.U1_TempBarraFA, \n" +
      "ana.U1_TempBarraFB, ana.U1_TempBarraFC, ana.U1_TransTempAceite1, ana.U1_TransTempAceite2, \n" +
      "ana.U1_TransTempDevanado, ana.U2_FAceite_Bomba1, ana.U2_FAceite_Bomba2, ana.U2_FAceite_Bomba3, \n" +
      "ana.U2_FlujoAguaEntrada, ana.U2_PresAguaEntrada, ana.U2_TemAceiteInterc, ana.U2_TempBarraFA, \n" +
      "ana.U2_TempBarraFB, ana.U2_TempBarraFC, ana.U2_TransTempAceite1, ana.U2_TransTempAceite2, \n" +
      "ana.U2_TransTempDevanado, ana.U3_FAceite_Bomba1, ana.U3_FAceite_Bomba2, ana.U3_FAceite_Bomba3, \n" +
      "ana.U3_FlujoAguaEntrada, ana.U3_PresAguaEntrada, ana.U3_TemAceiteInterc, ana.U3_TempBarraFA, \n" +
      "ana.U3_TempBarraFB, ana.U3_TempBarraFC, ana.U3_TransTempAceite1, ana.U3_TransTempAceite2, \n" +
      "ana.U3_TransTempDevanado, str.U1_BombaInter, str.U1_TransNivelAceite, str.U2_BombaInter, \n" +
      "str.U2_TransNivelAceite, str.U3_BombaInter, str.U3_TransNivelAceite \n" +
      "from (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
      "    FROM timevalue_enj b  \n" +
      "    WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
      "    AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema) \n" +
      "    GROUP BY loctimestamp) Horario \n" +
      "INNER JOIN (select sv.LOCTIMESTAMP, MAX(case when sv.STRING_MRID = 650501 then sv.VALUEORIG end) as U1_BombaInter,  \n" +
      "            MAX(case when sv.STRING_MRID = 650502 then sv.VALUEORIG end) as U2_BombaInter,  \n" +
      "            MAX(case when sv.STRING_MRID = 650503 then sv.VALUEORIG end) as U3_BombaInter,  \n" +
      "            MAX(case when sv.STRING_MRID = 651001 then sv.VALUEORIG end) as U1_TransNivelAceite,  \n" +
      "            MAX(case when sv.STRING_MRID = 652001 then sv.VALUEORIG end) as U2_TransNivelAceite,  \n" +
      "            MAX(case when sv.STRING_MRID = 653001 then sv.VALUEORIG end) as U3_TransNivelAceite, \n" +
      "   sv.id_actividad as id_actividad \n" +
      "     from stringvalue_enj sv \n" +
      "     where sv.STRING_MRID in (select aa.mrid from 'string' aa where aa.id_sistema = $Sistema) \n" +
      "     GROUP by sv.LOCTIMESTAMP) str  \n" +
      "     on str.loctimestamp = Horario.fecha  \n" +
      "INNER JOIN (select av.LOCTIMESTAMP,  MAX(case when av.ANALOG_MRID = 651901 then av.VALUEORIG end) as U1_FlujoAguaEntrada,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651902 then av.VALUEORIG end) as U1_PresAguaEntrada,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651903 then av.VALUEORIG end) as U1_TemAceiteInterc,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651904 then av.VALUEORIG end) as U1_FAceite_Bomba1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651905 then av.VALUEORIG end) as U1_FAceite_Bomba2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651906 then av.VALUEORIG end) as U1_FAceite_Bomba3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651907 then av.VALUEORIG end) as U1_TempBarraFA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651908 then av.VALUEORIG end) as U1_TempBarraFB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651909 then av.VALUEORIG end) as U1_TempBarraFC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651910 then av.VALUEORIG end) as U1_TransTempAceite1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651911 then av.VALUEORIG end) as U1_TransTempAceite2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 651912 then av.VALUEORIG end) as U1_TransTempDevanado,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652901 then av.VALUEORIG end) as U2_FlujoAguaEntrada,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652902 then av.VALUEORIG end) as U2_PresAguaEntrada,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652903 then av.VALUEORIG end) as U2_TemAceiteInterc,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652904 then av.VALUEORIG end) as U2_FAceite_Bomba1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652905 then av.VALUEORIG end) as U2_FAceite_Bomba2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652906 then av.VALUEORIG end) as U2_FAceite_Bomba3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652907 then av.VALUEORIG end) as U2_TempBarraFA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652908 then av.VALUEORIG end) as U2_TempBarraFB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652909 then av.VALUEORIG end) as U2_TempBarraFC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652910 then av.VALUEORIG end) as U2_TransTempAceite1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652911 then av.VALUEORIG end) as U2_TransTempAceite2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 652912 then av.VALUEORIG end) as U2_TransTempDevanado,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653901 then av.VALUEORIG end) as U3_FlujoAguaEntrada,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653902 then av.VALUEORIG end) as U3_PresAguaEntrada,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653903 then av.VALUEORIG end) as U3_TemAceiteInterc,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653904 then av.VALUEORIG end) as U3_FAceite_Bomba1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653905 then av.VALUEORIG end) as U3_FAceite_Bomba2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653906 then av.VALUEORIG end) as U3_FAceite_Bomba3,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653907 then av.VALUEORIG end) as U3_TempBarraFA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653908 then av.VALUEORIG end) as U3_TempBarraFB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653909 then av.VALUEORIG end) as U3_TempBarraFC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653910 then av.VALUEORIG end) as U3_TransTempAceite1,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653911 then av.VALUEORIG end) as U3_TransTempAceite2,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653912 then av.VALUEORIG end) as U3_TransTempDevanado,  \n" +
      "   av.id_actividad as id_actividad \n" +
      "         from 'analogvalue_enj' av \n" +
      "         where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "         GROUP by av.LOCTIMESTAMP) ana  \n" +
      "     on ana.loctimestamp = Horario.fecha "
   }
   6 -> { // TransformadoresSSAA
    qry = "SELECT Horario.fecha, Horario.horainicio,  \n" +
      "ana.TE_1_FA, ana.TE_1_FB, ana.TE_1_FC, ana.TE_2_FA, ana.TE_2_FB, ana.TE_2_FC, ana.TE_3_FA, ana.TE_3_FB, ana.TE_3_FC, \n" +
      "ana.TSA_1_FA, ana.TSA_1_FB, ana.TSA_1_FC, ana.TSA_2_FA, ana.TSA_2_FB, ana.TSA_2_FC, ana.TSA_3_FA, ana.TSA_3_FB, ana.TSA_3_FC, \n" +
      "ana.TSA_4_FA, ana.TSA_4_FB, ana.TSA_4_FC \n" +
      "from  (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
            "    FROM timevalue_enj b  \n" +
            "    WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
            "    AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema) \n" +
            "    GROUP BY loctimestamp) Horario \n" +
      "INNER JOIN (select av.LOCTIMESTAMP, MAX(case when av.ANALOG_MRID = 650045 then av.VALUEORIG end) as TSA_1_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650046 then av.VALUEORIG end) as TSA_1_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650047 then av.VALUEORIG end) as TSA_1_FC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650048 then av.VALUEORIG end) as TSA_2_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650049 then av.VALUEORIG end) as TSA_2_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650050 then av.VALUEORIG end) as TSA_2_FC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650051 then av.VALUEORIG end) as TSA_3_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650052 then av.VALUEORIG end) as TSA_3_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650053 then av.VALUEORIG end) as TSA_3_FC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650054 then av.VALUEORIG end) as TSA_4_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650055 then av.VALUEORIG end) as TSA_4_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650056 then av.VALUEORIG end) as TSA_4_FC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650057 then av.VALUEORIG end) as TE_1_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650058 then av.VALUEORIG end) as TE_1_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650059 then av.VALUEORIG end) as TE_1_FC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650060 then av.VALUEORIG end) as TE_2_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650061 then av.VALUEORIG end) as TE_2_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650062 then av.VALUEORIG end) as TE_2_FC,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650063 then av.VALUEORIG end) as TE_3_FA,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650064 then av.VALUEORIG end) as TE_3_FB,  \n" +
      "            MAX(case when av.ANALOG_MRID = 650065 then av.VALUEORIG end) as TE_3_FC,  \n" +
      "   av.id_actividad as id_actividad \n" +
      "         from 'analogvalue_enj' av \n" +
      "         where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "         GROUP by av.LOCTIMESTAMP) ana  \n" +
      "     on ana.loctimestamp = Horario.fecha "
   }
   7 -> { // Mag.Enfri.Unidades
    qry = "SELECT Horario.fecha, Horario.horainicio, ana.Dis_Ent_U01, ana.Dis_Ent_U02, ana.Dis_Ent_U03, \n" +
      "ana.Dis_Sal_U01, ana.Dis_Sal_U02, ana.Dis_Sal_U03, ana.F_A_Ent_U01, ana.F_A_Ent_U02, ana.F_A_Ent_U03, \n" +
      "ana.F_A_S_E_U01, ana.F_A_S_E_U02, ana.F_A_S_E_U03, ana.F_A_S_S_U01, ana.F_A_S_S_U02, ana.F_A_S_S_U03, \n" +
      "ana.F_A_Sal_U01, ana.F_A_Sal_U02, ana.F_A_Sal_U03, ana.Flu_Agu_Ent_01, ana.Flu_Agu_Ent_02, ana.Flu_Agu_Ent_03, \n" +
      "ana.Flu_Agu_Ent_U01, ana.Flu_Agu_Ent_U02, ana.Flu_Agu_Ent_U03, ana.Pre_Ent_01, ana.Pre_Ent_02, ana.Pre_Ent_03, \n" +
      "ana.Pres_Agu_Ra_U01, ana.Pres_Agu_Ra_U02, ana.Pres_Agu_Ra_U03, ana.Rad_Pre_Ent_A_U01, ana.Rad_Pre_Ent_A_U02, ana.Rad_Pre_Ent_A_U03, \n" +
      "ana.V_R_P_Ent_U01, ana.V_R_P_Ent_U02, ana.V_R_P_Ent_U03, ana.V_R_P_Sal_U01, ana.V_R_P_Sal_U02, ana.V_R_P_Sal_U03, \n" +
      "str.Bomba_U01, str.Bomba_U02, str.Bomba_U03, str.Circu_Agua_U01, str.Circu_Agua_U02, str.Circu_Agua_U03 \n" +
      "from (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
      "    FROM timevalue_enj b  \n" +
      "    WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
      "    AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema) \n" +
      "    GROUP BY loctimestamp) Horario \n" +
      "INNER JOIN (select sv.LOCTIMESTAMP,  MAX(case when sv.STRING_MRID = 650034 then sv.VALUEORIG end) as Circu_Agua_U01,  \n" +
      "            MAX(case when sv.STRING_MRID = 650038 then sv.VALUEORIG end) as Bomba_U01,  \n" +
      "            MAX(case when sv.STRING_MRID = 650059 then sv.VALUEORIG end) as Circu_Agua_U02,  \n" +
      "            MAX(case when sv.STRING_MRID = 650060 then sv.VALUEORIG end) as Bomba_U02,  \n" +
      "            MAX(case when sv.STRING_MRID = 650066 then sv.VALUEORIG end) as Circu_Agua_U03,  \n" +
      "            MAX(case when sv.STRING_MRID = 650067 then sv.VALUEORIG end) as Bomba_U03,  \n" +
      "   sv.id_actividad as id_actividad \n" +
      "     from stringvalue_enj sv \n" +
      "     where sv.STRING_MRID in (select aa.mrid from 'string' aa where aa.id_sistema = $Sistema) \n" +
      "     GROUP by sv.LOCTIMESTAMP) str  \n" +
      "     on str.loctimestamp = Horario.fecha \n" +
      "INNER JOIN (select av.LOCTIMESTAMP,  MAX(case when av.ANALOG_MRID = 653302 then av.VALUEORIG end) as Pres_Agu_Ra_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653303 then av.VALUEORIG end) as Flu_Agu_Ent_01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653304 then av.VALUEORIG end) as Pre_Ent_01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653305 then av.VALUEORIG end) as V_R_P_Ent_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653306 then av.VALUEORIG end) as V_R_P_Sal_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653307 then av.VALUEORIG end) as F_A_Ent_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653308 then av.VALUEORIG end) as F_A_Sal_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653309 then av.VALUEORIG end) as Dis_Ent_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653310 then av.VALUEORIG end) as Dis_Sal_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653311 then av.VALUEORIG end) as F_A_S_E_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653312 then av.VALUEORIG end) as F_A_S_S_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653313 then av.VALUEORIG end) as Rad_Pre_Ent_A_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653314 then av.VALUEORIG end) as Flu_Agu_Ent_U01,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653315 then av.VALUEORIG end) as Pres_Agu_Ra_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653316 then av.VALUEORIG end) as Flu_Agu_Ent_02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653317 then av.VALUEORIG end) as Pre_Ent_02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653318 then av.VALUEORIG end) as V_R_P_Ent_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653319 then av.VALUEORIG end) as V_R_P_Sal_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653320 then av.VALUEORIG end) as F_A_Ent_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653321 then av.VALUEORIG end) as F_A_Sal_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653322 then av.VALUEORIG end) as Dis_Ent_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653323 then av.VALUEORIG end) as Dis_Sal_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653324 then av.VALUEORIG end) as F_A_S_E_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653325 then av.VALUEORIG end) as F_A_S_S_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653326 then av.VALUEORIG end) as Rad_Pre_Ent_A_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653327 then av.VALUEORIG end) as Flu_Agu_Ent_U02,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653328 then av.VALUEORIG end) as Pres_Agu_Ra_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653329 then av.VALUEORIG end) as Flu_Agu_Ent_03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653330 then av.VALUEORIG end) as Pre_Ent_03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653331 then av.VALUEORIG end) as V_R_P_Ent_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653332 then av.VALUEORIG end) as V_R_P_Sal_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653333 then av.VALUEORIG end) as F_A_Ent_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653334 then av.VALUEORIG end) as F_A_Sal_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653335 then av.VALUEORIG end) as Dis_Ent_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653336 then av.VALUEORIG end) as Dis_Sal_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653337 then av.VALUEORIG end) as F_A_S_E_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653338 then av.VALUEORIG end) as F_A_S_S_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653339 then av.VALUEORIG end) as Rad_Pre_Ent_A_U03,  \n" +
      "            MAX(case when av.ANALOG_MRID = 653340 then av.VALUEORIG end) as Flu_Agu_Ent_U03, \n" +
      "   av.id_actividad as id_actividad \n" +
      "         from 'analogvalue_enj' av \n" +
      "         where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "         GROUP by av.LOCTIMESTAMP) ana  \n" +
      "     on ana.loctimestamp = Horario.fecha  \n"
   }
   9 -> { // Temp_Pres_Flu_Inter_Calor
    qry = "SELECT Horario.fecha, Horario.horainicio, \n" +
      "   ana.Coj_Emp_Temp_Ent_01, ana.Coj_GTur_Flu_Ag_03, ana.Coj_Emp_Temp_Sal_01, ana.Coj_Emp_Temp_M1_01, ana.Coj_Emp_Temp_M2_01, \n" +
      "   ana.Coj_Emp_Flu_Ag_01, ana.Coj_GSup_Temp_Ent_01, ana.Coj_GSup_Temp_Sal_01, ana.Coj_GSup_Temp_M1_01, ana.Coj_GSup_Temp_M2_01, \n" +
      "   ana.Coj_GSup_Flu_Ag_01, ana.Coj_GInf_Temp_Ent_01, ana.Coj_GInf_Temp_Sal_01, ana.Coj_GInf_Temp_M1_01, ana.Coj_GInf_Temp_M2_01, \n" +
      "   ana.Coj_GInf_Flu_Ag_01, ana.Coj_GTur_Temp_Ent_01, ana.Coj_GTur_Temp_Sal_01, ana.Coj_GTur_Temp_M1_01, ana.Coj_GTur_Temp_M2_01, \n" +
      "   ana.Coj_GTur_Flu_Ag_01, ana.Coj_Emp_Temp_Ent_02, ana.Coj_Emp_Temp_Sal_02, ana.Coj_Emp_Temp_M1_02, ana.Coj_Emp_Temp_M2_02, \n" +
      "   ana.Coj_Emp_Flu_Ag_02, ana.Coj_GSup_Temp_Ent_02, ana.Coj_GSup_Temp_Sal_02, ana.Coj_GSup_Temp_M1_02, ana.Coj_GSup_Temp_M2_02, \n" +
      "   ana.Coj_GSup_Flu_Ag_02, ana.Coj_GInf_Temp_Ent_02, ana.Coj_GInf_Temp_Sal_02, ana.Coj_GInf_Temp_M1_02, ana.Coj_GInf_Temp_M2_02, \n" +
      "   ana.Coj_GInf_Flu_Ag_02, ana.Coj_GTur_Temp_Ent_02, ana.Coj_GTur_Temp_Sal_02, ana.Coj_GTur_Temp_M1_02, ana.Coj_GTur_Temp_M2_02, \n" +
      "   ana.Coj_GTur_Flu_Ag_02, ana.Coj_Emp_Temp_Ent_03, ana.Coj_Emp_Temp_Sal_03, ana.Coj_Emp_Temp_M1_03, ana.Coj_Emp_Temp_M2_03, \n" +
      "   ana.Coj_Emp_Flu_Ag_03, ana.Coj_GSup_Temp_Ent_03, ana.Coj_GSup_Temp_Sal_03, ana.Coj_GSup_Temp_M1_03, ana.Coj_GSup_Temp_M2_03, \n" +
      "   ana.Coj_GSup_Flu_Ag_03, ana.Coj_GInf_Temp_Ent_03, ana.Coj_GInf_Temp_Sal_03, ana.Coj_GInf_Temp_M1_03, ana.Coj_GInf_Temp_M2_03, \n" +
      "   ana.Coj_GInf_Flu_Ag_03, ana.Coj_GTur_Temp_Ent_03, ana.Coj_GTur_Temp_Sal_03, ana.Coj_GTur_Temp_M1_03, ana.Coj_GTur_Temp_M2_03 \n" +
      "   from (SELECT MAX(case when b.time_mRID=(select mrid from time where id_sistema = $Sistema and unidades = 'H') then strftime('%H:%M', time(b.VALUEORIG)) else strftime('%H:%M', time(b.locTimeStamp))END) AS HoraInicio, b.locTimeStamp AS Fecha \n" +
      "    FROM timevalue_enj b  \n" +
      "    WHERE  LOCTIMESTAMP BETWEEN datetime('now', '-$periodo hours') and datetime('now') \n" +
      "    AND b.time_mRID IN (select mrid from time where id_sistema = $Sistema) \n" +
      "    GROUP BY loctimestamp) Horario \n" +
      "  INNER JOIN (select av.LOCTIMESTAMP, MAX(case when av.ANALOG_MRID = 650003 then av.VALUEORIG end) as Coj_Emp_Temp_Ent_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650004 then av.VALUEORIG end) as Coj_Emp_Temp_Sal_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650005 then av.VALUEORIG end) as Coj_Emp_Temp_M1_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650006 then av.VALUEORIG end) as Coj_Emp_Temp_M2_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650007 then av.VALUEORIG end) as Coj_Emp_Flu_Ag_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650008 then av.VALUEORIG end) as Coj_GSup_Temp_Ent_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650009 then av.VALUEORIG end) as Coj_GSup_Temp_Sal_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650010 then av.VALUEORIG end) as Coj_GSup_Temp_M1_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650011 then av.VALUEORIG end) as Coj_GSup_Temp_M2_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650012 then av.VALUEORIG end) as Coj_GSup_Flu_Ag_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650016 then av.VALUEORIG end) as Coj_GInf_Temp_Ent_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650017 then av.VALUEORIG end) as Coj_GInf_Temp_Sal_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650018 then av.VALUEORIG end) as Coj_GInf_Temp_M1_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650019 then av.VALUEORIG end) as Coj_GInf_Temp_M2_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650021 then av.VALUEORIG end) as Coj_GInf_Flu_Ag_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650022 then av.VALUEORIG end) as Coj_GTur_Temp_Ent_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650023 then av.VALUEORIG end) as Coj_GTur_Temp_Sal_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650024 then av.VALUEORIG end) as Coj_GTur_Temp_M1_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650025 then av.VALUEORIG end) as Coj_GTur_Temp_M2_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650026 then av.VALUEORIG end) as Coj_GTur_Flu_Ag_01,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650027 then av.VALUEORIG end) as Coj_Emp_Temp_Ent_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650028 then av.VALUEORIG end) as Coj_Emp_Temp_Sal_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650029 then av.VALUEORIG end) as Coj_Emp_Temp_M1_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650030 then av.VALUEORIG end) as Coj_Emp_Temp_M2_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650031 then av.VALUEORIG end) as Coj_Emp_Flu_Ag_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650033 then av.VALUEORIG end) as Coj_GSup_Temp_Ent_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650035 then av.VALUEORIG end) as Coj_GSup_Temp_Sal_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650036 then av.VALUEORIG end) as Coj_GSup_Temp_M1_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650037 then av.VALUEORIG end) as Coj_GSup_Temp_M2_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650041 then av.VALUEORIG end) as Coj_GSup_Flu_Ag_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650042 then av.VALUEORIG end) as Coj_GInf_Temp_Ent_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650043 then av.VALUEORIG end) as Coj_GInf_Temp_Sal_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650071 then av.VALUEORIG end) as Coj_GInf_Temp_M1_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650072 then av.VALUEORIG end) as Coj_GInf_Temp_M2_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650073 then av.VALUEORIG end) as Coj_GInf_Flu_Ag_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650074 then av.VALUEORIG end) as Coj_GTur_Temp_Ent_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650075 then av.VALUEORIG end) as Coj_GTur_Temp_Sal_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650076 then av.VALUEORIG end) as Coj_GTur_Temp_M1_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650077 then av.VALUEORIG end) as Coj_GTur_Temp_M2_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650078 then av.VALUEORIG end) as Coj_GTur_Flu_Ag_02,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650079 then av.VALUEORIG end) as Coj_Emp_Temp_Ent_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650080 then av.VALUEORIG end) as Coj_Emp_Temp_Sal_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650081 then av.VALUEORIG end) as Coj_Emp_Temp_M1_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650082 then av.VALUEORIG end) as Coj_Emp_Temp_M2_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650083 then av.VALUEORIG end) as Coj_Emp_Flu_Ag_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650084 then av.VALUEORIG end) as Coj_GSup_Temp_Ent_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650085 then av.VALUEORIG end) as Coj_GSup_Temp_Sal_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650086 then av.VALUEORIG end) as Coj_GSup_Temp_M1_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650087 then av.VALUEORIG end) as Coj_GSup_Temp_M2_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650088 then av.VALUEORIG end) as Coj_GSup_Flu_Ag_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650089 then av.VALUEORIG end) as Coj_GInf_Temp_Ent_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650090 then av.VALUEORIG end) as Coj_GInf_Temp_Sal_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650091 then av.VALUEORIG end) as Coj_GInf_Temp_M1_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650092 then av.VALUEORIG end) as Coj_GInf_Temp_M2_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650093 then av.VALUEORIG end) as Coj_GInf_Flu_Ag_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650094 then av.VALUEORIG end) as Coj_GTur_Temp_Ent_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650095 then av.VALUEORIG end) as Coj_GTur_Temp_Sal_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650096 then av.VALUEORIG end) as Coj_GTur_Temp_M1_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650097 then av.VALUEORIG end) as Coj_GTur_Temp_M2_03,  \n" +
      "                MAX(case when av.ANALOG_MRID = 650098 then av.VALUEORIG end) as Coj_GTur_Flu_Ag_03, \n" +
      "   av.id_actividad as id_actividad \n" +
      "   from 'analogvalue_enj' av \n" +
      "    where av.ANALOG_MRID in (select aa.mrid from 'analog' aa where aa.id_sistema = $Sistema) \n" +
      "   GROUP by av.LOCTIMESTAMP) ana \n" +
      "   on ana.loctimestamp = Horario.fecha"
   }
   else -> {
    qry = ""
   }
  }

  Log.d("---------- Query ", qry)
  val cursor : Cursor
  val db : SQLiteDatabase

  //openDatabase()
  db = mDatabase!!

  cursor = db.rawQuery(qry, null)
  //Log.d("Query", qry)
  //closeDatabase()
  return  cursor
 }

 fun copyDatabase(context: Context): Boolean {
  return try {
   val outFileName: String = dbPath + nameDatabase

   // copiando BD
   Log.d("######### dbPath", dbPath)
   Log.d("######### nameDatabase", nameDatabase)

   val `is` = context.assets.open(nameDatabase)
   val os = FileOutputStream(outFileName)

   val buffer = ByteArray(1024)
   while (`is`.read(buffer) > 0) {
    os.write(buffer)
    //Log.d("#DB", "writing>>")
   }

   os.flush()
   os.close()
   `is`.close()

   //Log.d("MainActivity", "DB copied")
   true
  } catch (e: java.lang.Exception) {
   //Log.d("MainActivity", "DB not copied")
   e.printStackTrace()
   false
  }
 }

 fun actualizaAnalog(elanalog : analog) : Long{
  var actualizado : Long = 0

  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("name", elanalog.Name)
   updateValue.put("aliasname", elanalog.AliasName)
   updateValue.put("pathname", elanalog.PathName)
   updateValue.put("description", elanalog.Description)
   updateValue.put("id_estado", elanalog.IdEstado)
   updateValue.put("id_sistema", elanalog.IdSistema)
   updateValue.put("id_tipo", elanalog.IdTipo)
   updateValue.put("maxvalue", elanalog.MaxValue)
   updateValue.put("minvalue", elanalog.MinValue)
   updateValue.put("unidades", elanalog.Unidades)
   updateValue.put("orden_medicion", elanalog.OrdenMedicion)
   updateValue.put("id_nfc", elanalog.IdNFC)
   updateValue.put("localname", elanalog.LocalName)
   updateValue.put("UPDATE_TIMESTAMP", elanalog.UPDATE_TIMESTAMP)
   updateValue.put("UPDATE_USER", elanalog.UPDATE_USER)
   actualizado = mDatabase?.update(
    Tabla_Analog,
    updateValue,
    "mrid = ?",
    arrayOf(elanalog.MRID.toString())
   )!!.toLong()
  }
  //closeDatabase()
  return actualizado
 }

 fun insertarAnalog(elanalog : analog) : Long{
  var insertado : Long = 0

  try {

   //var lafechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertAnalog = ContentValues()
   insertAnalog.put("mrid", elanalog.MRID)
   insertAnalog.put("name", elanalog.Name)
   insertAnalog.put("aliasname", elanalog.AliasName)
   insertAnalog.put("pathname", elanalog.PathName)
   insertAnalog.put("description", elanalog.Description)
   insertAnalog.put("id_estado", elanalog.IdEstado)
   insertAnalog.put("id_sistema", elanalog.IdSistema)
   insertAnalog.put("id_tipo", elanalog.IdTipo)
   insertAnalog.put("maxvalue", elanalog.MaxValue)
   insertAnalog.put("minvalue", elanalog.MinValue)
   insertAnalog.put("unidades", elanalog.Unidades)
   insertAnalog.put("orden_medicion", elanalog.OrdenMedicion)
   insertAnalog.put("id_nfc", elanalog.IdNFC)
   insertAnalog.put("localname", elanalog.LocalName)
   insertAnalog.put("insert_timestamp", elanalog.InsertTimestamp)
   insertAnalog.put("insert_user", elanalog.InsertUser)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_Analog, null, insertAnalog)!!
   }
   //closeDatabase()
  }catch (e: Exception){
   //Log.w("Error inserción StringValue", e.toString())
  }
  return insertado
 }

 fun actualizaString(elstring : string) : Long{
  var actualizado : Long = 0

  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("name", elstring.Name)
   updateValue.put("aliasname", elstring.AliasName)
   updateValue.put("pathname", elstring.PathName)
   updateValue.put("description", elstring.Description)
   updateValue.put("id_estado", elstring.IdEstado)
   updateValue.put("id_sistema", elstring.IdSistema)
   updateValue.put("id_tipo", elstring.IdTipo)
   updateValue.put("selcbo", elstring.SELCBO)
   updateValue.put("id_nfc", elstring.IdNFC)
   updateValue.put("unidades", elstring.Unidades)
   updateValue.put("localname", elstring.LocalName)
   updateValue.put("UPDATE_TIMESTAMP", elstring.UPDATE_TIMESTAMP)
   updateValue.put("UPDATE_USER", elstring.UPDATE_USER)

   actualizado = mDatabase?.update(
    Tabla_String,
    updateValue,
    "id = ?",
    arrayOf(elstring.MRID.toString())
   )!!.toLong()
  }
  //closeDatabase()

  return actualizado
 }

 fun insertarString(elstring : string) : Long{
  var insertado : Long = 0

  try {
   //var lafechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertString = ContentValues()
   insertString.put("mrid", elstring.MRID)
   insertString.put("name", elstring.Name)
   insertString.put("aliasname", elstring.AliasName)
   insertString.put("pathname", elstring.PathName)
   insertString.put("description", elstring.Description)
   insertString.put("id_estado", elstring.IdEstado)
   insertString.put("id_sistema", elstring.IdSistema)
   insertString.put("id_tipo", elstring.IdTipo)
   insertString.put("selcbo", elstring.SELCBO)
   insertString.put("unidades", elstring.Unidades)
   insertString.put("orden_medicion", elstring.OrdenMedicion)
   insertString.put("id_nfc", elstring.IdNFC)
   insertString.put("localname", elstring.LocalName)
   insertString.put("insert_timestamp", elstring.InsertTimestamp)
   insertString.put("insert_user", elstring.InsertUser)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_String, null, insertString)!!
   }
   //closeDatabase()
  }catch (e: Exception){
   //Log.w("Error inserción StringValue", e.toString())
  }
  return insertado
 }

 fun actualizaTime(eltime : time) : Long{
  var actualizado : Long = 0

  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("name", eltime.Name)
   updateValue.put("aliasname", eltime.AliasName)
   updateValue.put("pathname", eltime.PathName)
   updateValue.put("description", eltime.Description)
   updateValue.put("id_estado", eltime.IdEstado)
   updateValue.put("id_sistema", eltime.IdSistema)
   updateValue.put("id_tipo", eltime.IdTipo)
   updateValue.put("unidades", eltime.Unidades)
   updateValue.put("localname", eltime.LocalName)
   updateValue.put("UPDATE_TIMESTAMP", eltime.UpdateTimestamp)
   updateValue.put("UPDATE_USER", eltime.UpdateUser)
   actualizado = mDatabase?.update(
    Tabla_Time,
    updateValue,
    "mrid = ?",
    arrayOf(eltime.MRID.toString())
   )!!.toLong()
  }
  //closeDatabase()
  return actualizado
 }

 fun insertarTime(eltime : time) : Long{
  var insertado : Long = 0

  try {
   //var lafechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertTime = ContentValues()
   insertTime.put("mrid", eltime.MRID)
   insertTime.put("name", eltime.Name)
   insertTime.put("aliasname", eltime.AliasName)
   insertTime.put("pathname", eltime.PathName)
   insertTime.put("description", eltime.Description)
   insertTime.put("id_estado", eltime.IdEstado)
   insertTime.put("id_sistema", eltime.IdSistema)
   insertTime.put("id_tipo", eltime.IdTipo)
   insertTime.put("unidades", eltime.Unidades)
   insertTime.put("orden_medicion", eltime.OrdenMedicion)
   insertTime.put("localname", eltime.LocalName)
   insertTime.put("insert_timestamp", eltime.InsertTimestamp)
   insertTime.put("insert_user", eltime.InsertUser)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_Time, null, insertTime)!!
   }
   //closeDatabase()
  }catch (e: Exception){
   //Log.w("Error inserción StringValue", e.toString())
  }

  return insertado
 }

 fun getTime(mrid : String) : time{
  val eltime = time()
  val qry = "select * from $Tabla_Time where time_mrid = $mrid "
  val cursor : Cursor
  val db : SQLiteDatabase

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     eltime.MRID = cursor.getInt(cursor.getColumnIndexOrThrow( "mrid"))
     //elTime.Name = cursor.getInt(2)

     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //closeDatabase()
  }

  return eltime
 }

 fun actualizaActividad(laactividad : actividades) : Long{
  var actualizado : Long = 0

  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("fecha", laactividad.fecha)
   updateValue.put("horainicio", laactividad.horainicio)
   updateValue.put("horafin", laactividad.horafin)
   updateValue.put("id_sistema", laactividad.id_sistema)
   updateValue.put("ipt", laactividad.idplanificacion_ipt)
   updateValue.put("operador", laactividad.operador)
   updateValue.put("idobservaciones", laactividad.idobservaciones)
   updateValue.put("observacion", laactividad.observacion)
   actualizado = mDatabase?.update(
    Tabla_Actividades,
    updateValue,
    "id = ?",
    arrayOf(laactividad.id.toString())
   )!!.toLong()
  }
  //closeDatabase()
  return actualizado
 }

 fun insertarActividad(laactividad : actividades) : Long{
  var insertado : Long = 0

  try {

   //var lafechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertActividad = ContentValues()
   insertActividad.put("id", laactividad.id)
   insertActividad.put("fecha", laactividad.fecha)
   insertActividad.put("horainicio", laactividad.horainicio)
   insertActividad.put("horafin", laactividad.horafin)
   insertActividad.put("observacion", laactividad.observacion)
   insertActividad.put("idobservaciones", laactividad.idobservaciones)
   insertActividad.put("ipt", laactividad.idplanificacion_ipt)
   insertActividad.put("id_sistema", laactividad.id_sistema)
   insertActividad.put("operador", laactividad.operador)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_Actividades, null, insertActividad)!!
   }
   //closeDatabase()
  }catch (e: Exception){
   //Log.w("Error inserción StringValue", e.toString())
  }

  return insertado
 }

 fun getActividadId(id : Int) : actividades{
  val lactividad = actividades()
  val qry = "select * from $Tabla_Actividades where id = $id "
  val cursor : Cursor
  val db : SQLiteDatabase

  if(openDatabase()){
   db = mDatabase!!
   cursor = db.rawQuery(qry, null)
   //if (cursor != null) {
    if (cursor.count == 0) {
     //Toast.makeText(mCtx, "Sin registros almacenados", Toast.LENGTH_SHORT).show()
    }
    else{
     cursor.moveToFirst()
     lactividad.id = cursor.getInt(cursor.getColumnIndexOrThrow( "id"))
     //elTime.Name = cursor.getInt(2)

     ////Log.d("#DB - Registros: ", cursor.count.toString())
     //Toast.makeText(mCtx,"${cursor.count.toString()} Registros encontrados", Toast.LENGTH_SHORT).show()
    }
    cursor.close()
   //}
   //closeDatabase()
  }
  return lactividad
 }

 fun getActividadesIPT(fecha : String, hora : String, idNFC : String) : Cursor{
  //val lasctividades = ArrayList<actividades>()
  val qry = "SELECT act.id as act_id, str.MRID, str.ALIASNAME, act.fecha, act.horainicio, act.horafin, " +
          "act.operador, act.diainicio, act.diafinal, act.idusuario, plnipt.id_periodo, periodo_ipt.nombre, " +
          "plnipt.semana, str.id_sistema, IFNULL(str.id_estado,1) as estado " +
          "FROM actividades act " +
          "INNER JOIN planificacion_ipt plnipt " +
          "ON act.idplanificacion_ipt = plnipt.id " +
          "INNER JOIN string str " +
          "ON plnipt.id_descripcion_ipt = str.MRID " +
          "INNER JOIN periodo_ipt " +
          "ON periodo_ipt.id = plnipt.id_periodo " +
          "WHERE act.idplanificacion_ipt IS NOT NULL " +
          "AND '" + fecha + "' BETWEEN act.diainicio AND act.diafinal " +
          "AND '" + hora + "' BETWEEN act.horainicio AND act.horafin "
  val cursor : Cursor

  val db : SQLiteDatabase = mDatabase!!
  cursor = db.rawQuery(qry, null)

  return cursor
 }

 fun getHistActividadesIPT(mrid : String) : Cursor{
  //val lasctividades = ArrayList<actividades>()
  val qry = "SELECT * FROM stringvalue_enj " +
             "ORDER BY INSERT_TIMESTAMP DESC " +
             "LIMIT 10"

  // "--WHERE STRING_MRID = $mrid  " +

  val cursor : Cursor

  val db : SQLiteDatabase = mDatabase!!
  cursor = db.rawQuery(qry, null)

  return cursor
 }

 fun actualizaUsuario(elusuario: usuario) : Long{
  var actualizado : Long = 0
  val updateValue = ContentValues()
  if(openDatabase()){
   updateValue.put("correo", elusuario.correo)
   updateValue.put("id_central", elusuario.idCentral)
   updateValue.put("id_perfil", elusuario.idPerfil)
   updateValue.put("password", elusuario.password)
   updateValue.put("activado", elusuario.activado)
   actualizado = mDatabase?.update(
    Tabla_Usuario,
    updateValue,
    "usuario = ?",
    arrayOf(elusuario.usuario)
   )!!.toLong()
  }
  //closeDatabase()
  return actualizado
 }

 fun insertarUsuario(elusuario : usuario) : Long{
  var insertado : Long = 0

  try {
   //var lafechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
   //var fecha = Date()
   val insertUsuario = ContentValues()
   insertUsuario.put("usuario", elusuario.usuario)
   insertUsuario.put("correo", elusuario.correo)
   insertUsuario.put("password", elusuario.password)
   insertUsuario.put("id_perfil", elusuario.idPerfil)
   insertUsuario.put("id_central", elusuario.idCentral)
   insertUsuario.put("activado", elusuario.activado)
   if(openDatabase()){
    insertado= mDatabase?.insert(Tabla_Usuario, null, insertUsuario)!!
   }
   //closeDatabase()
  }catch (e: Exception){
   //Log.w("Error inserción StringValue", e.toString())
  }
  return insertado
 }

 fun verificaDB(contexto : Context) : Boolean {
  var resultado = true
  val database : File = contexto.getDatabasePath(nameDatabase)
  if (!database.exists()){
   this.readableDatabase
   if(copyDatabase(contexto)){
    Toast.makeText(contexto, "BD Copiada", Toast.LENGTH_SHORT).show()
    resultado = true
   }
   else{
    Toast.makeText(
     contexto,
     "Error en la Copia de BD",
     Toast.LENGTH_SHORT
    ).show()
    resultado = false
   }
  }
  return resultado
 }

 fun getDeviceId(): String? {
  //val deviceId: String
  /*deviceId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
   Settings.Secure.getString(
    context.contentResolver,
    Settings.Secure.ANDROID_ID
   )
  } else {
   val mTelephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
   if (mTelephony.deviceId != null) {
    mTelephony.deviceId
   } else {
    Settings.Secure.getString(
     context.contentResolver,
     Settings.Secure.ANDROID_ID
    )
   }
  }*/
  //Log.d("...Valores de sistema...", "Board " + Build.BOARD)
  //Log.d("...Valores de sistema...", "Device " + Build.DEVICE)
  //Log.d("...Valores de sistema...", "User " + Build.USER)
  //Log.d("...Valores de sistema...", "User " + Build.ID)
  return Build.ID
 }

 fun aInsertarAnalogValue(mCtx: Context, analogvalue: analogvalue_enj, msp : MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/

  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  Log.d("--------- ainsertaranalogvalue -- ", "Llego a ainsertaranalogvalue")
  //if(nsc.isNetworkAvailable(mCtx))
  Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    Log.w("insertaranalogvalue sincrono", "Falla en AInsertarAnalogValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    Log.d("--------- ainsertaranalogvalue --", "TASK - " + task.result.toString())
    //var result : Boolean? = null
    //if (result == true)

    //if(!task.result.toString().contains("Error", true))
    // updateSincronizadoAnalogValue(0, insertado, 1)

   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK", task.result.length.toString())
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDANALOGVALUE,
    Response.Listener { response ->
     //Toast.makeText(mCtx, response, Toast.LENGTH_SHORT).show()
     Log.d("---------Listener analogvalue", response.toString())

     val jsonObject = JSONObject(response)
     val jsonArray = jsonObject.getJSONArray("message_response")
//     val resultado = jsonArray.getJSONObject(0)  //jsonObject.get("message_response").toString()
     for (i in 0 until jsonArray.length()) {
      val resultado = jsonArray.getJSONObject(i)
      if (resultado.getInt("insertado") > 0) {
       val analog_mrid = resultado.getInt("analog_mrid")
       val id_actividad = resultado.getInt("id_actividad")
       if (!resultado.get("resultado").toString().contains("error", true)) {
        updateSincronizadoAnalogValue(0, analog_mrid, id_actividad)
       }
      }
     }
    },
    Response.ErrorListener { error ->
     Toast.makeText(mCtx, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener analogvalue", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["analog_mrid"] = analogvalue.ANALOG_MRID.toString()
     param["loctimestamp"] = analogvalue.Loctimestamp
     param["valueorig"] = analogvalue.ValueOrig.toString()
     param["valueedit"] = analogvalue.ValueOrig.toString()
     param["insert_user"] = analogvalue.InsertUser
     param["update_user"] = analogvalue.InsertUser
     param["insert_timestamp"] = analogvalue.InsertTimestamp
     param["id_actividad"] = analogvalue.IdActividad.toString()
     param["origen"] = analogvalue.Origen
     param["observacion"] = analogvalue.Observacion
     param["idobservaciones"] = analogvalue.IdObservaciones.toString()
     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   //Log.d("Llamada MySingleton", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG, msg)
   //Log.d("SendTokenID", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }

 fun aInsertarStringValue(mCtx: Context, stringvalue : stringvalue_enj, msp : MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/

  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    Log.w("---------insertarstringvalue sincrono", "Falla en AInsertarStringValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    Log.d("--------- ainsertarstringvalue --", task.result)
    //var result : Boolean? = null

    //if(!task.result.toString().contains("Error", true))
    // updateSincronizadoStringValue(0, insertado, 4)
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK", task.result.length.toString())
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDSTRINGVALUE,
    Response.Listener { response ->
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     Log.d("---------Listener stringvalue", response.toString())
     val jsonObject = JSONObject(response)
     val jsonArray = jsonObject.getJSONArray("message_response")
       //jsonObject.get("message_response").toString()

     for (i in 0 until jsonArray.length()){
      val resultado = jsonArray.getJSONObject(i)
      if (resultado.getInt("insertado") > 0){
       val stringMRID = resultado.getInt("string_mrid")
       val idActividad = resultado.getInt("id_actividad")
       updateSincronizadoStringValue(0, stringMRID, idActividad)
      }        //(response.isDigitsOnly() && response.toInt() > 0))
     }

    },
    Response.ErrorListener { error ->
     Toast.makeText(mCtx, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener stringvalue", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["string_mrid"] = stringvalue.STRING_MRID.toString()
     param["loctimestamp"] = stringvalue.Loctimestamp
     param["valueorig"] = stringvalue.ValueOrig
     param["valueedit"] = stringvalue.ValueOrig
     param["insert_user"] = stringvalue.InsertUser
     param["update_user"] = stringvalue.InsertUser
     param["insert_timestamp"] = stringvalue.InsertTimestamp
     param["id_actividad"] = stringvalue.IdActividad.toString()
     param["origen"] = stringvalue.Origen
     param["observacion"] = stringvalue.Observacion
     param["idobservaciones"] = stringvalue.IdObservaciones.toString()

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   ////Log.d("Llamada MySingleton", SendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)
   //verStringValues(PrincipalActivity.actualMRID, 24)
   //habilitaBotones(insertado.toInt())

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG, msg)
   //Log.d("SendTokenID", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }

 fun aInsertarTimeValue(mCtx: Context, timevalue : timevalue_enj, msp: MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/
  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    //Log.w(TAG, "Falla en AInsertarTimeValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    //if (id > 0){
    //var result : Boolean? = null

    //if(!task.result.toString().contains("Error", true))
    // updateSincronizadoTimeValue(0, id, 5)
    //}
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK TimeValue", task.result.length.toString())
   //Log.d("TASK TimeValue", task.result)
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDTIMEVALUE,
    Response.Listener { response ->
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     Log.d("---------Listener timevalue", response.toString())
     val jsonObject = JSONObject(response)
     val jsonArray = jsonObject.getJSONArray("message_response")
     for (i in 0 until jsonArray.length()) {
      val resultado = jsonArray.getJSONObject(i)
      if (resultado.getInt("insertado") > 0) {
       val timeMrid = resultado.getInt("time_mrid")
       val idActividad = resultado.getInt("id_actividad")
       if (!resultado.get("resultado").toString().contains("error", true)) {
        updateSincronizadoTimeValue(0, timeMrid, idActividad)
       }
      }
     }
    },
    Response.ErrorListener { error ->
     Toast.makeText(mCtx, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener timevalue", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["time_mrid"] = timevalue.TIME_MRID.toString()
     param["loctimestamp"] = timevalue.Loctimestamp
     param["valueorig"] = timevalue.ValueOrig
     param["valueedit"] = timevalue.ValueOrig
     param["insert_user"] = timevalue.InsertUser
     param["update_user"] = timevalue.InsertUser
     param["insert_timestamp"] = timevalue.InsertTimestamp
     param["id_actividad"] = timevalue.IdActividad.toString()
     param["origen"] = timevalue.Origen
     param["observacion"] = timevalue.Observacion
     param["idobservaciones"] = timevalue.IdObservaciones.toString()

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   //Log.d("Llamada MySingleton timevalue", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG + " timevalue", msg)
   //Log.d("SendTokenID timevalue", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }

 /*fun aInsertarAnalogValueArray(mCtx: Context, analogvaluearray: JSONArray, insertado: Long, msp : MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/

  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  Log.d("--------- ainsertaranalogvalue -- ", "Llego a ainsertaranalogvalue")
  //if(nsc.isNetworkAvailable(mCtx))
  if(true) Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    Log.w("insertaranalogvalue sincrono", "Falla en AInsertarAnalogValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    Log.d("--------- ainsertaranalogvalue --", task.result)
    //var result : Boolean? = null

    //if (result == true)
    //if(!task.result.toString().contains("Error", true))
    // updateSincronizadoAnalogValue(0, insertado, 1)
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK", task.result.length.toString())
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDANALOGVALUE_ARRAY,
    Response.Listener { response ->
     //Toast.makeText(mCtx, response, Toast.LENGTH_SHORT).show()
     Log.d("---------Listener analogvalue", response.toString())

     val jsonObject = JSONObject(response)
     val jsonArray = jsonObject.getJSONArray("message_response")
//     val resultado = jsonArray.getJSONObject(0)  //jsonObject.get("message_response").toString()
     for (i in 0 until jsonArray.length()) {
      val resultado = jsonArray.getJSONObject(i)
      if (resultado.getInt("insertado") > 0) {
       val analogMrid = resultado.getInt("analog_mrid")
       val idActividad = resultado.getInt("id_actividad")
       if (!resultado.get("resultado").toString().contains("error", true)) {
        updateSincronizadoAnalogValue(0, analogMrid, idActividad)
       }
      }
     }
    },
    Response.ErrorListener { error ->
     Toast.makeText(mCtx, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener analogvalue", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["analogvaluearray"] = analogvaluearray.toString()
     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   //Log.d("Llamada MySingleton", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG, msg)
   //Log.d("SendTokenID", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }*/

 /*fun aInsertarStringValueArray(mCtx: Context, stringvaluearray : JSONArray, insertado: Long, msp : MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/

  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    Log.w("---------insertarstringvalue sincrono", "Falla en AInsertarStringValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    Log.d("--------- ainsertarstringvalue --", task.result)
    //var result : Boolean? = null

    //if(!task.result.toString().contains("Error", true))
    // updateSincronizadoStringValue(0, insertado, 4)
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK", task.result.length.toString())
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDSTRINGVALUE_ARRAY,
    Response.Listener { response ->
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     Log.d("---------Listener stringvalue", response.toString())
     val jsonObject = JSONObject(response)
     val jsonArray = jsonObject.getJSONArray("message_response")
     //jsonObject.get("message_response").toString()

     for (i in 0 until jsonArray.length()){
      val resultado = jsonArray.getJSONObject(i)
      if (resultado.getInt("insertado") > 0){
       val string_mrid = resultado.getInt("string_mrid")
       val id_actividad = resultado.getInt("id_actividad")
       updateSincronizadoStringValue(0, string_mrid, id_actividad)
      }        //(response.isDigitsOnly() && response.toInt() > 0))
     }
    },
    Response.ErrorListener { error ->
     Toast.makeText(mCtx, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener stringvalue", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["stringvaluearray"] = stringvaluearray.toString()

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   ////Log.d("Llamada MySingleton", SendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)
   //verStringValues(PrincipalActivity.actualMRID, 24)
   //habilitaBotones(insertado.toInt())

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG, msg)
   //Log.d("SendTokenID", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }*/

 /*fun aInsertarTimeValueArray(mCtx: Context, timevaluearray : JSONArray, id : Long, msp: MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/
  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    //Log.w(TAG, "Falla en AInsertarTimeValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    //if (id > 0){
    //var result : Boolean? = null

    //if(!task.result.toString().contains("Error", true))
    // updateSincronizadoTimeValue(0, id, 5)
    //}
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK TimeValue", task.result.length.toString())
   //Log.d("TASK TimeValue", task.result)
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDTIMEVALUE_ARRAY,
    Response.Listener { response ->
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     Log.d("---------Listener timevalue ARRAY", response.toString())
     val jsonObject = JSONObject(response)
     val jsonArray = jsonObject.getJSONArray("message_response")
     for (i in 0 until jsonArray.length()) {
      val resultado = jsonArray.getJSONObject(i)
      if (resultado.getInt("insertado") > 0) {
       val time_mrid = resultado.getInt("time_mrid")
       val id_actividad = resultado.getInt("id_actividad")
       if (!resultado.get("resultado").toString().contains("error", true)) {
        updateSincronizadoTimeValue(0, time_mrid, id_actividad)
       }
      }
     }
    },
    Response.ErrorListener { error ->
     Toast.makeText(mCtx, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener timevalue", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["timevaluearray"] = timevaluearray.toString()

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   //Log.d("Llamada MySingleton timevalue", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG + " timevalue", msg)
   //Log.d("SendTokenID timevalue", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }*/

 fun aInsertarAnalog(mCtx: Context, analog : analog, id : Long, msp: MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/
  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    //Log.w(TAG, "Falla en AInsertarTimeValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    //if (id > 0){
    //var result : Boolean? = null

    //if (result == true) updateSincronizadoAnalog(0, id, 5)
    //}
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK TimeValue", task.result.length.toString())
   //Log.d("TASK TimeValue", task.result)
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDANALOG,
    Response.Listener { response ->
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     //Log.d("Listener analog", response)
     if(!response.toString().contains("Error", true) || response.toInt() > 0)
      updateSincronizadoAnalog(0, id)
    },
    Response.ErrorListener { error ->
     //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener analog", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["mrid"] = analog.MRID.toString()
     param["name"] = analog.Name
     param["localname"] = analog.LocalName
     param["pathname"] = analog.PathName
     param["description"] = analog.Description
     param["maxvalue"] = analog.MaxValue.toString()
     param["minvalue"] = analog.MinValue.toString()
     param["INSERT_USER"] = analog.InsertUser
     param["INSERT_TIMESTAMP"] = analog.InsertTimestamp
     param["id_nfc"] = analog.IdNFC
     param["id_sistema"] = analog.IdSistema.toString()
     param["id_tipo"] = analog.IdTipo.toString()
     param["id_estado"] = analog.IdEstado.toString()
     param["unidades"] = analog.Unidades
     param["orden_medicion"] = analog.OrdenMedicion.toString()
     param["idobservaciones"] = analog.IdObservaciones.toString()
     param["observacion"] = analog.Observacion

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   //Log.d("Llamada MySingleton ANALOG", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG + " timevalue", msg)
   //Log.d("SendTokenID timevalue", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }

 fun aInsertarString(mCtx: Context, string : string, id : Long, msp: MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/
  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    //Log.w(TAG, "Falla en AInsertarTimeValue", task.exception)
    return@OnCompleteListener
   } else {
    // actualiza valor de sincronizado pone 0
    //if (id > 0){
    //var result : Boolean? = null

    //if (result == true) updateSincronizadoString(0, id, 5)
    //}
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK TimeValue", task.result.length.toString())
   //Log.d("TASK TimeValue", task.result)
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDSTRING,
    Response.Listener {
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     //Log.d("Listener string", response)
     updateSincronizadoString(0, id)
    },
    Response.ErrorListener { error ->
     //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener string", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["mrid"] = string.MRID.toString()
     param["name"] = string.Name
     param["localname"] = string.LocalName
     param["pathname"] = string.PathName
     param["description"] = string.Description
     param["SELCBO"] = string.SELCBO.toString()
     param["INSERT_USER"] = string.InsertUser
     param["INSERT_TIMESTAMP"] = string.InsertTimestamp
     param["id_nfc"] = string.IdNFC
     param["id_sistema"] = string.IdSistema.toString()
     param["id_tipo"] = string.IdTipo.toString()
     param["id_estado"] = string.IdEstado.toString()
     param["unidades"] = string.Unidades
     param["orden_medicion"] = string.OrdenMedicion.toString()
     param["idobservaciones"] = string.IdObservaciones.toString()
     param["observacion"] = string.Observacion

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   Log.d("Llamada MySingleton string", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG + " timevalue", msg)
   //Log.d("SendTokenID timevalue", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }

 fun aInsertarTipos(mCtx: Context, tipo : tipos, id : Long, msp: MySingleton){
  /*var sharedPreferences = getSharedPreferences(
   resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE
   //"FCM", Context.MODE_PRIVATE
  )*/
  //var ms = msp
  //var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
  var sendTokenID: StringRequest
  //val token_id = sharedPreferences.getString("FCM", null)
  //Toast.makeText(this, token_id, Toast.LENGTH_SHORT).show()
  //Toast.makeText(this, resources.getString(R.string.FCM_TOKEN), Toast.LENGTH_SHORT).show()
  /*if (MainScope().launch {
   withContext(Dispatchers.Default){
    //var nsc = NetworkStateChecker()
    Log.d("------- servidor disponible ", nsc.isReachableByTcp("172.16.231.6", 3306, 5000).toString())
    nsc.isReachableByTcp("172.16.231.6", 3306, 5000)
   }
  })*/

  if(NetworkStateChecker().isNetworkAvailable(mCtx)) Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
   if (!task.isSuccessful) {
    //Log.w(TAG, "Falla en AInsertarTimeValue", task.exception)
    return@OnCompleteListener
   }

   // Get new FCM registration token
   //token_id = task.result
   //Log.d("TASK TimeValue", task.result.length.toString())
   //Log.d("TASK TimeValue", task.result)
   sendTokenID = object : StringRequest(
    Method.POST, URL_SENDTIPOS,
    Response.Listener { response ->
     //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
     Log.d("Listener tipos", response)
     updateSincronizadoTipos(0, id)
    },
    Response.ErrorListener { error ->
     //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
     Log.d("ErrListener tipos", error.toString())
    }) {
    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String> {
     val param: MutableMap<String, String> = HashMap()
     param["nombre"] = tipo.nombre

     ////Log.d("param", param.toString())
     ////Log.d("param.token", param["tokenid"].toString().length.toString())
     return param
    }
   }
   //Log.d("Llamada MySingleton tipos", sendTokenID.toString())

   //ms = MySingleton.getInstance(mCtx)
   /*SendTokenID.setRetryPolicy(
    DefaultRetryPolicy(
     30000,
     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    )
   )*/
   msp.addToRequestQueue<String>(sendTokenID)

   // Log and toast
   //val msg = getString(R.string.msg_token_fmt, token_id)
   //Log.d(TAG + " timevalue", msg)
   //Log.d("SendTokenID timevalue", sendTokenID.toString())
   //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
  })
 }
}