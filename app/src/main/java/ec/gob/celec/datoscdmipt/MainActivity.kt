package ec.gob.celec.datoscdmipt

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.*
import ec.gob.celec.datoscdmipt.login.LoginActivity
import ec.gob.celec.datoscdmipt.nfc.EditornfcActivity
import ec.gob.celec.datoscdmipt.nfc.MIME_TEXT_PLAIN
import org.json.JSONArray
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var dbHandler: DatabaseOpenHelper
        var valorUsuario: String = ""
        var valorPerfil: Int = 1
        var valorCentral: Int = 650
    }

    private lateinit var nfcAdapter: NfcAdapter
    private var elementosJSON = JSONArray()
    private var itemElementosJSON: Int = 0
    private var actividad = actividades()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cmdGrabar = findViewById<Button>(R.id.cmdGrabar)
        val cmdBuscar = findViewById<Button>(R.id.cmdBuscar)
        val txtIdAnalog = findViewById<TextView>(R.id.txtIdAnalog)
        //val txtValAnalog = findViewById<TextView>(R.id.txtAnalogValue)
        val txtPeriodo = findViewById<TextView>(R.id.txtPeriodo)
        //val txtMaxValue = findViewById<TextView>(R.id.txtMaxValue)
        //val txtMinValue = findViewById<TextView>(R.id.txtMinValue)
        val txtObservacion = findViewById<TextView>(R.id.txtObservacion)
        val chkObservacion = findViewById<CheckBox>(R.id.chkObservacion)
        //val lstDatos = findViewById<RecyclerView>(R.id.lstAnalog)
        val cmdEditorNFC = findViewById<Button>(R.id.cmdEditorNFC)
        val cmdSiguiente = findViewById<Button>(R.id.cmdSiguiente)
        val cmdAnterior = findViewById<Button>(R.id.cmdAnterior)

        if (!intent.hasExtra("Usuario")) {
            try {
                val actividadLogin = Intent(this, LoginActivity::class.java)
                startActivity(actividadLogin)
            } catch (e: Exception) {
                e.printStackTrace()
//                Log.w("MainActivity", "Error en botón editornfc")
                Toast.makeText(applicationContext, "Error al ejecutar el Login", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }

        valorUsuario = intent.getStringExtra("Usuario").toString()
        valorPerfil = intent.getIntExtra("Perfil", 1)
        valorCentral = intent.getIntExtra("Central", 650)

        Log.d("#STRING EXTRA", valorUsuario)
        Log.d("#STRING EXTRA", valorPerfil.toString())
        Log.d("#STRING EXTRA", valorCentral.toString())

        dbHandler = DatabaseOpenHelper(this, null, null, 1)
        // preparación de NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter.isEnabled) {
            Toast.makeText(applicationContext, "NFC Habilitado", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "NFC No Habilitado", Toast.LENGTH_LONG).show()
            finish()
        }


        if (valorPerfil == 1) {
            cmdEditorNFC.isEnabled = true
        }

        actividad = obtenerActidadDeAhora()

        txtPeriodo.text =
            "Período desde las  ${actividad.horainicio} hasta las ${actividad.horafin} horas"

        cmdGrabar.setOnClickListener {
            when (elementosJSON.getJSONObject(itemElementosJSON).getInt("id_tipo")) {
                1 -> {
                    insertarAnalogValue()
                }
                4 -> {
                    insertarStringValue()
                }
            }
        }

        cmdBuscar.setOnClickListener {
            // revisa q exista la BD
            buscarDatos()
        }

        cmdEditorNFC.setOnClickListener {
            try {
                val actividadEditorNFC = Intent(this, EditornfcActivity::class.java)
                actividadEditorNFC.putExtra("Usuario", intent.getStringExtra("Usuario"))
                actividadEditorNFC.putExtra("Perfil", intent.getIntExtra("Perfil", 1))
                actividadEditorNFC.putExtra("Central", intent.getIntExtra("Central", 650))
                startActivity(actividadEditorNFC)
            } catch (e: Exception) {
                e.printStackTrace()
//                Log.w("MainActivity", "Error en botón editornfc")
            }
        }

        chkObservacion.setOnClickListener {
            txtObservacion.isEnabled = chkObservacion.isChecked
        }

        cmdAnterior.setOnClickListener {
            if (itemElementosJSON <= elementosJSON.length() && itemElementosJSON > 0) {
                itemElementosJSON -= 1
                cmdSiguiente.isEnabled = true
            } else {
                cmdAnterior.isEnabled = false
                return@setOnClickListener
            }
            txtIdAnalog.text = elementosJSON.getJSONObject(itemElementosJSON).opt("MRID")
                .toString()//inMessage.substring(3)
            buscarDatos()
        }

        cmdSiguiente.setOnClickListener {
            if (itemElementosJSON < elementosJSON.length() - 1 && itemElementosJSON >= 0) {
                itemElementosJSON += 1
                cmdAnterior.isEnabled = true
            } else {
                cmdSiguiente.isEnabled = false
                return@setOnClickListener
            }
            txtIdAnalog.text = elementosJSON.getJSONObject(itemElementosJSON).opt("MRID")
                .toString()//inMessage.substring(3)
            buscarDatos()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buscarDatos() {
        val database: File = applicationContext.getDatabasePath(dbHandler.nameDatabase)
        val txtIdAnalog = findViewById<TextView>(R.id.txtIdAnalog)
        //val txtValAnalog = findViewById<TextView>(R.id.txtAnalogValue)
        //val txtAnalog = findViewById<TextView>(R.id.txtAnalog)
        val txtMaxValue = findViewById<TextView>(R.id.txtMaxValue)
        val txtMinValue = findViewById<TextView>(R.id.txtMinValue)
        //val txtObservacion = findViewById<TextView>(R.id.txtObservacion)
        val txtElemento = findViewById<TextView>(R.id.txtElemento)

        if (!database.exists()) {
            dbHandler.readableDatabase
            if (copyDatabase(this)) {
                Toast.makeText(applicationContext, "BD Copiada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Error en la Copia de BD",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        try {
            val MRID: CharSequence = txtIdAnalog.getText()
            val elanalog: analog
            val elstring: string
            //var laActividad : actividades

            when (elementosJSON.getJSONObject(itemElementosJSON).getInt("id_tipo")) {
                1 -> {
                    elanalog = verAnalogValues(MRID.toString())
                    txtMaxValue.text = elanalog.MaxValue.toString()
                    txtMinValue.text = elanalog.MinValue.toString()
                    txtElemento.text = elanalog.Description
                }
                4 -> {
                    elstring = verStringValues(MRID.toString())
                    txtMaxValue.text = "0.0" //elanalog.MaxValue.toString()
                    txtMinValue.text = "0.0" //elanalog.MinValue.toString()
                    txtElemento.text = elstring.Description
                }
                else -> {
                    txtMaxValue.text = "0.0" //elanalog.MaxValue.toString()
                    txtMinValue.text = "0.0" //elanalog.MinValue.toString()
                    txtElemento.text = "Sin descripción - Sin datos"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("MainActivity", "Error en carga de datos")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    //@Deprecated("clase descontinuada getParcelableArrayExtra - receiveMessageFromDevice")
    override fun onResume() {
        super.onResume()
        // foreground dispatch should be enabled here, as onResume is the guaranteed place where app
        // is in the foreground
        enableForegroundDispatch(this, this.nfcAdapter)
        receiveMessageFromDevice(intent)
    }

    override fun onPause() {
        super.onPause()
        disableForegroundDispatch(this, this.nfcAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    //@Deprecated("Clase descontinuada getParcelableArrayExtra")
    private fun receiveMessageFromDevice(intent: Intent) {
        val action = intent.action

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val parcelables = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NfcAdapter::class.java)
            } else {
                @Suppress("DEPRECATION") intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                //intent.getParcelableExtra<NfcAdapter.EXTRA_NDEF_MESSAGES>(NfcAdapter.EXTRA_NDEF_MESSAGES)
            }

            //intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            with(parcelables) {
                val inNdefMessage = this?.get(0) as NdefMessage
                val inNdefRecords = inNdefMessage.records
                val ndefRecord0 = inNdefRecords[0]
                val inMessage = String(ndefRecord0.payload)

                val txtObservacion = findViewById<TextView>(R.id.txtObservacion)
                val txtIdAnalog = findViewById<TextView>(R.id.txtIdAnalog)
                txtObservacion.text = inMessage

                val txtJason = JSONArray(
                    "[" + inMessage.substring(
                        inMessage.indexOf("{"),
                        inMessage.length
                    ) + "]"
                )
                elementosJSON = txtJason

                txtIdAnalog.text =
                    txtJason.getJSONObject(0).opt("MRID").toString()//inMessage.substring(3)
                itemElementosJSON = 0
                if (elementosJSON.length() > 1) {
                    val cmdAnterior = findViewById<Button>(R.id.cmdAnterior)
                    val cmdSiguiente = findViewById<Button>(R.id.cmdSiguiente)

                    cmdAnterior.isEnabled = true
                    cmdSiguiente.isEnabled = true
                }
                buscarDatos()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Deprecated("Clase descontinuada receiveMessageFromDevice")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        receiveMessageFromDevice(intent)
    }

    private fun insertarAnalogValue(){
        val nuevoAnalog = analogvalue_enj()
        val txtValMRID = findViewById<TextView>(R.id.txtIdAnalog)
        val txtValAnalog = findViewById<TextView>(R.id.txtAnalogValue)
        //val txtAnalog = findViewById<TextView>(R.id.txtAnalog)
        val txtMaxVal = findViewById<TextView>(R.id.txtMaxValue)
        val txtMinVal = findViewById<TextView>(R.id.txtMinValue)
        val insertado : Long
        // obtener el timestamp
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)

        // verificación de límites
        val maximo : Double = txtMaxVal.text.toString().toDouble()
        val minimo : Double = txtMinVal.text.toString().toDouble()
        val valor = txtValAnalog.text.toString().toDouble()

        if (txtValAnalog.text.toString().isNotEmpty()){
            if (valor in minimo..maximo){
                nuevoAnalog.ANALOG_MRID = txtValMRID.text.toString().toInt()
                nuevoAnalog.InsertUser = valorUsuario
                nuevoAnalog.InsertTimestamp = formatted.toString()
                nuevoAnalog.ValueOrig = txtValAnalog.text.toString().toDouble()
                nuevoAnalog.IdActividad = actividad.id!!
                insertado = dbHandler.insertarAnalogValue(nuevoAnalog)
                if(insertado > 0){
                    Toast.makeText(applicationContext,"Valor insertado", Toast.LENGTH_SHORT).show()
                    verAnalogValues(nuevoAnalog.ANALOG_MRID.toString())
                }
                else{
                    Toast.makeText(applicationContext,"Valor no insertado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext,"Valor fuera de límites", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext,"Completar todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun insertarStringValue(){
        val nuevoString = stringvalue_enj()
        val txtValMRID = findViewById<TextView>(R.id.txtIdAnalog)
        val txtValAnalog = findViewById<TextView>(R.id.txtAnalogValue)
        //val txtAnalog = findViewById<TextView>(R.id.txtAnalog)
        //val txtMaxVal = findViewById<TextView>(R.id.txtMaxValue)
        //val txtMinVal = findViewById<TextView>(R.id.txtMinValue)
        var insertado : Long = 0
        // obtener el timestamp
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)

        // verificación de límites
        //val maximo : Double = txtMaxVal.text.toString().toDouble()
        //val minimo : Double = txtMinVal.text.toString().toDouble()
        //val valor : Double = txtValAnalog.text.toString().toDouble()

        if (txtValAnalog.text.toString().isNotEmpty()){
            txtValMRID.text.toString().toInt().also { nuevoString.STRING_MRID = it }
            nuevoString.InsertUser = valorUsuario
            nuevoString.InsertTimestamp = formatted.toString()
            nuevoString.ValueOrig = txtValAnalog.text.toString()
            nuevoString.IdActividad = actividad.id!!
            insertado = dbHandler.insertarStringValue(nuevoString)
        } else {
            Toast.makeText(applicationContext,"Completar todos los campos", Toast.LENGTH_SHORT).show()
        }
        if(insertado > 0){
            Toast.makeText(applicationContext,"Valor insertado", Toast.LENGTH_SHORT).show()
            verStringValues(nuevoString.STRING_MRID.toString())
        }
        else{
            Toast.makeText(applicationContext,"Valor no insertado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verAnalogValues(MRID: String) : analog {
        val analogvalueslist = dbHandler.getAnalogValues(this, MRID, 24)
        val adapteranalogvalues = AnalogValueAdapter(this, analogvalueslist)
        val rv : RecyclerView = findViewById(R.id.lstAnalog)
        val analogvalue = dbHandler.getAnalog(this, MRID)

        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false) //as RecyclerView.LayoutManager
        rv.adapter = adapteranalogvalues

        return analogvalue
    }

    private fun verStringValues(MRID: String) : string {
        val stringvalueslist = dbHandler.getStringValues(this, MRID, 24)
        val adapterstringvalues = StringValueAdapter(this, stringvalueslist)
        val rv : RecyclerView = findViewById(R.id.lstAnalog)
        val stringvalue = dbHandler.getString(this, MRID)

        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false) //as RecyclerView.LayoutManager
        rv.adapter = adapterstringvalues

        return stringvalue
    }

    private fun obtenerActidadDeAhora() : actividades {
        val actividad : actividades
        val laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fecha = Date()

        actividad = dbHandler.getActividad(this, laFechaT.format(fecha))
        return actividad
    }

    private fun copyDatabase(context: Context): Boolean {
        return try {
            val outFileName: String = dbHandler.dbPath + dbHandler.nameDatabase

            // copiando BD
            val `is` = context.assets.open(dbHandler.nameDatabase)
            val os = FileOutputStream(outFileName)

            val buffer = ByteArray(1024)
            while (`is`.read(buffer) > 0) {
                os.write(buffer)
                Log.d("#DB", "writing>>")
            }

            os.flush()
            os.close()
            `is`.close()

            Log.d("MainActivity", "DB copied")
            true
        } catch (e: java.lang.Exception) {
            Log.d("MainActivity", "DB not copied")
            e.printStackTrace()
            false
        }
    }

    private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters

        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            activity.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
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
}