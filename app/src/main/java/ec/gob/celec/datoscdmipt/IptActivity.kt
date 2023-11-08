package ec.gob.celec.datoscdmipt

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.instacart.library.truetime.TrueTime
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.actividades
import ec.gob.celec.datoscdmipt.database.models.stringvalue_enj
import ec.gob.celec.datoscdmipt.database.models.time
import ec.gob.celec.datoscdmipt.database.models.timevalue_enj
import ec.gob.celec.datoscdmipt.database.models.usuario
import ec.gob.celec.datoscdmipt.fragmentos.CustomAdapter
import ec.gob.celec.datoscdmipt.fragmentos.CustomAdapterDetail
import ec.gob.celec.datoscdmipt.fragmentos.SplashnfcFragment
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class IptActivity : AppCompatActivity() {

    lateinit var dbHandler : DatabaseOpenHelper
    private lateinit var operador : usuario
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var listView : ListView
    private lateinit var adapter : CustomAdapter
    private lateinit var tareasIPT : List<JSONObject>
    private lateinit var expandableListView: ExpandableListView
    private lateinit var data: ArrayList<HashMap<String, String>>
    private lateinit var radioButton0: RadioButton
    private lateinit var radioButton1: RadioButton
    private lateinit var radioButton2: RadioButton
    private lateinit var radioButton3: RadioButton
    private lateinit var radioButton4: RadioButton
    private lateinit var txtActuacionesIPT: EditText
    private lateinit var cmdGrabarIPT : Button
    private lateinit var cmdActivarIPT : Button
    private lateinit var cmdLeerNFCIPT : Button
    private lateinit var cmdSincronizarIPT : Button
    private var mNfcReadFragment : SplashnfcFragment ? = null

    companion object{
        var actualMRID : String = ""
        var actividad = actividades()
        var elementosJSON = JSONArray()
        var itemElementosJSON : Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_ipt)
        //binding = ActivityIptBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        listView = findViewById(R.id.lista_items)

        dbHandler = DatabaseOpenHelper(this, null, null, 1)
        if (!dbHandler.verificaDB(applicationContext)) finish()
        dbHandler.openDatabase()
        // Obtén una instancia de la base de datos SQLite

        cargaListaIPT("")

        radioButton0 = findViewById(R.id.rdIPTActuacion0)
        radioButton1 = findViewById(R.id.rdIPTActuacion1)
        radioButton2 = findViewById(R.id.rdIPTActuacion2)
        radioButton3 = findViewById(R.id.rdIPTActuacion3)
        radioButton4 = findViewById(R.id.rdIPTActuacion4)

        txtActuacionesIPT = findViewById(R.id.txtActuacionesIPT)
        
        var txtTextoObservacion = txtActuacionesIPT.text.toString()
        radioButton0.setOnClickListener {
            txtTextoObservacion += "\nSe han realizado 0 actuaciones \n"
            llenaTextoObservacionIPT(txtTextoObservacion)
        }

        radioButton1.setOnClickListener {
            txtTextoObservacion += "\nSe han realizado 1 actuaciones \n"
            llenaTextoObservacionIPT(txtTextoObservacion)
        }

        radioButton2.setOnClickListener {
            txtTextoObservacion += "\nSe han realizado 2 actuaciones \n"
            llenaTextoObservacionIPT(txtTextoObservacion)
        }

        radioButton3.setOnClickListener {
            txtTextoObservacion += "\nSe han realizado 3 actuaciones \n"
            llenaTextoObservacionIPT(txtTextoObservacion)
        }

        radioButton4.setOnClickListener {
            txtTextoObservacion += "\nSe han realizado 4 actuaciones \n"
            llenaTextoObservacionIPT(txtTextoObservacion)
        }

        cmdGrabarIPT = findViewById(R.id.cmdGrabarIPT)
        cmdActivarIPT = findViewById(R.id.cmdActivarIPT)
        cmdLeerNFCIPT = findViewById(R.id.cmdLeerNFCIPT)
        cmdSincronizarIPT = findViewById(R.id.cmdSincronizarIPT)

        cmdGrabarIPT.setOnClickListener {
            insertarStringValue()
            txtActuacionesIPT.setText("")
            cmdGrabarIPT.isEnabled = false
        }

        cmdActivarIPT.setOnClickListener {
            //TODO: activar desactivar tarea IPT
        }

        cmdLeerNFCIPT.setOnClickListener {
            mostrarNFCLogo()
        }

        cmdSincronizarIPT.setOnClickListener {
            //TODO: Sincronizar datos con bd remota
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveMessageFromDevice(intent)
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

                    // TODO: cambiar SELECT para que obtenga actividades en función de lo ID del NFC leído grabados en la tabla STRING
                    // trigger de carga de datos es la lectura de la etiqueta NFC
                    // Cargar los datos desde la tabla "actividades"
                    cargaListaIPT(inMessage)
                    // cerrar splash de lectura nfc
                    funCerrarNFCLogo()
                    /*val txtTitulo = findViewById<TextView>(R.id.txtTitulo)
                    //val idSistema : Int = txtJason.getJSONObject(itemElementosJSON).optInt("St")
                    val sistema = dbHandler.getSistemaXIDTipo(
                        actualMRID.toInt(), txtJason.getJSONObject(
                            itemElementosJSON
                        ).optInt("tp"))
                    //Log.d("Recibe NFC: ", sistema.descripcion)
                    txtTitulo.text = sistema.descripcion*/

                }
            }
        } catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }

    private fun funCerrarNFCLogo(){
        mNfcReadFragment?.dismiss()
    }

    private fun mostrarNFCLogo() {
        try {
            mNfcReadFragment = supportFragmentManager.findFragmentByTag(
                SplashnfcFragment.TAG) as? SplashnfcFragment
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

    private fun llenaTextoObservacionIPT(txtTextoObservacion : String){

        val pos = txtActuacionesIPT.text.length
        txtActuacionesIPT.setSelection(pos)
        //txtActuacionesIPT.setText("\n $txtTextoObservacion")

        /*val start = Math.max(txtActuacionesIPT.getSelectionStart(), 0)
        val end = Math.max(txtActuacionesIPT.getSelectionEnd(), 0)
        txtActuacionesIPT.getText().replace(
            Math.min(start, end), Math.max(start, end),
            txtTextoObservacion, 0, txtTextoObservacion.length
        )*/
        txtActuacionesIPT.text.insert(txtActuacionesIPT.selectionStart, txtTextoObservacion)

        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(txtActuacionesIPT, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun cargaListaIPT(idNFC : String) {
        val items = loadItemsFromDatabase(idNFC)
        tareasIPT = items
        // Crear una instancia del adaptador personalizado
        adapter = CustomAdapter(this, items)
        // Asignar el adaptador al ListView
        listView.adapter = adapter
        val selector = ColorDrawable(Color.GRAY)
        listView.selector = selector
        listView.setOnItemClickListener { adapterView, view, position, id ->
            view.setSelected(true)
            Log.d("### - position", position.toString())
            val tareaIPT = tareasIPT.get(position) //listView.selectedView

            val txtIPTidUsuario = tareaIPT.get("idusuario").toString()
            actualMRID = tareaIPT.get("MRID").toString()

            operador = dbHandler.getUsuarioByID(txtIPTidUsuario)

            cargarDetallesTareaIPT(tareaIPT)

            habilitaBotones()
        }

    }

    private fun habilitaBotones() {
        if(actualMRID != ""){
            cmdGrabarIPT.isEnabled = true
            cmdSincronizarIPT.isEnabled = true
            cmdActivarIPT.isEnabled = true
        }
    }

    private fun cargarDetallesTareaIPT(tareaIPT : JSONObject) {
        /* obtiene stringvalue guardados para un MRID especifico */
        val detalles = loadItemsDetailsFromDatabase(tareaIPT.get("MRID").toString())
        val detallesadapter = CustomAdapterDetail(this, detalles)
        val listviewdetails : ListView = findViewById(R.id.lista_detalle_items)
        val txtTareasIpt = findViewById<TextView>(R.id.txtTareasIPT)
        val periodo : String
        val iptHoraInicio= tareaIPT.get("horainicio").toString()
        val txtIPTActID = tareaIPT.get("act_id").toString()

        if (iptHoraInicio.contains("07:30")){
            periodo = "MAÑANA"
        } else {
            periodo = "TARDE"
        }
        listviewdetails.setAdapter(detallesadapter)
        /* fin obtiene stringvalue guardados para un MRID especifico */
        actividad = dbHandler.getActividadId(txtIPTActID.toInt())
        txtTareasIpt.text = "Tarea ${txtIPTActID} de HOY de $periodo de '${operador.usuario}'"

        // llena detalles de la tarea especifica
        val txtDetallesTareaIPT = findViewById<TextView>(R.id.txtDetallesTareaIPT)
        val semana = tareaIPT.get("semana").toString()
        val periodonombre = tareaIPT.get("nombre").toString()
        val diainicio = tareaIPT.get("diainicio").toString()
        val diafinal = tareaIPT.get("diafinal").toString()
        var strDetallesIPT = "Detalles de la Tarea IPT\n"
        strDetallesIPT += "Semana: $semana \nPeriodo: $periodonombre \n"
        strDetallesIPT += "Inicio: $diainicio - Fin: $diafinal"

        txtDetallesTareaIPT.text = strDetallesIPT
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_ipt)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menureporteria, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mnuExitReporteria -> {
                this.finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadItemsFromDatabase(idNFC : String): List<JSONObject> {
        val items = mutableListOf<JSONObject>()

        val cursor = dbHandler.getActividadesIPT("2023-06-26",getTimeInSpecificFormat("HH:mm"), idNFC)//(getSpecificDate("yyyy-MM-dd"), getTimeInSpecificFormat("HH:mm") )

        if (cursor.count > 0){
            cursor.moveToFirst()
            while (cursor.moveToNext()) {
                val item = JSONObject()
                item.put("act_id", cursor.getString(cursor.getColumnIndexOrThrow("act_id")))
                item.put("MRID", cursor.getString(cursor.getColumnIndexOrThrow("MRID")))
                item.put("ALIASNAME", cursor.getString(cursor.getColumnIndexOrThrow("ALIASNAME")))
                item.put("fecha", cursor.getString(cursor.getColumnIndexOrThrow("fecha")))
                item.put("horainicio", cursor.getString(cursor.getColumnIndexOrThrow("horainicio")))
                item.put("horafin", cursor.getString(cursor.getColumnIndexOrThrow("horafin")))
                item.put("operador", cursor.getString(cursor.getColumnIndexOrThrow("operador")))
                item.put("diainicio", cursor.getString(cursor.getColumnIndexOrThrow("diainicio")))
                item.put("diafinal", cursor.getString(cursor.getColumnIndexOrThrow("diafinal")))
                item.put("idusuario", cursor.getString(cursor.getColumnIndexOrThrow("idusuario")))
                item.put("id_periodo", cursor.getString(cursor.getColumnIndexOrThrow("id_periodo")))
                item.put("semana", cursor.getString(cursor.getColumnIndexOrThrow("semana")))
                item.put("id_sistema", cursor.getString(cursor.getColumnIndexOrThrow("id_sistema")))
                item.put("estado", cursor.getString(cursor.getColumnIndexOrThrow("estado")))
                //item.put("prioridad", cursor.getString(cursor.getColumnIndexOrThrow("prioridad")))
                item.put("nombre", cursor.getString(cursor.getColumnIndexOrThrow("nombre")))
                items.add(item)
            }
        }

        cursor.close()
        return items
    }

    
    private fun loadItemsDetailsFromDatabase(mrid : String): List<JSONObject> {
        val items = mutableListOf<JSONObject>()

        val cursor = dbHandler.getHistActividadesIPT(mrid)

        if (cursor.count > 0){
            cursor.moveToFirst()
            while (cursor.moveToNext()) {
                val item = JSONObject()
                item.put("STRING_MRID", cursor.getString(cursor.getColumnIndexOrThrow("STRING_MRID")))
                item.put("VALUEORIG", cursor.getString(cursor.getColumnIndexOrThrow("VALUEORIG")))
                item.put("VALUEEDIT", cursor.getString(cursor.getColumnIndexOrThrow("VALUEEDIT")))
                item.put("INSERT_USER", cursor.getString(cursor.getColumnIndexOrThrow("INSERT_USER")))
                item.put("INSERT_TIMESTAMP", cursor.getString(cursor.getColumnIndexOrThrow("INSERT_TIMESTAMP")))
                items.add(item)
            }
        }

        cursor.close()
        return items
    }

    fun getSpecificDate(format: String): String {
        val currentDate = getCurrentTrueTime()
        val dateFormat = SimpleDateFormat(format)
        // TODO: cambiar la fecha fija por currentdate
        //return dateFormat.format(currentDate)
        return dateFormat.format(Date("2023/06/26"))
    }

    fun getTimeInSpecificFormat(format: String): String {
        val currentTime = getCurrentTrueTime()
        val timeFormat = SimpleDateFormat(format)
        return timeFormat.format(currentTime)
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

    fun insertarStringValue(){
        val nuevoStringValue = stringvalue_enj()
        //val spSelCBO = findViewById<Spinner>(R.id.spSELCBO)
        val elString = dbHandler.getString(this, actualMRID)
        var insertado : Long = 0
        val txtActuacionesIPT = findViewById<EditText>(R.id.txtActuacionesIPT)
        // obtener el timestamp
        //obtenerTimeStamp()

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
            //habilitaBotones(nuevaLectura,verificarHabilitado(actualMRID))
        } else {
            //if (spSelCBO.selectedItemPosition > 0) {
                nuevoStringValue.STRING_MRID = actualMRID.toInt()
                nuevoStringValue.InsertUser = operador.usuario
                nuevoStringValue.Loctimestamp = actividad.fecha.toString() + " " + actividad.horainicio.toString() + ":00"
                nuevoStringValue.InsertTimestamp = getTrueTimeD().toString() //TimeConverters().convertLongToTime(getTrueTime()!!)  //loctimestampglobal
                //nuevoString.ValueOrig = spSelCBO.getItemAtPosition(spSelCBO.selectedItemPosition).toString()
                nuevoStringValue.ValueOrig = txtActuacionesIPT.text.toString() // spSelCBO.getItemAtPosition(posicionSelCBO).toString()
                nuevoStringValue.IdActividad = actividad.id!!
                nuevoStringValue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                insertado = dbHandler.insertarStringValue(nuevoStringValue)
            //} else {
            //    Toast.makeText(applicationContext, "Seleccionar un valor", Toast.LENGTH_SHORT).show()
            //}
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
                    timevalue.InsertUser = operador.usuario
                    timevalue.InsertTimestamp = getTrueTimeD().toString() //TimeConverters().convertLongToTime(getTrueTime()!!)//
                    timevalue.Loctimestamp = actividad.fecha.toString() + " " + actividad.horainicio.toString() + ":00"
                    timevalue.ValueOrig = actividad.horainicio.toString()
                    timevalue.Origen = dbHandler.getDeviceId().toString() //Build.getSerial()
                    // inserta si no existe
                    //insertime = dbHandler.insertarTimeValue(timevalue)
                }
                //Toast.makeText(applicationContext, "Valor insertado", Toast.LENGTH_SHORT).show()
                //verStringValues(actualMRID, 24)
                //TODO: actualizar el listado del detalle de la actividad actual y mostrar el nuevo valor ingresado
                //dbHandler.aInsertarTimeValue(applicationContext, timevalue, insertime, ms)
                //dbHandler.aInsertarStringValue(applicationContext, nuevoStringValue, insertado, ms)
            } else {
                Toast.makeText(applicationContext, "Valor no insertado", Toast.LENGTH_SHORT).show()
            }
            //habilitaBotones(insertado.toInt(),verificarHabilitado(actualMRID))
        }
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
}