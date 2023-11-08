package ec.gob.celec.datoscdmipt.sincronizador

//import com.google.firebase.iid.FirebaseInstanceId
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.analog
import ec.gob.celec.datoscdmipt.database.models.analogvalue_enj
import ec.gob.celec.datoscdmipt.database.models.string
import ec.gob.celec.datoscdmipt.database.models.stringvalue_enj
import ec.gob.celec.datoscdmipt.database.models.time
import ec.gob.celec.datoscdmipt.database.models.timevalue_enj
import org.json.JSONException
import org.json.JSONObject

class MyFireBaseMessagingService : FirebaseMessagingService() {
    companion object {
        //val IP_SERVER = "172.16.231.6"
        //private const val WEB_SERVER = "http://172.16.231.6"

        //val SYNCHRONIZEANALOG_URL =      "$WEB_SERVER/DatosCdM/synchronizeanalog.php"
        //val SYNCHRONIZESTRING_URL =      "$WEB_SERVER/DatosCdM/synchronizestring.php"
        //val SYNCHRONIZETIME_URL =        "$WEB_SERVER/DatosCdM/synchronizetime.php"
        //val SYNCHRONIZEANALOGVALUE_URL = "$WEB_SERVER/DatosCdM/synchronizeanalogvalue.php"
        //val SYNCHRONIZESTRINGVALUE_URL = "$WEB_SERVER/DatosCdM/synchronizestringvalue.php"
        //val SYNCHRONIZETIMEVALUE_URL =   "$WEB_SERVER/DatosCdM/synchronizetimevalue.php"
        //val SYNCHRONIZEACTIVIDADES_URL = "$WEB_SERVER/DatosCdM/synchronizeactividades.php"
        //val SYNCHRONIZEUSUARIOS_URL =    "$WEB_SERVER/DatosCdM/synchronizeusuarios.php"
        private const val TAG = "--------- MyFirebaseMsgService"

        var Token_ID : String = ""
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        //val Token_ID = FirebaseInstanceId.getInstance().token
        /*val Token_ID = FirebaseMessaging.getInstance().getToken()
        if (Token_ID != null) {
            ////Log.d("TOKEN_ID", Token_ID.toString())
        }
        val sharedPreferences = applicationContext.getSharedPreferences(
            resources.getString(R.string.FCM_Pref),
            MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(resources.getString(R.string.FCM_TOKEN), Token_ID)
        editor.commit()*/
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                ////Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            Token_ID = task.result

            // //Log and toast
            //val msg = getString(R.string.msg_token_fmt, Token_ID)
            ////Log.d(TAG, msg)
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
        /*val editor = sharedPreferences.edit()
        editor.putString(resources.getString(R.string.FCM_TOKEN), Token_ID)
        editor.commit()*/
    }

    //var messagesSQliteOpenHelper: DatabaseOpenHelper? = null
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val data = remoteMessage.data
        val latabla = data["tabla"].toString()

        Log.d(TAG, "From: " + remoteMessage.from)

        //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Channel_ID", "Messages", importance)
            channel.description = "SynchronizeMessages"
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        //}
        val path: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val newMessageNotification: Notification = NotificationCompat.Builder(
            applicationContext,
            "Channel_ID"
        )
            .setSmallIcon(android.R.mipmap.sym_def_app_icon)
            .setContentTitle(remoteMessage.notification!!.title)
            .setContentText(remoteMessage.notification!!.body)
            .setSound(path)
            .build()
        //Log.d("Notification------>", remoteMessage.notification!!.toString())
        //val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(1, newMessageNotification)
        /*
        * escoge el tipo de tabla y manda a ejecutar la sincronización específica
        * */

        //var ms : MySingleton = MySingleton.getInstance(applicationContext)
        Log.d("---------La Tabla: ", data["tabla"].toString() + " " + latabla)
        sincronizaRemotos(remoteMessage, latabla)
        //when (data["tabla"].toString()){
    }

    private fun sincronizaRemotos(remoteMessage : RemoteMessage, latabla : String) {
        //fun sincronizaRemotos(remoteMessage : RemoteMessage, latabla : String, ms : MySingleton) {

        val data = remoteMessage.data
        //Log.d("--------- Data Size", data.toString())
        var contador = 0

        while (contador < data.size){
            contador += 1
        }

        when (latabla.trim()){
            "actividades" -> {
                        try {
                            val objeto = JSONObject()

                            objeto.put("origen", data["origen"])
                            objeto.put("valueedit", data["valueedit"])
                            objeto.put("valueorig", data["valueorig"])
                            objeto.put("update_user", data["update_user"])
                            objeto.put("insert_user", data["insert_user"])
                            objeto.put("id_actividad", data["id_actividad"])
                            objeto.put("tabla", data["tabla"])
                            objeto.put("observacion", data["observacion"])
                            objeto.put("analog_mrid", data["analog_mrid"])
                            objeto.put("insert_timestamp", data["insert_timestamp"])
                            objeto.put("loctimestamp", data["loctimestamp"])
                            objeto.put("idobservaciones", data["idobservaciones"])

                            insertarActividadesOJ(objeto)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "analog" -> {
                        try {
                                insertarAnalogOJ(JSONObject(data.toString()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "analogvalue_enj" -> {
                        try {
                            val objeto = JSONObject()

                            objeto.put("origen", data["origen"])
                            objeto.put("valueedit", data["valueedit"])
                            objeto.put("valueorig", data["valueorig"])
                            objeto.put("update_user", data["update_user"])
                            objeto.put("insert_user", data["insert_user"])
                            objeto.put("id_actividad", data["id_actividad"])
                            objeto.put("tabla", data["tabla"])
                            objeto.put("observacion", data["observacion"])
                            objeto.put("analog_mrid", data["analog_mrid"])
                            objeto.put("insert_timestamp", data["insert_timestamp"])
                            objeto.put("loctimestamp", data["loctimestamp"])
                            objeto.put("idobservaciones", data["idobservaciones"])

                            insertarAnalogValueOJ(objeto)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "string" -> {
                        try {
                                insertarStringOJ(JSONObject(data.toString()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "stringvalue_enj" -> {
                        try {
                            val objeto = JSONObject()

                            objeto.put("origen", data["origen"])
                            objeto.put("valueedit", data["valueedit"])
                            objeto.put("valueorig", data["valueorig"])
                            objeto.put("update_user", data["update_user"])
                            objeto.put("insert_user", data["insert_user"])
                            objeto.put("id_actividad", data["id_actividad"])
                            objeto.put("tabla", data["tabla"])
                            objeto.put("observacion", data["observacion"])
                            objeto.put("string_mrid", data["string_mrid"])
                            objeto.put("insert_timestamp", data["insert_timestamp"])
                            objeto.put("loctimestamp", data["loctimestamp"])
                            objeto.put("idobservaciones", data["idobservaciones"])

                            insertarStringValueOJ(objeto)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "usuarios" -> {
                        try {
                                insertarUsuariosOJ(JSONObject(data.toString()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "time" -> {
                        try {
                                insertarTimeOJ(JSONObject(data.toString()))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
            "timevalue_enj" -> {
                        try {
                            val objeto = JSONObject()

                            objeto.put("origen", data["origen"])
                            objeto.put("valueedit", data["valueedit"])
                            objeto.put("valueorig", data["valueorig"])
                            objeto.put("update_user", data["update_user"])
                            objeto.put("insert_user", data["insert_user"])
                            objeto.put("id_actividad", data["id_actividad"])
                            objeto.put("tabla", data["tabla"])
                            objeto.put("observacion", data["observacion"])
                            objeto.put("time_mrid", data["time_mrid"])
                            objeto.put("insert_timestamp", data["insert_timestamp"])
                            objeto.put("loctimestamp", data["loctimestamp"])
                            objeto.put("idobservaciones", data["idobservaciones"])
                            insertarTimeValueOJ(objeto)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
            }
        }
    }

    /*fun sincronizaExternos(remoteMessage : RemoteMessage, latabla : String, ms : MySingleton) {

        val data = remoteMessage.data

        Log.d("--------- Data Size", data.toString())

        var contador = 0

        while (contador < data.size){
            contador += 1
        }

        when (latabla.trim()){
            "actividades" -> {
                val synchronizeActividades = StringRequest(Request.Method.POST, SYNCHRONIZEACTIVIDADES_URL,
                    { response ->
                        try {
                            //if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                InsertarActividadesOJ(jo)
                                ////Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            //Toast.makeText( applicationContext, "New Message Recieved", Toast.LENGTH_LONG ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
                            //}
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeActividades)
            }
            "analog" -> {
                val synchronizeAnalog = StringRequest(Request.Method.POST, SYNCHRONIZEANALOG_URL,
                    { response ->
                        try {
//                            if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                InsertarAnalogOJ(jo)
                                //Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            //Toast.makeText( applicationContext, "New Message Recieved", Toast.LENGTH_LONG ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeAnalog)
            }
            "analogvalue_enj" -> {
                val synchronizeAnalogValue = StringRequest(Request.Method.POST, SYNCHRONIZEANALOGVALUE_URL,
                    { response ->
                        try {
                            Log.d("la response ---->>>", response.toString())

//                            if (!response.toString().contains("Connection Error", true)){
                            val jsonObject = JSONObject(response)
                            Log.d("la response synchav ---->>>", jsonObject.toString(2))
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            //val jsonArray = JSONObject().getJSONArray(data.toString())
                            ////Log.d("Remote Message jsonarray --->>> ", jsonArray.toString())
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                Log.d("--------- analogvalue", jo.toString(2))
                                InsertarAnalogValueOJ(jo)
                                ////Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            //Toast.makeText( applicationContext, "AnalogValue Recibido",  Toast.LENGTH_LONG ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeAnalogValue)
            }
            "string" -> {
                val synchronizeString = StringRequest(Request.Method.POST, SYNCHRONIZESTRING_URL,
                    { response ->
                        try {
//                            if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                InsertarStringOJ(jo)
                                ////Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            //oast.makeText( applicationContext, "New Message Recieved", Toast.LENGTH_LONG ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeString)
            }
            "stringvalue_enj" -> {
                val synchronizeStringValue = StringRequest(Request.Method.POST, SYNCHRONIZESTRINGVALUE_URL,
                    { response ->
                        try {
//                            if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                InsertarStringValueOJ(jo)
                                ////Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            //Toast.makeText( applicationContext, "New Message Recieved", Toast.LENGTH_LONG ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeStringValue)
            }
            "usuarios" -> {
                val synchronizeUsuarios = StringRequest(Request.Method.POST, SYNCHRONIZEUSUARIOS_URL,
                    { response ->
                        try {
//                            if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                InsertarUsuariosOJ(jo)
                                //Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            //Toast.makeText( applicationContext, "New Message Recieved", Toast.LENGTH_LONG ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeUsuarios)
            }
            "time" -> {
                val synchronizeTime = StringRequest(Request.Method.POST, SYNCHRONIZETIME_URL,
                    { response ->
                        try {
//                            if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                val jo = jsonArray.getJSONObject(count)
                                InsertarTimeOJ(jo)
                                ////Log.d("Objeto Jason", jo.toString())
                                count++
                            }
                            // Toast.makeText( applicationContext, "New Message Recieved",  Toast.LENGTH_LONG  ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeTime)
            }
            "timevalue_enj" -> {
                //Log.d("Ingresa en timevalue", data["tabla"].toString())
                val synchronizeTimeValue = StringRequest(Request.Method.POST, SYNCHRONIZETIMEVALUE_URL,
                    { response ->
                        try {
//                            if (!response.toString().contains("Connection Error", true)) {
                            val jsonObject = JSONObject(response)
                            val jsonArray = jsonObject.getJSONArray("message_response")
                            var count = 0
                            while (count < jsonArray.length()) {
                                //val jo = jsonArray.getJSONObject(count)
                                //Log.d("Objeto Jason timevalue", jo.toString())
                                //var inserttvalue = InsertarTimeValueOJ(jo)
                                //Log.d("Insertado ", inserttvalue.toString())
                                count++
                            }
                            //Toast.makeText( applicationContext, "New Message Recieved",  Toast.LENGTH_LONG  ).show()
                            //sendBroadcast(Intent(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
//                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error ->
                    //Toast.makeText(applicationContext, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("Error --> ", error.toString())
                }
                ms.addToRequestQueue(synchronizeTimeValue)
            }
        }

    }*/

    /*private fun SaveMessage(title: String, message: String) {
        messagesSQliteOpenHelper = MessagesSQliteOpenHelper(applicationContext)
        val database: SQLiteDatabase = messagesSQliteOpenHelper.getWritableDatabase()
        messagesSQliteOpenHelper.SaveMessage(title, message, database)
        messagesSQliteOpenHelper.close()
    }*/

    /*private fun ExisteMedicion(tipoDato: Int, idactividad: Int, mrid: Int) : Int {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)
        var existe : Int = 0

        when(tipoDato){
            1 -> {
                existe = dbHandler.getAnalogValue(applicationContext, idactividad, mrid)
            }
            4 -> {
                existe = dbHandler.getStringValue(applicationContext, idactividad, mrid)
            }
            5 -> { // fecha

            }
            6 -> { // hora
                existe = dbHandler.getTimeValueObj(mrid, idactividad).TIME_MRID
            }
        }
        return existe
    }*/

    private fun insertarAnalogOJ(oj : JSONObject) : Long{
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)
        val insertado : Long

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************

        val nuevoAnalog : analog = dbHandler.getAnalog(applicationContext, oj.getInt("mrid").toString()) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())

        if (nuevoAnalog.MRID > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            nuevoAnalog.MRID = oj.getInt("mrid")
            nuevoAnalog.Name = oj.getString("name")
            nuevoAnalog.AliasName = oj.getString("aliasname")
            nuevoAnalog.PathName = oj.getString("pathname")
            nuevoAnalog.Description = oj.getString("description")
            nuevoAnalog.MaxValue = oj.getDouble("maxvalue")
            nuevoAnalog.MinValue = oj.getDouble("minvalue")
            nuevoAnalog.IdSistema = oj.getInt("id_sistema")
            insertado = dbHandler.actualizaAnalog(nuevoAnalog)
        } else {
            nuevoAnalog.MRID = oj.getInt("mrid")
            nuevoAnalog.Name = oj.getString("name")
            nuevoAnalog.AliasName = oj.getString("aliasname")
            nuevoAnalog.PathName = oj.getString("pathname")
            nuevoAnalog.Description = oj.getString("description")
            nuevoAnalog.MaxValue = oj.getDouble("maxvalue")
            nuevoAnalog.MinValue = oj.getDouble("minvalue")
            nuevoAnalog.IdSistema = oj.getInt("id_sistema")

            insertado = dbHandler.insertarAnalog(nuevoAnalog)

        }
        return insertado
    }

    private fun insertarAnalogValueOJ(oj : JSONObject) : Long{
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)
        val nuevoAnalog = analogvalue_enj()

        var insertado : Long = 0

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************

        //NuevaLectura = ExisteMedicion(1, oj.getInt("id_actividad"), oj.getInt("analog_mrid"))
        val nuevaLectura : Int =
            dbHandler.getAnalogValue(this.applicationContext, oj.getInt("id_actividad"), oj.getInt("analog_mrid"))

        Log.d("--------- getanalogvalue", nuevaLectura.toString())

        if (nuevaLectura > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            dbHandler.updateSincronizadoAnalogValue(0, oj.getInt("analog_mrid"), oj.getInt("id_actividad"))
        } else {
            nuevoAnalog.ANALOG_MRID = oj.getInt("analog_mrid")
            nuevoAnalog.InsertUser = oj.getString("insert_user")
            nuevoAnalog.Loctimestamp = oj.getString("loctimestamp")
            nuevoAnalog.InsertTimestamp = oj.getString("insert_timestamp")
            nuevoAnalog.ValueOrig = oj.getDouble("valueorig")
            nuevoAnalog.IdActividad = oj.getInt("id_actividad")
            nuevoAnalog.Observacion = oj.getString("observacion")
            nuevoAnalog.IdObservaciones = oj.getInt("idobservaciones")
            nuevoAnalog.Sincronizado = 0
            insertado = dbHandler.insertarAnalogValue(nuevoAnalog)

        }
        return insertado
    }

    private fun insertarStringOJ(oj : JSONObject) : Long {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)

        val insertado : Long

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************
        //var NuevaLectura : Int

        //NuevaLectura = ExisteMedicion(1, oj.getInt("id_actividad"), oj.getInt("analog_mrid")) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())
        val nuevoString : string = dbHandler.getString(applicationContext, oj.getInt("mrid").toString()) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())

        if (nuevoString.MRID > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            nuevoString.MRID = oj.getInt("mrid")
            nuevoString.Name = oj.getString("name")
            nuevoString.AliasName = oj.getString("aliasname")
            nuevoString.PathName = oj.getString("pathname")
            nuevoString.Description = oj.getString("description")
            nuevoString.IdSistema = oj.getInt("id_sistema")
            insertado = dbHandler.actualizaString(nuevoString)
        } else {
            nuevoString.MRID = oj.getInt("mrid")
            nuevoString.Name = oj.getString("name")
            nuevoString.AliasName = oj.getString("aliasname")
            nuevoString.PathName = oj.getString("pathname")
            nuevoString.Description = oj.getString("description")
            nuevoString.IdSistema = oj.getInt("id_sistema")
            insertado = dbHandler.insertarString(nuevoString)
        }

        return insertado
    }

    private fun insertarStringValueOJ(oj : JSONObject) : Long {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)
        val nuevoString = stringvalue_enj()

        var insertado : Long = 0

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************

        //NuevaLectura = ExisteMedicion(4, oj.getInt("id_actividad"), oj.getInt("string_mrid"))
        val nuevaLectura =
            dbHandler.getStringValue(this.applicationContext, oj.getInt("id_actividad"), oj.getInt("string_mrid"))

        if (nuevaLectura > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            dbHandler.updateSincronizadoStringValue(0, oj.getInt("string_mrid"), oj.getInt("id_actividad"))
        } else {
            nuevoString.STRING_MRID = oj.getInt("string_mrid")
            nuevoString.InsertUser = oj.getString("insert_user")
            nuevoString.Loctimestamp = oj.getString("loctimestamp")
            nuevoString.InsertTimestamp = oj.getString("insert_timestamp")
            nuevoString.ValueOrig = oj.getString("valueorig")
            nuevoString.IdActividad = oj.getInt("id_actividad")
            nuevoString.Observacion = oj.getString("observacion")
            nuevoString.IdObservaciones = oj.getInt("idobservaciones")
            nuevoString.Sincronizado = 0
            insertado = dbHandler.insertarStringValue(nuevoString)

        }
        return insertado
    }

    private fun insertarTimeOJ(oj : JSONObject) : Long {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)

        val insertado : Long

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************
        //var NuevaLectura : Int

        //NuevaLectura = ExisteMedicion(1, oj.getInt("id_actividad"), oj.getInt("analog_mrid")) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())
        val nuevoTime : time = dbHandler.getTime( oj.getInt("mrid").toString()) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())

        if (nuevoTime.MRID > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            nuevoTime.MRID = oj.getInt("mrid")
            nuevoTime.Name = oj.getString("name")
            nuevoTime.AliasName = oj.getString("aliasname")
            nuevoTime.PathName = oj.getString("pathname")
            nuevoTime.Description = oj.getString("description")
            nuevoTime.IdSistema = oj.getInt("id_sistema")
            insertado = dbHandler.actualizaTime(nuevoTime)
        } else {
            nuevoTime.MRID = oj.getInt("mrid")
            nuevoTime.Name = oj.getString("name")
            nuevoTime.AliasName = oj.getString("aliasname")
            nuevoTime.PathName = oj.getString("pathname")
            nuevoTime.Description = oj.getString("description")
            nuevoTime.IdSistema = oj.getInt("id_sistema")
            insertado = dbHandler.insertarTime(nuevoTime)
        }

        return insertado
    }

    private fun insertarTimeValueOJ(oj : JSONObject) : Long {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)
        val nuevoTimeValue = timevalue_enj()

        var insertado : Long = 0

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************

        //NuevaLectura = ExisteMedicion(6, oj.getInt("id_actividad"), oj.getInt("time_mrid")) //
        val nuevaLectura =
            dbHandler.getTimeValue(applicationContext, oj.getInt("id_actividad"), oj.getInt("time_mrid")) //.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())
        Log.d("InsertarTimveAnalog - Existe medicion", oj.getInt("time_mrid").toString() + " .- " + nuevaLectura.toString())
        if (nuevaLectura > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            dbHandler.updateSincronizadoTimeValue(0, oj.getInt("time_mrid"), oj.getInt("id_actividad"))

        } else {
            nuevoTimeValue.TIME_MRID = oj.getInt("time_mrid")
            nuevoTimeValue.InsertUser = oj.getString("insert_user")
            nuevoTimeValue.Loctimestamp = oj.getString("loctimestamp")
            nuevoTimeValue.InsertTimestamp = oj.getString("insert_timestamp")
            nuevoTimeValue.ValueOrig = oj.getString("valueorig")
            nuevoTimeValue.ValueEdit = oj.getString("valueorig")
            nuevoTimeValue.IdActividad = oj.getInt("id_actividad")
            nuevoTimeValue.Sincronizado = 0
            insertado = dbHandler.insertarTimeValue(nuevoTimeValue)
        }

        //Log.d("timevalue insertado ", insertado.toString())
        return insertado
    }

    private fun insertarActividadesOJ(oj : JSONObject) : Long {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)

        val insertado : Long

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************
        //var NuevaLectura : Int

        //NuevaLectura = ExisteMedicion(1, oj.getInt("id_actividad"), oj.getInt("analog_mrid")) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())
        val nuevoActividad = dbHandler.getActividadId( oj.getInt("id")) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())

        if (nuevoActividad.id!! > 0) {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            nuevoActividad.fecha = oj.getString("fecha")
            nuevoActividad.horainicio = oj.getString("horainicio")
            nuevoActividad.horafin = oj.getString("horafin")
            nuevoActividad.id_sistema = oj.getInt("id_sistema")
            nuevoActividad.idplanificacion_ipt = oj.getInt("ipt")
            nuevoActividad.operador = oj.getString("operador")
            insertado = dbHandler.actualizaActividad(nuevoActividad)
        } else {
            nuevoActividad.fecha = oj.getString("fecha")
            nuevoActividad.horainicio = oj.getString("horainicio")
            nuevoActividad.horafin = oj.getString("horafin")
            nuevoActividad.id_sistema = oj.getInt("id_sistema")
            nuevoActividad.idplanificacion_ipt = oj.getInt("ipt")
            nuevoActividad.operador = oj.getString("operador")
            insertado = dbHandler.insertarActividad(nuevoActividad)
        }

        return insertado
    }

    private fun insertarUsuariosOJ(oj : JSONObject) : Long {
        val dbHandler = DatabaseOpenHelper(applicationContext, null, null, 1)

        val insertado : Long

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************
        //var NuevaLectura : Int

        //NuevaLectura = ExisteMedicion(1, oj.getInt("id_actividad"), oj.getInt("analog_mrid")) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())
        val nuevoUsuario = dbHandler.getUsuario( oj.getString("usuario").toString()) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())

        if (nuevoUsuario.usuario != "") {
            /*Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            HabilitaBotones(NuevaLectura)*/
            nuevoUsuario.usuario = oj.getString("usuario")
            nuevoUsuario.idCentral = oj.getInt("id_central")
            nuevoUsuario.idPerfil = oj.getInt("id_perfil")
            nuevoUsuario.correo = oj.getString("correo")
            insertado = dbHandler.actualizaUsuario(nuevoUsuario)
        } else {
            nuevoUsuario.usuario = oj.getString("usuario")
            nuevoUsuario.idCentral = oj.getInt("id_central")
            nuevoUsuario.idPerfil = oj.getInt("id_perfil")
            nuevoUsuario.correo = oj.getString("correo")
            insertado = dbHandler.insertarUsuario(nuevoUsuario)
        }

        return insertado
    }

}