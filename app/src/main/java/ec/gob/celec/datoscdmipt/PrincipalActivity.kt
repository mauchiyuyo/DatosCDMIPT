package ec.gob.celec.datoscdmipt

//import kotlinx.android.synthetic.main.activity_principal.*
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.instacart.library.truetime.TrueTime
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.exception.MsalException
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.*
import ec.gob.celec.datoscdmipt.fragmentos.GraficoFragment
import ec.gob.celec.datoscdmipt.fragmentos.SplashnfcFragment
import ec.gob.celec.datoscdmipt.fragmentos.TablaValoresFragment
import ec.gob.celec.datoscdmipt.helpers.CheckNetworkConnection
import ec.gob.celec.datoscdmipt.login.LoginADActivity
import ec.gob.celec.datoscdmipt.nfc.EditornfcActivity
import ec.gob.celec.datoscdmipt.nfc.MIME_TEXT_PLAIN
import ec.gob.celec.datoscdmipt.reporteria.ReporteriaActivity
import ec.gob.celec.datoscdmipt.sincronizador.MySingleton
import ec.gob.celec.datoscdmipt.sincronizador.NetworkStateChecker
import kotlinx.coroutines.Runnable
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("DEPRECATION")
class PrincipalActivity : AppCompatActivity() {

    private lateinit var checkNetworkConnection : CheckNetworkConnection
    //private lateinit var clock : TrueTime // = ParallelClock.getInstance()
    //private lateinit var nsc : NetworkStateChecker

    companion object {
        lateinit var dbHandler : DatabaseOpenHelper
        lateinit var nfcAdapter: NfcAdapter
        lateinit var ms : MySingleton
        //lateinit var laCuenta : IAccount
        //const val IP_SERVER = "172.16.231.6"
        private const val WEB_SERVER = "http://172.16.231.6"
        var elementosJSON = JSONArray()
        var itemElementosJSON : Int = 0
        var actividad = actividades()
        var actualMRID : String = ""
        var tipoElemento : Int = 0
        var valorUsuario : String = ""
        var buildId : String = ""
        var valorPerfil : Int = 0
        var valorCentral : Int = 0
        var posicionSelCBO : Int = 0
        //var TOKEN_ID_URL : String = "http://$IP_SERVER:81/DatosCdM/sendtokenid.php"
        var TOKEN_ID_URL : String = "$WEB_SERVER/DatosCdM/sendtokenid.php"
        var broadcastReceiver: BroadcastReceiver? = null
        //private const val TAG = "PrincipalActivity"
        var loctimestampglobal = ""
        private var mNfcReadFragment : SplashnfcFragment ? = null
        private lateinit var handler: Handler
    }

    init {
        dbHandler = DatabaseOpenHelper(this, null, null, 1)
        buildId = dbHandler.getDeviceId().toString()

        Thread {
            try {
                TrueTime.build().withNtpHost("time.google.com").withLoggingEnabled(false)
                    .withConnectionTimeout(31428).initialize()
            } catch (var2: IOException) {
                var2.printStackTrace()
            }
        }.start()

    }

    override fun onStart() {
        super.onStart()
        registerReceiver(broadcastReceiver, IntentFilter(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
    }

    @Deprecated("Bandera descontinuada FLAG_FULLSCREEN")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView( R.layout.activity_principal )

        checkNetworkConnection = CheckNetworkConnection(application)
        checkNetworkConnection.observe(this) { isConnected ->
            if (isConnected) {
                Log.d("CheckNetworkConnection---", "verdadero")
            } else {
                Log.d("CheckNetworkConnection---", "falso")
            }
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_principal)

        val txtSubTitulo = findViewById<TextView>(R.id.txtSubTitulo)
        val txtActividad = findViewById<TextView>(R.id.txtActividad)
        //var txtValnuevo = findViewById<EditText>(R.id.txtValNuevo)
        val cmdGrabarValor = findViewById<Button>(R.id.cmdGrabarValor)
        val cmdSiguienteElemento = findViewById<Button>(R.id.cmdSiguienteElemento)
        val cmdAnteriorElemento = findViewById<Button>(R.id.cmdAnteriorElemento)
        val cmdValores24 = findViewById<Button>(R.id.cmdValores24)
        val cmdValores72 = findViewById<Button>(R.id.cmdValores72)
        val cmdValoresHis = findViewById<Button>(R.id.cmdValoresHis)
        val spSelCBO = findViewById<Spinner>(R.id.spSELCBO)
        val cmdLeerNFC = findViewById<Button>(R.id.cmdLeerNFC)
        val cmdSincronizacion = findViewById<Button>(R.id.cmdSincronizar)
        val cmdActivarElemento = findViewById<Button>(R.id.cmdActivarElemento)
        val txtHoraActual = findViewById<TextView>(R.id.txtHoraActual)
        val preferencias : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if(preferencias.getString("elUsuario",null).isNullOrBlank()){
            try{
                //var nsc = NetworkStateChecker()
                //if(nsc.isNetworkAvailable(applicationContext)){
                    val actividadLogin = Intent(this, LoginADActivity::class.java)
                    startActivity(actividadLogin)
                    Log.d("****** Error en login", "Error al iniciar ventana")
                    Toast.makeText(applicationContext, "Principal Error al ejecutar el Login", Toast.LENGTH_SHORT).show()

                //} else {
                //}
            }
            catch (e: Exception){
                e.printStackTrace()
                Toast.makeText(applicationContext, "Catch Error al ejecutar el Login", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        else {
            if(!intent.hasExtra("Usuario") || !intent.getStringExtra("Usuario").equals(preferencias.getString("elUsuario", null)) ){
                intent.putExtra("Usuario", preferencias.getString("elUsuario",null))
            }
            if(!intent.hasExtra("Perfil") || intent.getIntExtra("Perfil",1) != preferencias.getInt("Perfil", 0)){
                intent.putExtra("Perfil", preferencias.getInt("Perfil",0))
            }
            if(!intent.hasExtra("Central") || intent.getIntExtra("Central", 0) != preferencias.getInt("Central", 0)){
                intent.putExtra("Central", preferencias.getInt("Central",0))
            }
            valorUsuario = intent.getStringExtra("Usuario").toString()
            valorPerfil = intent.getIntExtra("Perfil", 1)
            valorCentral = intent.getIntExtra("Central", 650)
            //laCuenta = LoginADActivity.laCuenta

            this.title = "DatosCdM-IPT - Bienvenido: $valorUsuario - $buildId"
        }

        //obtieneHoraNTP()
        //clock.initialize()
        //val policy = ThreadPolicy.Builder().permitAll().build()
        //StrictMode.setThreadPolicy(policy)

        //TrueTime.build().initialize()

        val updater = object : Runnable{
            override fun run() {
                val time = getTrueTimeD().toString()
                loctimestampglobal = time
                txtHoraActual.text = loctimestampglobal

                handler.postDelayed(this, 1000)
            }
        }
        handler = Handler()
        handler.post(updater)

        nfcAdapter = NfcAdapter.getDefaultAdapter(applicationContext)

        if(nfcAdapter.isEnabled){
            //Toast.makeText(applicationContext, "NFC Habilitado", Toast.LENGTH_LONG).show()
        }
        else{
            Toast.makeText(applicationContext, "NFC No Habilitado", Toast.LENGTH_LONG).show()
            finish()
        }
        // preparación de NFC
        if (!dbHandler.verificaDB(applicationContext)) finish()
        dbHandler.openDatabase()

        /*if(valorPerfil == 1 ){
            //cmdEditorNFC.isEnabled = true
        }*/

        // obtiene la hora del servidor ntp
        //actualizaTiempo()

        actividad = obtenerActidadDeAhora()
        //Log.d("actividad en principal", actividad.id.toString() + " " + actividad.fecha + " " + actividad.horainicio )
        val texto = "Fecha: ${actividad.fecha} Hora: ${actividad.horainicio}"
        txtActividad.text = texto
        mostrarNFCLogo()
        //txtValnuevo.requestFocus()

        habilitaBotones(10, 1)

        //val androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID)
        //val androidID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        //val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(this)
        //val androidID = adInfo.id

        //Log.d("********* android_id...", androidID.toString())
        // TODO: revisar el uso de android_id en vez de buidlid

        cmdGrabarValor.setOnClickListener {
            when(elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")){
                1 -> {
                    insertarAnalogValue()
                }
                4 -> {
                    insertarStringValue()
                }
            }
        }

        cmdSiguienteElemento.setOnClickListener{
            if(itemElementosJSON < elementosJSON.length() - 1  && itemElementosJSON >= 0) {
                itemElementosJSON += 1
                actualMRID =  elementosJSON.getJSONObject(itemElementosJSON).opt("ID")!!.toString()//inMessage.substring(3)
                txtSubTitulo.text = elementosJSON.getJSONObject(itemElementosJSON).opt("ID")!!.toString()//inMessage.substring(3)

                val tipoDato = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
                //var existeMedicion = ExisteMedicion(tipoDato)

                buscarDatos(actualMRID, 24)
                //verificarHabilitado(actualMRID)
                habilitaBotones(existeMedicion(tipoDato, actividad.id!!, actualMRID.toInt()),verificarHabilitado(actualMRID))
            }
            else{
                //mostrarNFCLogo()
                cmdLeerNFC.isEnabled = true
                cmdLeerNFC.setBackgroundColor(Color.BLUE)
                return@setOnClickListener
            }
        }

        cmdAnteriorElemento.setOnClickListener {
            if(itemElementosJSON <= elementosJSON.length() && itemElementosJSON > 0) {
                itemElementosJSON -= 1
                //cmdSiguienteElemento.isEnabled = true
                actualMRID =  elementosJSON.getJSONObject(itemElementosJSON).opt("ID")!!.toString()//inMessage.substring(3)
                txtSubTitulo.text = elementosJSON.getJSONObject(itemElementosJSON).opt("ID")!!.toString()//inMessage.substring(3)
                val tipoDato = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
                buscarDatos(actualMRID, 24)
                habilitaBotones(existeMedicion(tipoDato, actividad.id!!, actualMRID.toInt()),verificarHabilitado(actualMRID))
            }
            else{
                //cmdAnteriorElemento.isEnabled = false
                mostrarNFCLogo()
                cmdLeerNFC.isEnabled = true
                return@setOnClickListener
            }
        }

        cmdValores24.setOnClickListener {
            val tipoDato : Int = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
            if(actualMRID != ""){
                buscarDatos(actualMRID, 24)
                habilitaBotones(existeMedicion(tipoDato, actividad.id!!, actualMRID.toInt()),verificarHabilitado(actualMRID))
            }
        }

        cmdValores72.setOnClickListener {
            val tipoDato : Int = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
            buscarDatos(actualMRID, 72)
            habilitaBotones(existeMedicion(tipoDato, actividad.id!!, actualMRID.toInt()),verificarHabilitado(actualMRID))
        }

        cmdValoresHis.setOnClickListener {
            val tipoDato : Int = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
            buscarDatos(actualMRID, 30)
            habilitaBotones(existeMedicion(tipoDato, actividad.id!!, actualMRID.toInt()),verificarHabilitado(actualMRID))
        }

        spSelCBO.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //var sistema = dbHandler.getSistema(position)
                //var valorselcbo : String = spSelCBO.getItemAtPosition(position).toString()//.substringBefore(" -").toInt()
                posicionSelCBO = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                posicionSelCBO = 0
            }
        }

        cmdLeerNFC.setOnClickListener {
            mostrarNFCLogo()
            return@setOnClickListener
        }

        cmdSincronizacion.setOnClickListener{

            val nst = NetworkStateChecker()

            iniciarSincronizador()
            recibirMensajes()

            val contador: Int
            //contador = nst.synchroManual(applicationContext, contador)
            contador = nst.synchroManual(applicationContext)
            //confirmarSincronizaion(contador)
            Toast.makeText(
                applicationContext,
                "Sincronizados...$contador",
                Toast.LENGTH_SHORT
            ).show()

        }

        /////////////////////////////////

        /*MainScope().launch {
            withContext(Dispatchers.Default){
                var nsc = NetworkStateChecker()
                //Log.d("------- servidor disponible ", nsc.isReachableByTcp(IP_SERVER, 3306, 5000).toString())
            }
        }*/

        cmdActivarElemento.setOnClickListener{
            try{
                var titulo = ""
                var pregunta = ""
                var text = ""
                //val factory = LayoutInflater.from(this)
                //val dialogo_observacion: View = factory.inflate(R.layout.dialogo_observacion, null)
                //val inflater : factory.inflate(R.layout.dialogo_observacion, null)

                val txtLaObservacion = EditText(this)
                //txtLaObservacion.inputType = InputType.TYPE_CLASS_TEXT
                txtLaObservacion.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                txtLaObservacion.minLines = 3
                txtLaObservacion.maxLines = 4
                txtLaObservacion.setLines(3)

                val builder = AlertDialog.Builder(this@PrincipalActivity)
                when(this.elementoActivo(actualMRID, tipoElemento)){
                    0 -> {
                        // habilitado
                        titulo = "Deshabilitar elemento"
                        pregunta = "¿Estás seguro de deshabilitar el elemento?"
                        text = "Deshabilitando elemento... "
                    }
                    1 -> {
                        // deshabilitado
                        titulo = "Habilitar elemento"
                        pregunta = "¿Estás seguro de deshabilitar el elemento?"
                        text = "Habilitando elemento... "
                    }
                }
                builder.setTitle(titulo)
                builder.setMessage(pregunta)
                builder.setView(txtLaObservacion)
                    .setPositiveButton(
                        "Sí"
                    ) { _, _ ->
                        Toast.makeText(
                            applicationContext,
                            text,
                            Toast.LENGTH_SHORT
                        ).show()
                        when(this.elementoActivo(actualMRID, tipoElemento)){
                            0 -> {
                                // habilitado
                                Log.d("El texto: ", txtLaObservacion.text.toString())
                                this.deshabilitaDatabase(actualMRID, tipoElemento)
                            }
                            1 -> {
                                // deshabilitado
                                this.habilitaDatabase(actualMRID, tipoElemento)
                            }
                        }
                        //dbHandler.copyDatabase(this)
                    }
                    .setNegativeButton(
                        android.R.string.cancel
                    ) { dialog, _ ->
                        Toast.makeText(applicationContext, "Cancel...", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCurrentTrueTime(): Long {
        var trueDate: Date? = null
        if (TrueTime.isInitialized()) {
            trueDate = TrueTime.now()
        }
        return trueDate?.time ?: System.currentTimeMillis()
    }

    fun getTrueTime(): Long? {
        var trueDate: Date? = null
        if (TrueTime.isInitialized()) {
            trueDate = TrueTime.now()
        }
        return trueDate?.time
    }

    fun getTrueTimeD(): Date? {
        var trueDate: Date? = null
        if (TrueTime.isInitialized()) {
            trueDate = TrueTime.now()
        }
        return trueDate
    }

    private fun iniciarSincronizador(){
        //var sharedPreferences = getSharedPreferences(resources.getString(R.string.FCM_Pref), MODE_PRIVATE)//"FCM", Context.MODE_PRIVATE        )
        var tokenID : String//= sharedPreferences.getString(resources.getString(R.string.FCM_TOKEN), null)
        var sendTokenID : StringRequest

        Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                //Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            tokenID = task.result
            //Log.d("TASK", task.result.length.toString())
            sendTokenID = object : StringRequest(
                Method.POST, TOKEN_ID_URL,
                Response.Listener {
                    //Toast.makeText(this@PrincipalActivity, response, Toast.LENGTH_SHORT).show()
                    //Log.d("Listener", response)
                },
                Response.ErrorListener { error ->
                    //Toast.makeText(this@PrincipalActivity, error.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("ErrListener", error.toString())
                }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val param: MutableMap<String, String> = HashMap()
                    param["tokenid"] = tokenID
                    return param
                }
            }

            ms = MySingleton.getInstance(this@PrincipalActivity)
            ms.addToRequestQueue<String>(sendTokenID)

            // Log and toast
            //val msg = getString(R.string.msg_token_fmt, token_id)
            //Log.d(TAG, msg)
            //Log.d("SendTokenID", sendTokenID.toString())
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun recibirMensajes(){
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(NetworkStateChecker(), filter)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if(actualMRID.trim().isNotEmpty()) {
                    //buscarDatos(actualMRID, 24)
                    //habilitaBotones(existeMedicion(tipoElemento, actividad.id!!, actualMRID.toInt()))
                    //TODO verificar necesidad
                    Log.d("","")
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter(DatabaseOpenHelper.UI_SYNCHRONIZE_SQLITE))
    }

    private fun habilitaBotones(graba: Int, estado: Int){
        val cmdGrabarValor = findViewById<Button>(R.id.cmdGrabarValor)
        val cmdSiguienteElemento = findViewById<Button>(R.id.cmdSiguienteElemento)
        val cmdAnteriorElemento = findViewById<Button>(R.id.cmdAnteriorElemento)
        val cmd24horas = findViewById<Button>(R.id.cmdValores24)
        val cmd72horas = findViewById<Button>(R.id.cmdValores72)
        val cmdHis = findViewById<Button>(R.id.cmdValoresHis)
        val txtValNuevo = findViewById<EditText>(R.id.txtValNuevo)
        val spSelCBO = findViewById<Spinner>(R.id.spSELCBO)
        val txtActividad = findViewById<TextView>(R.id.txtActividad)

        actividad = obtenerActidadDeAhora()
        val texto = "Fecha: ${actividad.fecha} Hora: ${actividad.horainicio}"
        txtActividad.text = texto

        if(estado != 1){
            cmdGrabarValor.isEnabled = false
            cmdGrabarValor.setBackgroundColor(Color.DKGRAY)
            if(itemElementosJSON > 0) {
                cmdAnteriorElemento.isEnabled = true
                cmdAnteriorElemento.setBackgroundColor(Color.BLUE)
            }
            if(itemElementosJSON < elementosJSON.length()) {
                cmdSiguienteElemento.isEnabled = true
                cmdSiguienteElemento.setBackgroundColor(Color.BLUE)
            }
            txtValNuevo.isEnabled = false
            spSelCBO.isEnabled = false
        } else {
            when(graba){
                0 -> {
                    cmdGrabarValor.isEnabled = true
                    cmdGrabarValor.setBackgroundColor(Color.BLUE)
                    cmdAnteriorElemento.isEnabled = false
                    cmdAnteriorElemento.setBackgroundColor(Color.DKGRAY)
                    cmdSiguienteElemento.isEnabled = false
                    cmdSiguienteElemento.setBackgroundColor(Color.DKGRAY)

                    if (findViewById<TextInputLayout>(R.id.txtValNuevoLy).isGone) {
                        spSelCBO.requestFocus()
                    } else {
                        txtValNuevo.text.clear()
                        txtValNuevo.requestFocus()
                    }
                    txtValNuevo.isEnabled = true
                    spSelCBO.isEnabled = true
                }
                10 -> {
                    cmdGrabarValor.isEnabled = false
                    cmdGrabarValor.setBackgroundColor(Color.DKGRAY)
                    cmdAnteriorElemento.isEnabled = false
                    cmdAnteriorElemento.setBackgroundColor(Color.DKGRAY)
                    cmdSiguienteElemento.isEnabled = false
                    cmdSiguienteElemento.setBackgroundColor(Color.DKGRAY)
                    cmd24horas.isEnabled = false
                    cmd24horas.setBackgroundColor(Color.DKGRAY)
                    cmd72horas.isEnabled = false
                    cmd72horas.setBackgroundColor(Color.DKGRAY)
                    cmdHis.isEnabled = false
                    cmdHis.setBackgroundColor(Color.DKGRAY)
                }
                11 -> {
                    cmd24horas.isEnabled = true
                    cmd24horas.setBackgroundColor(Color.BLUE)
                    cmd72horas.isEnabled = true
                    cmd72horas.setBackgroundColor(Color.BLUE)
                    cmdHis.isEnabled = true
                    cmdHis.setBackgroundColor(Color.BLUE)
                }
                else -> {
                    cmdGrabarValor.isEnabled = false
                    cmdGrabarValor.setBackgroundColor(Color.DKGRAY)
                    if(itemElementosJSON > 0) {
                        cmdAnteriorElemento.isEnabled = true
                        cmdAnteriorElemento.setBackgroundColor(Color.BLUE)
                    }
                    if(itemElementosJSON < elementosJSON.length()) {
                        cmdSiguienteElemento.isEnabled = true
                        cmdSiguienteElemento.setBackgroundColor(Color.BLUE)
                    }
                    txtValNuevo.isEnabled = false
                    spSelCBO.isEnabled = false
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (valorPerfil == 1){
            val itemmenu : MenuItem = menu!!.findItem(R.id.mnuEditorNFC)
            itemmenu.isEnabled = true
            itemmenu.isVisible = true
            val itemmenur : MenuItem = menu.findItem(R.id.mnuRestoreDB)
            itemmenur.isEnabled = true
            itemmenur.isVisible = true
            val itemmenubk : MenuItem = menu.findItem(R.id.mnuBackupDB)
            itemmenubk.isEnabled = true
            itemmenubk.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mnuIPT -> {
                try {
                    val actividadIPT = Intent(this, IptActivity::class.java)
                    actividadIPT.putExtra("Usuario", intent.getStringExtra("Usuario"))
                    actividadIPT.putExtra("Perfil", intent.getIntExtra("Perfil", 1))
                    actividadIPT.putExtra("Central", intent.getIntExtra("Central", 650))
                    startActivity(actividadIPT)
                } catch (e: Exception) {
                    e.printStackTrace()
//                //Log.w("MainActivity", "Error en botón editornfc")
                }
            }
            R.id.mnuReportes -> {
                try {
                    val actividadReporteria = Intent(this, ReporteriaActivity::class.java)
                    actividadReporteria.putExtra("Usuario", intent.getStringExtra("Usuario"))
                    actividadReporteria.putExtra("Perfil", intent.getIntExtra("Perfil", 1))
                    actividadReporteria.putExtra("Central", intent.getIntExtra("Central", 650))
                    startActivity(actividadReporteria)
                } catch (e: Exception) {
                    e.printStackTrace()
//                //Log.w("MainActivity", "Error en botón editornfc")
                }
            }
            R.id.mnuEditorNFC -> {
                try {
                    val actividadEditorNFC = Intent(this, EditornfcActivity::class.java)
                    //val actividadEditorNFC = Intent(this, EditorEtiquetasNFCActivity::class.java)
                    actividadEditorNFC.putExtra("Usuario", intent.getStringExtra("Usuario"))
                    actividadEditorNFC.putExtra("Perfil", intent.getIntExtra("Perfil", 1))
                    actividadEditorNFC.putExtra("Central", intent.getIntExtra("Central", 650))
                    startActivity(actividadEditorNFC)
                } catch (e: Exception) {
                    e.printStackTrace()
//                //Log.w("MainActivity", "Error en botón editornfc")
                }
            }
            R.id.mnuRestoreDB -> {
                try {
                    confirmarRestauracion()
                } catch (e: Exception) {
                    e.printStackTrace()
                    //Log.w("###Principal -", "Error en Copia de restauración de BD")
                }
            }
            R.id.mnuBackupDB -> {
                try{
                    copyFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.mnuLogout -> {
                confirmarSalida()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmarRestauracion(){
        try{
            val builder =
                AlertDialog.Builder(this@PrincipalActivity)
            builder.setTitle("Restaurar Base de Datos")
            builder.setMessage("¿Quieres restaurar la BD a su estado inicial?")
                .setPositiveButton(
                    "Sí"
                ) { _, _ ->
                    Toast.makeText(
                        applicationContext,
                        "Eliminamos datos...",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.deleteDatabase(dbHandler.nameDatabase)
                    dbHandler.copyDatabase(this)
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ ->
                    Toast.makeText(applicationContext, "Cancel...", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun copyFile() {
        try {
            val sd: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formatted = current.format(formatter)
            val backupDBPath = "datoscentral-${formatted}.db"

            val data: File = Environment.getDataDirectory()
            var copiado : Long = 0
            if (sd.canWrite()) {
                val currentDBPath = "/data/ec.gob.celec.datoscdmipt/databases/datoscentral.db"
                //val currentDBPath = "/data/user/0/ec.gob.celec.datoscdmipt/databases/datoscentral.db"
                val currentDB = File(data, currentDBPath)
                val backupDB = File(sd, backupDBPath)
                if (currentDB.exists()) {
                    val src: FileChannel = FileInputStream(currentDB).channel
                    val dst: FileChannel = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size()).also { copiado = it }
                    src.close()
                    dst.close()
                } else {
                    Log.d("-------- copiado sin copiar", "false")
                }
                if (copiado > 0) {
                    Toast.makeText(applicationContext, "Backup Complete", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("-------- sd no puede escribir", formatted)
            }
        } catch (e: java.lang.Exception) {
            Log.w("Settings Backup", e)
        }
    }

    private fun confirmarSalida(){
        try{
            val builder =
                AlertDialog.Builder(this@PrincipalActivity)
            builder.setTitle("Salir del sistema")
            builder.setMessage("¿Quieres salir del sistema?")
                .setPositiveButton(
                    "Sí"
                ) { _, _ ->
                    //val intent = Intent(applicationContext, LoginADActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                    val preferencias : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

                    val prefEditor = preferencias.edit()
                    prefEditor.remove("elUsuario")
                    prefEditor.apply()

                    if(LoginADActivity.mSingleAccountApp != null) {
                        LoginADActivity.mSingleAccountApp!!.signOut(object :
                            ISingleAccountPublicClientApplication.SignOutCallback {
                            override fun onSignOut() {
                                LoginADActivity.updateUI(null)
                                LoginADActivity.performOperationOnSignOut()
                                finish()
                                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                //startActivity(intent)
                            }
                            override fun onError(exception: MsalException) {
                                //displayError(exception)
                            }
                        })
                        dbHandler.closeDatabase()
                    } else {
                        finish()
                    }

                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ ->
                    Toast.makeText(applicationContext, "Cancel...", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /*fun confirmarSincronizaion(cuenta : Int) {
        try {
            val builder =
                AlertDialog.Builder(applicationContext)
            builder.setTitle("Sincronización de datos")
            builder.setMessage("Datos sincronizados: " + cuenta.toString())
                .setPositiveButton(
                    "Aceptar"
                ) { _, _ ->
                    Toast.makeText(
                        applicationContext,
                        "Sincronizados...",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                .setCancelable(false)
                .show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }*/

    @SuppressLint("SetTextI18n")
    private fun buscarDatos(actualMRID: String, periodo: Int){
        val database : File = applicationContext.getDatabasePath(dbHandler.nameDatabase)
        val txtSubTitulo = findViewById<TextView>(R.id.txtSubTitulo)
        val txtValMinimo = findViewById<TextView>(R.id.txtValMinimo)
        val txtValMaximo = findViewById<TextView>(R.id.txtValMaximo)
        val txtValNuevo = findViewById<EditText>(R.id.txtValNuevo)
        //val txtValNuevoLy = findViewById<TextInputLayout>(R.id.txtValNuevoLy)
        //val txtStringLy = findViewById<TextInputLayout>(R.id.txtStringLy)
        val lyLimites = findViewById<LinearLayout>(R.id.lyLimites)
        val lyLectura = findViewById<LinearLayout>(R.id.lyLectura)
        //val txtString = findViewById<EditText>(R.id.txtString)
        val spSELCBO = findViewById<Spinner>(R.id.spSELCBO)
        val txtUnidades = findViewById<TextView>(R.id.txtValUnidades)
        val lySpSelCBO = findViewById<LinearLayout>(R.id.lySpSelCBO)
        val txtEtiquetaSelCBO = findViewById<TextView>(R.id.txtEtiquetaSelCBO)

        if (!database.exists()){
            dbHandler.readableDatabase
            if(dbHandler.copyDatabase(this)){
                Toast.makeText(applicationContext, "BD Copiada", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(
                    applicationContext,
                    "Error en la Copia de BD",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        try {
            //var MRID : CharSequence = txtIdAnalog.getText()
            val elanalog : analog
            val elstring : string
            //var laActividad = actividades()
            //var sistema = sistemas()
            val txtNroEtiquetas = findViewById<TextView>(R.id.txtNroEtiquetas)

            var texto = "Elemento ${itemElementosJSON + 1} de ${elementosJSON.length()} en etiqueta"
            txtNroEtiquetas.text = texto

            tipoElemento = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")

            when(tipoElemento){
                1 -> {
                    // carga fragmento de analógicos
                    elanalog = verAnalogValues(actualMRID, periodo)//MRID.toString())
                    lyLectura.isVisible = true
                    //spSELCBO.isGone = true
                    lySpSelCBO.isGone = true
                    lyLimites.isVisible = true
                    txtValMaximo.text = "Máximo: " + elanalog.MaxValue.toString()
                    txtValMinimo.text = "Mínimo: " + elanalog.MinValue.toString()
                    texto = elanalog.MRID.toString() + " - " + elanalog.Description
                    txtSubTitulo.text = texto
                    txtUnidades.text = elanalog.Unidades
                    txtValNuevo.hint = elanalog.Description
                    txtValNuevo.requestFocus()
                    habilitaBotones(
                        existeMedicion(
                            elanalog.IdTipo,
                            actividad.id!!,
                            Companion.actualMRID.toInt()
                        ),verificarHabilitado(Companion.actualMRID)
                    )
                }
                4 -> {
                    // carga fragmento de String
                    // si tiene selcbo carga el combo
                    // sin selcbo carga cuadro de texto
                    elstring = verStringValues(actualMRID, periodo)
                    //txtValMaximo.setText("0.0 ") //elanalog.MaxValue.toString()
                    //txtValMinimo.setText("0.0 ") //elanalog.MinValue.toString()
                    texto = elstring.MRID.toString() + " - " + elstring.Description
                    txtSubTitulo.text = texto
                    lyLectura.isGone = true
                    //spSELCBO.isVisible = true
                    lySpSelCBO.isVisible = true
                    //txtStringLy.isGone = true
                    lyLimites.isGone = true
                    spSELCBO.requestFocus()
                    // llena el spinner

                    texto = "Opciones: " + llenarSpinnerElementos(elstring.SELCBO)
                    txtEtiquetaSelCBO.text = texto
                    habilitaBotones(
                        existeMedicion(
                            elstring.IdTipo,
                            actividad.id!!,
                            Companion.actualMRID.toInt()
                        ),verificarHabilitado(Companion.actualMRID)
                    )
                }
                else -> {
                    txtValMaximo.text = "Máximo: 0.0" //elanalog.MaxValue.toString()
                    txtValMinimo.text = "Mínimo: 0.0" //elanalog.MinValue.toString()
                    txtSubTitulo.text = "Sin descripción - Sin datos"
                }

            }
        }
        catch (e: Exception){
            e.printStackTrace()
            //Log.w("MainActivity", "Error en carga de datos")
        }
    }

    private fun verificarHabilitado(actualMRID: String) : Int{
        // verifica si el elemento está habilitado
        val analogo = dbHandler.getAnalog(this, actualMRID)

        return analogo.IdEstado
    }

    private fun llenarSpinnerElementos(elselcbo: Int) : String {
        // tiene el actualMRID
        // obtiene el selcbo del string
        // busca los selcbo de la tabla
        val selCBO = dbHandler.getListSelCBO(this, elselcbo)
        val nombres = ArrayList<String>()
        var opciones = ""

        nombres.add("Escoger Opción")
        for(i in 0 until selCBO.size){
            //nombres.add(selCBO[i].id.toString() + " - " + selCBO[i].VALUEORIG)
            nombres.add(selCBO[i].VALUEORIG)
            opciones = selCBO[i].VALUEORIG + ", " + opciones
        }
        val spSelCBO = findViewById<Spinner>(R.id.spSELCBO)

        val spinnerAdaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        spinnerAdaptador.setDropDownViewResource(R.layout.spinner_right_aligned)
        spSelCBO.adapter = spinnerAdaptador
        return opciones
    }

    fun existeMedicion(tipoDato: Int, idactividad: Int, mrid: Int): Int {
        return when (tipoDato) {
            1 -> {
                dbHandler.getAnalogValue(this, idactividad, mrid)
            }
            4 -> {
                dbHandler.getStringValue(this, idactividad, mrid)
            }
            else -> {
                0
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
        // is in the foreground
        enableForegroundDispatch(this, nfcAdapter)
        receiveMessageFromDevice(intent)
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch(this, nfcAdapter)
    }

    private fun receiveMessageFromDevice(intent: Intent) {
        val action = intent.action
        try {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
                val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                with(parcelables) {
                    val inNdefMessage = this?.get(0) as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    val ndefRecord0 = inNdefRecords[0]
                    val inMessage = String(ndefRecord0.payload)

                    //val txtObservacion = findViewById<TextView>(R.id.txtObservacion)
                    //val txtIdAnalog = findViewById<TextView>(R.id.txtIdAnalog)
                    //txtObservacion.text = inMessage

                    // TODO: editar proceso de lectura de etiqueta para obtener código único escrito en nuevo formato
                    // busca el código grabado en etqueta en la tabla elementosxetiqueta
                    // los separa según tipo de elemento para obtener un listado de json con id del elemento y tipo de datos tp
                    val txtJason : JSONArray
                    if(inMessage.indexOf("{") > 0){
                         txtJason = JSONArray(
                            "[" + inMessage.substring(
                                inMessage.indexOf("{"),
                                inMessage.length
                            ) + "]"
                        )
                    } else {
                        // el contenido debe ser el ID de la etiqueta y
                        // para llenar el elementosJSON se debe buscar este ID en la BD
                        // para obtener los elementos tanto en analog como en string
                        txtJason = JSONArray(dbHandler.getElementosXEtiqueta(applicationContext, inMessage))
                        // ID y tp (1 analog 4 string)

                    }
                    elementosJSON = txtJason
                    itemElementosJSON = 0

                    if(actividad.id == null){
                        actividad = obtenerActidadDeAhora()
                    }
                    //txtIdAnalog.text = txtJason.getJSONObject(itemElementosJSON).opt("MRID").toString()//inMessage.substring(3)
                    actualMRID = txtJason.getJSONObject(itemElementosJSON).opt("ID")!!.toString()

                    // cerrar splash de lectura nfc
                    funCerrarNFCLogo()
                    val sistema : sistemas
                    val txtTitulo = findViewById<TextView>(R.id.txtTitulo)
                    //val idSistema : Int = txtJason.getJSONObject(itemElementosJSON).optInt("St")
                    sistema = dbHandler.getSistemaXIDTipo(actualMRID.toInt(), txtJason.getJSONObject(itemElementosJSON).optInt("tp"))
                    //Log.d("Recibe NFC: ", sistema.descripcion)
                    txtTitulo.text = sistema.descripcion
                    //Log.d("Recibe NFC titulo", txtTitulo.text.toString())
                    // prepara el loctimestampglobal
                    loctimestampglobal = ""

                    buscarDatos(actualMRID, 24)
                    habilitaBotones(
                        existeMedicion(
                            txtJason.getJSONObject(itemElementosJSON).optInt("tp"),
                            actividad.id!!,
                            actualMRID.toInt()
                        ), 0
                    )
                    habilitaBotones(11, 1)
                    val cmdLeerNFC = findViewById<Button>(R.id.cmdLeerNFC)
                    cmdLeerNFC.isEnabled = false
                    cmdLeerNFC.setBackgroundColor(Color.DKGRAY)
                    //val txtValNuevo = findViewById<EditText>(R.id.txtValNuevo)
                    //txtValNuevo.requestFocus()
                }
            }
        } catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveMessageFromDevice(intent)
    }

    private fun insertarAnalogValue(){
        val nuevoAnalogValue = analogvalue_enj()
        val elAnalog : analog = dbHandler.getAnalog(this, actualMRID)
        val txtValMaximo = findViewById<TextView>(R.id.txtValMaximo)
        val txtValMinimo = findViewById<TextView>(R.id.txtValMinimo)
        val txtValNuevo = findViewById<TextView>(R.id.txtValNuevo)

        val insertado : Long
        // obtener el timestamp
        obtenerTimeStamp()

        // verificación de límites
        val maximo : Double = txtValMaximo.text.toString().substringAfter(":").trim().toDouble()
        val minimo : Double = txtValMinimo.text.toString().substringAfter(":").trim().toDouble()
        val valor : Double //= txtValNuevo.text.toString().toDouble()

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************
        val nuevaLectura : Int
        val tipoDato : Int = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
        nuevaLectura = existeMedicion(tipoDato, actividad.id!!, actualMRID.toInt()) //dbHandler.getAnalogValue(this.applicationContext, actividad.id!!, actualMRID.toInt())

        if (nuevaLectura > 0) {
            Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            habilitaBotones(nuevaLectura,verificarHabilitado(actualMRID))
        } else {
            if (txtValNuevo.text.toString().isNotEmpty() ){
                //&& txtValNuevo.text.toString().toDouble() > 0
                valor = txtValNuevo.text.toString().toDouble()
                nuevoAnalogValue.ANALOG_MRID = actualMRID.toInt()
                nuevoAnalogValue.InsertUser = valorUsuario
                nuevoAnalogValue.Loctimestamp = actividad.fecha + " " + actividad.horainicio + ":00"
                nuevoAnalogValue.InsertTimestamp = loctimestampglobal
                nuevoAnalogValue.ValueOrig = valor //txtValNuevo.text.toString().toDouble()
                nuevoAnalogValue.IdActividad = actividad.id!!
                nuevoAnalogValue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                insertado = if (valor in minimo..maximo){
                    // || valor == 0.0
                    dbHandler.insertarAnalogValue(nuevoAnalogValue)
                } else {
                    grabaFueraDeLimite(nuevoAnalogValue)
                    //Toast.makeText(applicationContext, "Valor fuera de límites", Toast.LENGTH_SHORT).show()
                }
                if(insertado > 0){
                    // grabar timevalue
                    val idsistema = elAnalog.IdSistema

                    //var insertime : Long = 0
                    // 1 - hora 0 - fecha
                    val eltime: time = dbHandler.getTimeXSistema(idsistema, "H")
                    val timevalue: timevalue_enj = dbHandler.getTimeValueObj(eltime.MRID, actividad.id!!)
                    if(timevalue.TIME_MRID == 0){
                        timevalue.TIME_MRID = eltime.MRID
                        timevalue.IdActividad = actividad.id!!
                        timevalue.InsertUser = valorUsuario
                        timevalue.InsertTimestamp = loctimestampglobal
                        timevalue.Loctimestamp = actividad.fecha + " " + actividad.horainicio + ":00"
                        timevalue.ValueOrig = actividad.horainicio!!
                        timevalue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                        // inserta si no existe
                        //insertime = dbHandler.insertarTimeValue(timevalue)
                    }

                    //Toast.makeText(applicationContext, "Valor insertado", Toast.LENGTH_SHORT).show()
                    // envía a servidor remoto
                    //dbHandler.aInsertarTimeValue(applicationContext, timevalue, insertime, ms)
                    //dbHandler.aInsertarAnalogValue(applicationContext, nuevoAnalogValue, insertado, ms)

                    //verAnalogValues(nuevoAnalogValue.ANALOG_MRID.toString(), 24)
                    //habilitaBotones(insertado.toInt())

                    verAnalogValues(nuevoAnalogValue.ANALOG_MRID.toString(), 24)
                    habilitaBotones(insertado.toInt(),verificarHabilitado(actualMRID))
                }
                else{
                    Toast.makeText(applicationContext, "Valor no insertado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Completar todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerTimeStamp() {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formatted = current.format(formatter)
        if (loctimestampglobal.isEmpty()){
            loctimestampglobal = getTrueTimeD().toString() //formatted.toString()
        }
    }

    private fun grabaFueraDeLimite(nuevoAnalog: analogvalue_enj) : Long {
        var insertado : Long = 0
        try{
            val builder =
                AlertDialog.Builder(this@PrincipalActivity)
            builder.setTitle("¿Grabar fuera de límites?")
            builder.setMessage("¿Desea guardar el valor fuera de sus límites?")
                .setPositiveButton(
                    "Sí"
                ) { _, _ ->
                    //Toast.makeText( applicationContext, "Grabando valores...", Toast.LENGTH_SHORT ).show()
                    val elAnalog : analog = dbHandler.getAnalog(this, actualMRID)
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val formatted = current.format(formatter)
                    insertado = dbHandler.insertarAnalogValue(nuevoAnalog)
                    val eltime: time = dbHandler.getTimeXSistema(elAnalog.IdSistema, "H")
                    //var insertime : Long = 0
                    val timevalue: timevalue_enj = dbHandler.getTimeValueObj(eltime.MRID, actividad.id!!)
                    if(timevalue.TIME_MRID == 0){
                        timevalue.TIME_MRID = eltime.MRID
                        timevalue.IdActividad = actividad.id!!
                        timevalue.InsertUser = valorUsuario
                        timevalue.InsertTimestamp = formatted.toString()
                        timevalue.Loctimestamp = actividad.fecha + " " + actividad.horainicio + ":00"
                        timevalue.ValueOrig = actividad.horainicio!!
                        timevalue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                        // inserta si no existe
                        //insertime = dbHandler.insertarTimeValue(timevalue)
                    }
                    //dbHandler.aInsertarTimeValue(applicationContext, timevalue, insertime, ms)
                    //dbHandler.aInsertarAnalogValue(applicationContext, nuevoAnalog, insertado, ms)
                    verAnalogValues(nuevoAnalog.ANALOG_MRID.toString(), 24)
                    habilitaBotones(insertado.toInt(),verificarHabilitado(actualMRID))
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ ->
                    Toast.makeText(applicationContext, "Cancel...", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
            return insertado
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {

        }
        return insertado
    }

    fun insertarStringValue(){
        val nuevoStringValue = stringvalue_enj()
        val spSelCBO = findViewById<Spinner>(R.id.spSELCBO)
        val elString = dbHandler.getString(this, actualMRID)
        var insertado : Long = 0
        // obtener el timestamp
        obtenerTimeStamp()

        //***************************************
        // si existe algún registro con la actividad IdActividad, entonces no graba el dato
        //***************************************

        //var tipoDato : Int = elementosJSON.getJSONObject(itemElementosJSON).getInt("tp")
        val nuevaLectura : Int = existeMedicion(4, actividad.id!!, actualMRID.toInt()) //dbHandler.getStringValue(this.applicationContext, actividad.id!!, actualMRID.toInt())
        //Log.d("--- existemedicion string --- ", nuevaLectura.toString())
        if (nuevaLectura > 0) {
            Toast.makeText(
                applicationContext,
                "Ya existe un valor para el período: ${actividad.horainicio}",
                Toast.LENGTH_SHORT
            ).show()
            habilitaBotones(nuevaLectura,verificarHabilitado(actualMRID))
        } else {
            if (spSelCBO.selectedItemPosition > 0) {
                nuevoStringValue.STRING_MRID = actualMRID.toInt()
                nuevoStringValue.InsertUser = valorUsuario
                nuevoStringValue.Loctimestamp = actividad.fecha + " " + actividad.horainicio + ":00"
                nuevoStringValue.InsertTimestamp = loctimestampglobal
                //nuevoString.ValueOrig = spSelCBO.getItemAtPosition(spSelCBO.selectedItemPosition).toString()
                nuevoStringValue.ValueOrig = spSelCBO.getItemAtPosition(posicionSelCBO).toString()
                nuevoStringValue.IdActividad = actividad.id!!
                nuevoStringValue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                insertado = dbHandler.insertarStringValue(nuevoStringValue)
            } else {
                Toast.makeText(applicationContext, "Seleccionar un valor", Toast.LENGTH_SHORT).show()
            }
            if (insertado > 0) {
                // grabar timevalue
                val idsistema = elString.IdSistema
                val timevalue : timevalue_enj
                //var insertime : Long = 0
                // 1 - hora 0 - fecha
                val eltime : time = dbHandler.getTimeXSistema(idsistema, "H")
                timevalue = dbHandler.getTimeValueObj(eltime.MRID, actividad.id!!)
                if(timevalue.TIME_MRID == 0){
                    timevalue.TIME_MRID = eltime.MRID
                    timevalue.IdActividad = actividad.id!!
                    timevalue.InsertUser = valorUsuario
                    timevalue.InsertTimestamp = loctimestampglobal
                    timevalue.Loctimestamp = actividad.fecha + " " + actividad.horainicio + ":00"
                    timevalue.ValueOrig = actividad.horainicio!!
                    timevalue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                    // inserta si no existe
                    //insertime = dbHandler.insertarTimeValue(timevalue)
                }
                //Toast.makeText(applicationContext, "Valor insertado", Toast.LENGTH_SHORT).show()
                verStringValues(actualMRID, 24)
                //dbHandler.aInsertarTimeValue(applicationContext, timevalue, insertime, ms)
                //dbHandler.aInsertarStringValue(applicationContext, nuevoStringValue, insertado, ms)
            } else {
                Toast.makeText(applicationContext, "Valor no insertado", Toast.LENGTH_SHORT).show()
            }
            habilitaBotones(insertado.toInt(),verificarHabilitado(actualMRID))
        }
    }

    private fun verAnalogValues(MRID: String, periodo: Int) : analog {
        val analogvalueslist : ArrayList<analogvalue_enj> = dbHandler.getAnalogValues(
            this,
            MRID,
            periodo
        )
        //val adapteranalogvalues = AnalogValueAdapter(this, analogvalueslist)
        val frgGrafico : GraficoFragment
        //val rv : RecyclerView = findViewById(R.id.lstAnalog)

        val analogvalue : analog = dbHandler.getAnalog(this, MRID)

        frgGrafico = GraficoFragment.newInstance(
            analogvalueslist,
            null,
            1,
            analogvalue.MinValue,
            analogvalue.MaxValue
        )
        replaceFragment(frgGrafico, R.id.fragmentContainer)

        return analogvalue
    }

    private fun verStringValues(MRID: String, periodo: Int) : string {
        val stringvalueslist : ArrayList<stringvalue_enj> = dbHandler.getStringValues(
            this,
            MRID,
            periodo
        )
        //var frgGrafico : GraficoFragment
        val stringvalue : string = dbHandler.getString(this, MRID)

        // carga fragmento de listado de datos
        //frgGrafico = GraficoFragment.newInstance(null, stringvalueslist, 2)
        val frgTablaValores : TablaValoresFragment =
            TablaValoresFragment.newInstance(null, stringvalueslist, 2)
        replaceFragment(frgTablaValores, R.id.fragmentContainer)

        return stringvalue
    }

    private fun obtenerActidadDeAhora(): actividades {
        //val laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        //val fecha =  laFechaT.parse(loctimestampglobal)// Date()

        //if(dbHandler.openDatabase()){
        //}
        return dbHandler.getActividad(this, loctimestampglobal)
    }

    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            activity.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()

        filters[0] = IntentFilter()
        with(filters[0]) {
            this?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
            this?.addCategory(Intent.CATEGORY_DEFAULT)
            try {
                this?.addDataType(MIME_TEXT_PLAIN)
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("Check your MIME type")
            }
        }

        adapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }

    private fun disableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        adapter?.disableForegroundDispatch(activity)
    }

    private fun mostrarNFCLogo(){
        try {
            mNfcReadFragment = supportFragmentManager.findFragmentByTag(SplashnfcFragment.TAG) as? SplashnfcFragment
            if (mNfcReadFragment == null) {
                mNfcReadFragment = SplashnfcFragment.newInstance("", "")
            }
            mNfcReadFragment?.show(supportFragmentManager, SplashnfcFragment.TAG)
        }
        catch (e: java.lang.Exception){
            //Log.d("#FragmentRead", "Error Fragement read")
            e.printStackTrace()
        }
    }

    private fun funCerrarNFCLogo(){
        mNfcReadFragment?.dismiss()
    }

    /*private fun loadFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }*/

    private fun replaceFragment(fragment: Fragment, contenedor: Int){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(contenedor, fragment)
        fragmentTransaction.addToBackStack(null)

        fragmentTransaction.commit()
    }

    private fun elementoActivo(actualMRID : String, tipoElemento : Int) : Int {
        Log.d(actualMRID,tipoElemento.toString())
        //TODO: crear función elementoActivo
        return 0
    }

    private fun deshabilitaDatabase(actualMRID : String, tipoElemento : Int){
        Log.d(actualMRID,tipoElemento.toString())
        //TODO: crear función deshabilitaDatabase
    }

    private fun habilitaDatabase(actualMRID : String, tipoElemento : Int){
        Log.d(actualMRID,tipoElemento.toString())
        //TODO: crear función habilitaDatabase
    }
}