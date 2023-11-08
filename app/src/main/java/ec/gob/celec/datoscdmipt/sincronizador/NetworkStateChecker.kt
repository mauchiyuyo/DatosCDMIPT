package ec.gob.celec.datoscdmipt.sincronizador

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress

class NetworkStateChecker : BroadcastReceiver()  {
    //context and database helper object
    private var context: Context? = null
    private var db: DatabaseOpenHelper? = null
    private lateinit var ms : MySingleton
    companion object{
        //val IP_SERVER = R.string.ipServer.toString() //"172.16.231.6"
        //val WEB_SERVER = R.string.webServer.toString()

        const val IP_SERVER = "172.16.231.6"
        //val WEB_SERVER = "http://172.16.231.6"

        //var URL_SENDANALOGVALUE = "$WEB_SERVER/DatosCdM/sendanalogvalue.php"
        //var URL_SENDSTRINGVALUE = "$WEB_SERVER/DatosCdM/sendstringvalue.php"
        //var URL_SENDTIMEVALUE =   "$WEB_SERVER/DatosCdM/sendtimevalue.php"
        //var URL_SENDANALOG =      "$WEB_SERVER/DatosCdM/sendanalog.php"
        //var URL_SENDSTRING =      "$WEB_SERVER/DatosCdM/sendstring.php"
        //var URL_SENDTIME =        "$WEB_SERVER/DatosCdM/sendtime.php"
        //var URL_SENDUSUARIOS =    "$WEB_SERVER/DatosCdM/sendusuarios.php"
        //var URL_SENDACTIVIDADES = "$WEB_SERVER/DatosCdM/sendactividades.php"
        //var URL_SENDTIPOS =       "$WEB_SERVER/DatosCdM/sendtipos.php"

    }

    override fun onReceive(context: Context, intent: Intent?) {
        //Log.d("------- ip server", R.string.ipServer.toString())
        //Log.d("------- ip server", R.string.webServer.toString())

        /*if (isNetworkAvailable(context)) {
            this.context = context
            ms = MySingleton(context)
            var result : Boolean? = null
            db = DatabaseOpenHelper(context, "", null, 1)
            //val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork //?: null //return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) //?: null // return false
            //result = when {
            //    actNw!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            //    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //    else -> false
            //}
            //val activeNetwork = cm.activeNetworkInfo
            var sSendTokenID: StringRequest
            //if there is a network
            //if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            //if (activeNetwork.type == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
            //if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
            MainScope().launch {
                withContext(Dispatchers.Default){
                    //Log.d("------- servidor disponible ", isReachableByTcp(IP_SERVER.toString(), 3306, 5000).toString())
                    result = isReachableByTcp(IP_SERVER.toString(), 3306, 5000)
                }
            }

            //Log.d("--- NetworkStateChecker --- ", "result " + result.toString())
            if(true){

                synchroTimeValue(context)

                synchroActividades(context)

                synchroAnalog(context)

                synchroAnalogValue(context)

                synchroString(context)

                synchroStringValue(context)

                synchroObservaciones(context)

                synchroTipos(context)

                synchroUsuarios(context)

            }
            //}
        }*/
    }

    @SuppressLint("Range")
    fun synchroTimeValue(context: Context) : Int {
        //getting all the timevalues por sincronizar
        var contador = 0
        val cursortv: Cursor? = db!!.obtenerPorSincronizarTimeValue()
        if (cursortv != null) {
            //Log.d("--- NetworkStateChecker --- ", "cursortv " + cursortv!!.count.toString())
            contador = cursortv.count
            if(cursortv.moveToFirst()) {
                do {
                    val timevalue = timevalue_enj()
                    //val insertado : Long = cursortv.getInt(cursortv.getColumnIndex("id")).toLong()
                    timevalue.TIME_MRID = cursortv.getInt(cursortv.getColumnIndex("TIME_MRID"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    timevalue.Loctimestamp = cursortv.getString(cursortv.getColumnIndex("LOCTIMESTAMP"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    timevalue.ValueOrig = cursortv.getString(cursortv.getColumnIndex("VALUEORIG"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    timevalue.InsertUser = cursortv.getString(cursortv.getColumnIndex("INSERT_USER"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    timevalue.InsertTimestamp = cursortv.getString(cursortv.getColumnIndex("INSERT_TIMESTAMP"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    timevalue.IdActividad = cursortv.getInt(cursortv.getColumnIndex("id_actividad"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    timevalue.Origen = db!!.getDeviceId().toString()
                    timevalue.IdObservaciones = cursortv.getInt(cursortv.getColumnIndex("idobservaciones"))
                    timevalue.Observacion = cursortv.getString(cursortv.getColumnIndex("observacion"))
                    //calling the method to save the unsynced name to MySQL

                    db!!.aInsertarTimeValue(context, timevalue, ms)
                } while (cursortv.moveToNext())
            }
        }
        return contador
    }

    @SuppressLint("Range")
    fun synchroAnalogValue(context: Context) : Int {
        //getting all the analogvalues por sincronizar
        var contador = 0
        val cursorav: Cursor? = db!!.obtenerPorSincronizarAnalogValue()
        if (cursorav != null) {
            contador = cursorav.count
            if (cursorav.moveToFirst()) {
                var i = 0
                do {
                    i += 1
                    //Log.d("el cursor numero.............." , (cursorav.count - i).toString())
                    val analogvalue = analogvalue_enj()
                    //val insertado: Long = cursorav.getInt(cursorav.getColumnIndex("id")).toLong()
                    analogvalue.ANALOG_MRID =
                        cursorav.getInt(cursorav.getColumnIndex("ANALOG_MRID"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    analogvalue.Loctimestamp =
                        cursorav.getString(cursorav.getColumnIndex("LOCTIMESTAMP"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    analogvalue.ValueOrig =
                        cursorav.getDouble(cursorav.getColumnIndex("VALUEORIG"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    analogvalue.InsertUser =
                        cursorav.getString(cursorav.getColumnIndex("INSERT_USER"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    analogvalue.InsertTimestamp =
                        cursorav.getString(cursorav.getColumnIndex("INSERT_TIMESTAMP"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analogvalue.Origen = db!!.getDeviceId().toString()
                    analogvalue.IdObservaciones =
                        cursorav.getInt(cursorav.getColumnIndex("idobservaciones"))
                    if(cursorav.getString(cursorav.getColumnIndex("observacion")) == null) {
                        analogvalue.Observacion = ""
                    } else {
                        analogvalue.Observacion =
                            cursorav.getString(cursorav.getColumnIndex("observacion"))
                    }
                    analogvalue.IdActividad = cursorav.getInt(cursorav.getColumnIndex("id_actividad"))

                    //calling the method to save the unsynced name to MySQL
                    /*var token_id : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
                    Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            //Log.w(TAG, "Falla en AInsertarAnalogValue", task.exception)
                            return@OnCompleteListener
                        } else {
                            // actualiza valor de sincronizado pone 0
                            db!!.updateSincronizado(0, insertado, 1)
                        }

                        // Get new FCM registration token
                        token_id = task.result
                        //Log.d("TASK", task.result.length.toString())
                        sSendTokenID = object : StringRequest(
                            Method.POST, URL_SENDANALOGVALUE,
                            Response.Listener { response ->
                                //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                                Log.d("Listener", response)
                            },
                            Response.ErrorListener { error ->
                                //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                                Log.d("ErrListener", error.toString())
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
                                param["idobservaciones"] =
                                    analogvalue.IdObservaciones.toString()
                                ////Log.d("param", param.toString())
                                ////Log.d("param.token", param["tokenid"].toString().length.toString())
                                return param
                            }
                        }
                        //Log.d("Llamada MySingleton", SendTokenID.toString())

                        ms = MySingleton.getInstance(context)
                        /*SendTokenID.setRetryPolicy(
                    DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                    )
                )*/
                        ms.addToRequestQueue<String>(sSendTokenID)
                    })*/
                    db!!.aInsertarAnalogValue(context, analogvalue, ms)
                } while (cursorav.moveToNext())
            }
        }
        return  contador
    }

    @SuppressLint("Range")
    fun synchroStringValue(context: Context) : Int {
        //getting all the stringvalues por sincronizar
        var contador = 0
        val cursorsv: Cursor? = db!!.obtenerPorSincronizarStringValue()
        if (cursorsv != null) {
            contador = cursorsv.count
            if(cursorsv.moveToFirst()) {
                do {
                    val stringvalue = stringvalue_enj()
                    //val insertado : Long = cursorsv.getInt(cursorsv.getColumnIndex("id")).toLong()
                    stringvalue.STRING_MRID = cursorsv.getInt(cursorsv.getColumnIndex("STRING_MRID"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    stringvalue.Loctimestamp = cursorsv.getString(cursorsv.getColumnIndex("LOCTIMESTAMP"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    stringvalue.ValueOrig = cursorsv.getString(cursorsv.getColumnIndex("VALUEORIG"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    stringvalue.InsertUser = cursorsv.getString(cursorsv.getColumnIndex("INSERT_USER"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    stringvalue.InsertTimestamp = cursorsv.getString(cursorsv.getColumnIndex("INSERT_TIMESTAMP"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    stringvalue.IdActividad = cursorsv.getInt(cursorsv.getColumnIndex("id_actividad"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    stringvalue.Origen = db!!.getDeviceId().toString()
                    stringvalue.IdObservaciones = cursorsv.getInt(cursorsv.getColumnIndex("idobservaciones"))
                    stringvalue.Observacion = cursorsv.getString(cursorsv.getColumnIndex("observacion"))
                    //calling the method to save the unsynced name to MySQL

                    /*var token_id : String
                    Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            //Log.w(TAG, "Falla en AInsertarStringValue", task.exception)
                            return@OnCompleteListener
                        } else {
                            // actualiza valor de sincronizado pone 0
                            db!!.updateSincronizado(0, insertado, 4)
                        }

                        // Get new FCM registration token
                        token_id = task.result
                        //Log.d("TASK", task.result.length.toString())
                        sSendTokenID = object : StringRequest(
                            Method.POST, URL_SENDSTRINGVALUE,
                            Response.Listener { response ->
                                //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                                Log.d("Listener", response)
                            },
                            Response.ErrorListener { error ->
                                //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                                Log.d("ErrListener", error.toString())
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
                                //Log.d("param", param.toString())
                                //Log.d("param.token", param["tokenid"].toString().length.toString())
                                return param
                            }
                        }
                        //Log.d("Llamada MySingleton", SendTokenID.toString())

                        ms = MySingleton.getInstance(context)
                        /*SendTokenID.setRetryPolicy(
                        DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                        )
                    )*/
                        ms.addToRequestQueue<String>(sSendTokenID)

                    })*/
                    db!!.aInsertarStringValue(context, stringvalue, ms)

                } while (cursorsv.moveToNext())
            }
        }
        return contador
    }

    /*@SuppressLint("Range")
    fun synchroString(context: Context){
        // getting all the string por sincronizar
        val cursors : Cursor? = db!!.obtenerPorSincronizarString()
        if (cursors != null) {
            if(cursors.moveToFirst()) {
                do {
                    val string = string()
                    var insertado : Long = cursors.getInt(cursors.getColumnIndex("MRID")).toLong()
                    string.MRID = cursors.getInt(cursors.getColumnIndex("MRID"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    string.Name = cursors.getString(cursors.getColumnIndex("NAME"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    string.LocalName = cursors.getString(cursors.getColumnIndex("LOCALNAME"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    string.PathName = cursors.getString(cursors.getColumnIndex("PATHNAME"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    string.AliasName = cursors.getString(cursors.getColumnIndex("ALIASNAME"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    string.Description = cursors.getString(cursors.getColumnIndex("DESCRIPTION"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    string.SELCBO = cursors.getInt(cursors.getColumnIndex("SELCBO"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    string.InsertUser = cursors.getString(cursors.getColumnIndex("INSERT_USER"))
                    string.InsertTimestamp = cursors.getString(cursors.getColumnIndex("INSERT_TIMESTAMP"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    string.IdNFC = cursors.getString(cursors.getColumnIndex("id_nfc"))
                    string.IdSistema = cursors.getInt(cursors.getColumnIndex("id_sistema"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    string.IdTipo = cursors.getInt(cursors.getColumnIndex("id_tipo"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    string.IdEstado = cursors.getInt(cursors.getColumnIndex("id_estado"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    string.Unidades = cursors.getString(cursors.getColumnIndex("unidades"))
                    string.OrdenMedicion =  cursors.getInt(cursors.getColumnIndex("orden_medicion"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    string.IdObservaciones =  cursors.getInt(cursors.getColumnIndex("idobservaciones"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    string.Observacion =  cursors.getString(cursors.getColumnIndex("observacion"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))

                    //calling the method to save the unsynced name to MySQL
                    db!!.aInsertarString(context, string, insertado, ms)
                } while (cursors.moveToNext())
            }
        }
    }*/

    /*fun synchroTime(context: Context){
        // getting all the time por sincronizar
        /*val cursort : Cursor? = db!!.ObtenerPorSincronizarTime(context)
        if (cursort != null) {
            if(cursort.moveToFirst()) {
                do {
                    val time = time()
                    time.MRID = cursort.getInt(cursort.getColumnIndex("mrid"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    time.Name = cursort.getString(cursort.getColumnIndex("name"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    time.LocalName = cursort.getString(cursort.getColumnIndex("localname"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    time.PathName = cursort.getString(cursort.getColumnIndex("pathname"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    time.AliasName = cursort.getString(cursort.getColumnIndex("aliasname"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    time.Description = cursort.getString(cursort.getColumnIndex("description"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    time.InsertUser = cursort.getString(cursort.getColumnIndex("INSERT_USER"))
                    time.InsertTimestamp = cursort.getString(cursort.getColumnIndex("INSERT_TIMESTAMP"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    time.OrdenMedicion =  cursort.getInt(cursort.getColumnIndex("orden_medicion"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    time.IdSistema = cursort.getInt(cursort.getColumnIndex("id_sistema"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    time.IdTipo = cursort.getInt(cursort.getColumnIndex("id_tipo"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    time.IdEstado = cursort.getInt(cursort.getColumnIndex("id_estado"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    time.Unidades = cursort.getString(cursort.getColumnIndex("unidades"))

                    //calling the method to save the unsynced name to MySQL
                    sSendTokenID = object : StringRequest(
                        Method.POST, URL_SENDTIME,
                        Response.Listener { response ->
                            //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                            Log.d("Listener", response)
                        },
                        Response.ErrorListener { error ->
                            //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                            Log.d("ErrListener", error.toString())
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val param: MutableMap<String, String> = HashMap()
                            param["mrid"] = time.MRID.toString()
                            param["name"] = time.Name
                            param["localname"] = time.LocalName
                            param["pathname"] = time.PathName
                            param["aliasname"] = time.AliasName
                            param["description"] = time.Description
                            param["INSERT_USER"] = time.InsertUser
                            param["INSERT_TIMESTAMP"] = time.InsertTimestamp
                            param["orden_medicion"] = time.OrdenMedicion.toString()
                            param["id_sistema"] = time.IdSistema.toString()
                            param["id_tipo"] = time.IdTipo.toString()
                            param["id_estado"] = time.IdEstado.toString()
                            param["unidades"] = time.Unidades
                            param["idobservaciones"] = time.IdObservaciones.toString()
                            param["observacion"] = time.Observacion

                            //Log.d("param", param.toString())
                            //Log.d("param.token", param["tokenid"].toString().length.toString())
                            return param
                        }
                    }
                    //Log.d("Llamada MySingleton", SendTokenID.toString())

                    ms = MySingleton.getInstance(context)
                    /*SendTokenID.setRetryPolicy(
                        DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                        )
                    )*/
                    ms.addToRequestQueue<String>(sSendTokenID)
                } while (cursort.moveToNext())
            }
        }*/
    }*/

    /*@SuppressLint("Range")
    fun synchroAnalog(context: Context){
        // getting all the analog por sincronizar
        val cursora : Cursor? = db!!.obtenerPorSincronizarAnalog()
        if (cursora != null) {
            if(cursora.moveToFirst()) {
                do {
                    val analog = analog()
                    val insertado : Long = cursora.getInt(cursora.getColumnIndex("mrid")).toLong()
                    analog.MRID = cursora.getInt(cursora.getColumnIndex("mrid"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    analog.Name = cursora.getString(cursora.getColumnIndex("name"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    analog.LocalName = cursora.getString(cursora.getColumnIndex("localname"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    analog.PathName = cursora.getString(cursora.getColumnIndex("pathname"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    analog.AliasName = cursora.getString(cursora.getColumnIndex("aliasname"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    analog.Description = cursora.getString(cursora.getColumnIndex("description"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    analog.MaxValue = cursora.getDouble(cursora.getColumnIndex("maxvalue"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    analog.MinValue = cursora.getDouble(cursora.getColumnIndex("minvalue"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    analog.InsertUser = cursora.getString(cursora.getColumnIndex("INSERT_USER"))
                    analog.InsertTimestamp = cursora.getString(cursora.getColumnIndex("INSERT_TIMESTAMP"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analog.IdNFC = cursora.getString(cursora.getColumnIndex("id_nfc"))
                    analog.IdSistema = cursora.getInt(cursora.getColumnIndex("id_sistema"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analog.IdTipo = cursora.getInt(cursora.getColumnIndex("id_tipo"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analog.IdEstado = cursora.getInt(cursora.getColumnIndex("id_estado"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analog.Unidades = cursora.getString(cursora.getColumnIndex("unidades"))
                    analog.OrdenMedicion =  cursora.getInt(cursora.getColumnIndex("orden_medicion"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analog.IdObservaciones =  cursora.getInt(cursora.getColumnIndex("idobservaciones"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    analog.Observacion =  cursora.getString(cursora.getColumnIndex("observacion"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))

                    //calling the method to save the unsynced name to MySQL
                    db!!.aInsertarAnalog(context, analog, insertado, ms)
                } while (cursora.moveToNext())
            }
        }
    }*/

    /*fun synchroActividades(context: Context){
        // getting all the actividades por sincronizar
        /*val cursorac : Cursor? = db!!.ObtenerPorSincronizarActividades(context)
        if (cursorac != null) {
            if(cursorac.moveToFirst()) {
                do {
                    val actividad = actividades()
                    actividad.id = cursorac.getInt(cursorac.getColumnIndex("id"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    actividad.fecha = cursorac.getString(cursorac.getColumnIndex("fecha"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    actividad.horainicio = cursorac.getString(cursorac.getColumnIndex("horainicio"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    actividad.horafin = cursorac.getString(cursorac.getColumnIndex("horafin"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    actividad.id_sistema = cursorac.getInt(cursorac.getColumnIndex("id_sistema"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    actividad.ipt = cursorac.getInt(cursorac.getColumnIndex("ipt"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    actividad.operador = cursorac.getString(cursorac.getColumnIndex("operador"))
                    actividad.observacion = cursorac.getString(cursorac.getColumnIndex("observacion"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))
                    actividad.idobservaciones =  cursorac.getInt(cursorac.getColumnIndex("idobservaciones"))//cursor.getColumnIndex(AnalogValue_InsertTimestamp))

                    //calling the method to save the unsynced name to MySQL
                    sSendTokenID = object : StringRequest(
                        Method.POST, URL_SENDACTIVIDADES,
                        Response.Listener { response ->
                            //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                            Log.d("Listener", response)
                        },
                        Response.ErrorListener { error ->
                            //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                            Log.d("ErrListener", error.toString())
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val param: MutableMap<String, String> = HashMap()
                            param["id"] = actividad.id.toString()
                            param["fecha"] = actividad.fecha.toString()
                            param["horainicio"] = actividad.horainicio.toString()
                            param["horafin"] = actividad.horafin.toString()
                            param["id_sistema"] = actividad.id_sistema.toString()
                            param["ipt"] = actividad.ipt.toString()
                            param["operador"] = actividad.operador
                            param["observacion"] = actividad.observacion
                            param["idobservaciones"] = actividad.idobservaciones.toString()

                            //Log.d("param", param.toString())
                            //Log.d("param.token", param["tokenid"].toString().length.toString())
                            return param
                        }
                    }
                    //Log.d("Llamada MySingleton", SendTokenID.toString())

                    ms = MySingleton.getInstance(context)
                    /*SendTokenID.setRetryPolicy(
                        DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                        )
                    )*/
                    ms.addToRequestQueue<String>(sSendTokenID)
                } while (cursorac.moveToNext())
            }
        }*/
    }*/

    /*fun synchroUsuarios(context: Context){
        // getting all the usuarios por sincronizar
        /*val cursoru : Cursor? = db!!.ObtenerPorSincronizarUsuarios(context)
        if (cursoru != null) {
            if(cursoru.moveToFirst()) {
                do {
                    val usuario = usuario()
                    //usuario.id = cursoru.getInt(cursoru.getColumnIndex("id"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    usuario.usuario = cursoru.getString(cursoru.getColumnIndex("usuario"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))
                    usuario.correo = cursoru.getString(cursoru.getColumnIndex("correo"))//cursor.getColumnIndex(AnalogValue_ValueOrig))
                    usuario.password = cursoru.getString(cursoru.getColumnIndex("password"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    usuario.idCentral = cursoru.getInt(cursoru.getColumnIndex("id_central"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    usuario.idPerfil = cursoru.getInt(cursoru.getColumnIndex("id_perfil"))//cursor.getColumnIndex(AnalogValue_InsertUser))
                    usuario.activado = cursoru.getInt(cursoru.getColumnIndex("activado"))

                    //calling the method to save the unsynced name to MySQL
                    sSendTokenID = object : StringRequest(
                        Method.POST, URL_SENDUSUARIOS,
                        Response.Listener { response ->
                            //Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                            Log.d("Listener", response)
                        },
                        Response.ErrorListener { error ->
                            //Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
                            Log.d("ErrListener", error.toString())
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getParams(): Map<String, String> {
                            val param: MutableMap<String, String> = HashMap()
                            //param["id"] = usuario.id.toString()
                            param["usuario"] = usuario.usuario
                            param["correo"] = usuario.correo
                            param["password"] = usuario.password
                            param["id_perfil"] = usuario.idPerfil.toString()
                            param["id_central"] = usuario.idCentral.toString()
                            param["activado"] = usuario.activado.toString()

                            //Log.d("param", param.toString())
                            //Log.d("param.token", param["tokenid"].toString().length.toString())
                            return param
                        }
                    }
                    //Log.d("Llamada MySingleton", SendTokenID.toString())

                    ms = MySingleton.getInstance(context)
                    /*SendTokenID.setRetryPolicy(
                        DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                        )
                    )*/
                    ms.addToRequestQueue<String>(sSendTokenID)
                } while (cursoru.moveToNext())
            }
        }*/

    }*/

    /*fun synchroObservaciones(context: Context){

    }*/

    /*@SuppressLint("Range")
    fun synchroTipos(context: Context){
        // getting all the usuarios por sincronizar
        val cursort : Cursor? = db!!.obtenerPorSincronizarTipos()
        if (cursort != null) {
            if(cursort.moveToFirst()) {
                do {
                    val tipo = tipos()
                    var insertado : Long = cursort.getInt(cursort.getColumnIndex("id")).toLong()
                    //usuario.id = cursoru.getInt(cursoru.getColumnIndex("id"))//cursor.getColumnIndex(AnalogValue_AMRID))
                    tipo.nombre = cursort.getString(cursort.getColumnIndex("nombre"))//cursor.getColumnIndex(AnalogValue_Loctimestamp))

                    //calling the method to save the unsynced name to MySQL
                    db!!.aInsertarTipos(context, tipo, insertado, ms)
                } while (cursort.moveToNext())
            }
        }

    }*/

    fun isNetworkAvailable(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return (getNetworkCapabilities(activeNetwork)?.run {
                    when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
            } ?: false) //as Boolean
        }
    }

    //fun synchroManual(context: Context, contador : Int) : Int {
    fun synchroManual(context: Context) : Int {
        var cuenta = 0
        var result = false
        if (isNetworkAvailable(context)) {
            this.context = context
            this.ms = MySingleton(context)
            //var result : Boolean? = null
            db = DatabaseOpenHelper(context, "", null, 1)
            //val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //val networkCapabilities = connectivityManager.activeNetwork //?: null //return false
            //val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) //?: null // return false
            //result = when {
            //    actNw!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            //    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            //    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            //    else -> false
            //}
            //val activeNetwork = cm.activeNetworkInfo
            //var sSendTokenID: StringRequest
            //if there is a network
            //if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            //if (activeNetwork.type == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
            //if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
            MainScope().launch {
                withContext(Dispatchers.Default){
                    //Log.d("------- servidor disponible ", isReachableByTcp(IP_SERVER.toString(), 3306, 5000).toString())
                    //TODO: cambiar ip, puerto y tiempo de espera dependiendo ambiente de produccion o desarrollo
                    val host = IP_SERVER
                    val puerto = 3306
                    val timeout = 5000
                    result = isReachableByTcp(host, puerto, timeout)
                }
            }

            //Log.d("--- NetworkStateChecker --- ", "result " + result.toString())
            if(result){
                cuenta = synchroTimeValue(context)
                cuenta += synchroAnalogValue(context)
                cuenta += synchroStringValue(context)
                //synchroActividades(context)
                //synchroAnalog(context)
                //synchroString(context)
                //synchroObservaciones(context)
                //synchroTipos(context)
                //synchroUsuarios(context)
            }
            //}
        }
        return cuenta
    }

    private fun isReachableByTcp(host: String?, port: Int, timeout: Int): Boolean {
        var res = false

        try {
            MainScope().launch {
                withContext(Dispatchers.IO){
                    //Log.d("------- servidor disponible ", nsc.isReachableByTcp(IP_SERVER.toString(), 3306, 5000).toString())
                    val socket = Socket()
                    val socketAddress: SocketAddress = InetSocketAddress(host, port)
                    res = try{
                        socket.connect(socketAddress, timeout)
                        socket.close()
                        true
                    } catch (e: IOException) {
                        false
                    }
                }
            }

        } catch (e: IOException) {
            res = false
        }

        return res
    }

    /*
    * method taking two arguments
    * name that is to be saved and id of the name from SQLite
    * if the name is successfully sent
    * we will update the status as synced in SQLite
    * */
    /*private fun saveName(id: Int, tipo : Int) {
        var db = DatabaseOpenHelper(context!!, "", null, 1)
        val stringRequest: StringRequest =
            object : StringRequest(Request.Method.POST, PrincipalActivity.URL_SENDANALOG,
                object : Response.Listener<String?> {
                    override fun onResponse(response: String?) {
                        try {
                            val obj = JSONObject(response)
                            if (!obj.getBoolean("error")) {
                                //updating the status in sqlite
                                db.updateSincronizado(1, id, tipo)

                                //sending the broadcast to refresh the list
                                context!!.sendBroadcast(Intent())
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                },
                object : Response.ErrorListener {
                    override fun onErrorResponse(error: VolleyError?) {}
                }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): MutableMap<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    param["analog_mrid"] = analogvalue.AMRID.toString()
                    param["loctimestamp"] = analogvalue.Loctimestamp
                    param["valueorig"] = analogvalue.ValueOrig.toString()
                    param["valueedit"] = analogvalue.ValueOrig.toString()
                    param["insert_user"] = analogvalue.InsertUser
                    param["update_user"] = analogvalue.InsertUser
                    param["insert_timestamp"] = analogvalue.InsertTimestamp
                    param["id_actividad"] = analogvalue.IdActividad.toString()
                    param["origen"] = analogvalue.Origen
                    return params
                }
            }
        MySingleton.getInstance(context!!).addToRequestQueue(stringRequest)
    }*/

}