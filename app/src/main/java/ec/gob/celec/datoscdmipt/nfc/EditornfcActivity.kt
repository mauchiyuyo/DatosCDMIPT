@file:Suppress("DEPRECATION")

package ec.gob.celec.datoscdmipt.nfc

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.AnalogAdapter
import ec.gob.celec.datoscdmipt.fragmentos.fragment_read
import ec.gob.celec.datoscdmipt.fragmentos.FragmentWrite
import ec.gob.celec.datoscdmipt.listeners.Listener
import ec.gob.celec.datoscdmipt.reporteria.ReporteriaActivity
import org.json.JSONArray
import org.json.JSONObject


class EditornfcActivity : AppCompatActivity(), Listener {
    companion object {
        lateinit var dbHandler : DatabaseOpenHelper
    }
    private var nfcAdapter: NfcAdapter? = null
    private var mNfcWriteFragment : FragmentWrite? = null
    private var mNfcReadFragment : fragment_read? = null
    //    lateinit val cmdEditar = null
    private var spElementos : Spinner? = null
    private var lstAnalog : RecyclerView? = null
    private var txtJSONElementos : TextView? = null
    private var elementosJSON = JSONArray()
    private var valorUsuario : String = ""
    private var valorPerfil : Int = 0
    private var valorCentral : Int = 0
    private var isDialogDisplayed :Boolean = false
    private var isWrite : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_editornfc)
        val cmdEscribir : Button = findViewById(R.id.cmdEscribir)
        val cmdLeer : Button = findViewById(R.id.cmdLeer)
        val spinnerCentrales = findViewById<Spinner>(R.id.spCentrales)
        val spinnerUbicaciones = findViewById<Spinner>(R.id.spUbicaciones)
        val spinnerSistemas = findViewById<Spinner>(R.id.spSistemasIPT)
        val spinnerSubSistemas = findViewById<Spinner>(R.id.spSubSistema)

        dbHandler = DatabaseOpenHelper(this, null, null, 1)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        valorUsuario = intent.getStringExtra("Usuario").toString()
        valorPerfil = intent.getIntExtra("Perfil", 1)
        valorCentral = intent.getIntExtra("Central", 650)
        this.title = "DatosCdM - Editor NFC - Usuario: $valorUsuario"
        llenarSpinnerCentrales()
        initViews()
        cmdEscribir.setOnClickListener { showWriteFragment() }
        cmdLeer.setOnClickListener { showReadFragment() }
        spinnerCentrales.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //var sistema = dbHandler.getSistema(position)
                val posicion : Int = spinnerCentrales.getItemAtPosition(position).toString().substringBefore(" -").toInt()
                Log.d("### Spinner sistemas", posicion.toString())
                //llenarSpinnerUbicaciones(position + 1)
                llenarSpinnerUbicaciones(posicion)
                // verElementosSistema(posicion)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        spinnerUbicaciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //var sistema = dbHandler.getSistema(position)
                val posicion : Int = spinnerUbicaciones.getItemAtPosition(position).toString().substringBefore(" -").toInt()
                ////Log.d("### Spinner sistemas", posicion.toString())
                //if(position > 0)
                    llenarSpinnerSistemas(position + 1)

                // verElementosSistema(posicion)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        spinnerSistemas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //var sistema = dbHandler.getSistema(position)
                val posicion : Int = spinnerSistemas.getItemAtPosition(position).toString().substringBefore(" -").toInt()
                ////Log.d("### Spinner sistemas", posicion.toString())
                //if(position > 0) {
                    llenarSpinnerSubSistemas(posicion)
                    verElementosSistema(posicion)
                //}
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        spinnerSubSistemas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //var sistema = dbHandler.getSistema(position)
                val posicion : Int = spinnerSubSistemas.getItemAtPosition(position).toString().substringBefore(" -").toInt()
                ////Log.d("### Spinner sistemas", posicion.toString())
                //if(position > 0)
                    verElementosSubSistema(posicion)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun llenarSpinnerCentrales(){
        val centrales = dbHandler.getListCentrales()
        val nombres = ArrayList<String>()
        for(i in 0 until centrales.size){
            nombres.add(centrales[i].codigo.toString() + " - " + centrales[i].nombre)
        }
        val spElementos = findViewById<Spinner>(R.id.spCentrales)
        val spinnerAdaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        spElementos.adapter = spinnerAdaptador
    }

    private fun llenarSpinnerUbicaciones(idCentral : Int){
        val ubicaciones = dbHandler.getListUbicaciones(idCentral)
        val nombres = ArrayList<String>()
        Log.d("### SpinnerUbicaciones count", ubicaciones.size.toString())
        for(i in 0 until ubicaciones.size){
            nombres.add(ubicaciones[i].id.toString() + " - " + ubicaciones[i].nombre)
        }
        val spElementos = findViewById<Spinner>(R.id.spUbicaciones)
        val spinnerAdaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        spElementos.adapter = spinnerAdaptador
    }

    private fun llenarSpinnerSistemas(id_ubicacion_ipt : Int){
        val sistemas = dbHandler.getListSistemas(id_ubicacion_ipt)
        val nombres = ArrayList<String>()
        for(i in 0 until sistemas.size){
            nombres.add(sistemas[i].id.toString() + " - " + sistemas[i].subsistema)
        }
        val spElementos = findViewById<Spinner>(R.id.spSistemasIPT)
        val spinnerAdaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        spElementos.adapter = spinnerAdaptador
    }

    private fun llenarSpinnerSubSistemas(id_sistema : Int) : Int{
        val subsistemas = dbHandler.getListSubSistemas(id_sistema)
        val nombres = ArrayList<String>()
        val spElementos = findViewById<Spinner>(R.id.spSubSistema)
        var spinnerAdaptador : ArrayAdapter<String>? = null
        // = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)

        if(subsistemas.size > 0) {
            spElementos.isEnabled = true
            for(i in 0 until subsistemas.size){
                nombres.add(subsistemas[i].id.toString() + " - " + subsistemas[i].subsistema)
            }
            spinnerAdaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        } else {
            spElementos.isEnabled = false
        }
        spElementos.adapter = spinnerAdaptador
        return subsistemas.size
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menueditornfc, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mnuExitNFC -> {
                this.finish()
            }
            R.id.mnuNFCReportes -> {
                try{
                    val actividadReporteria = Intent(this, ReporteriaActivity::class.java)
                    actividadReporteria.putExtra("Usuario", intent.getStringExtra("Usuario"))
                    actividadReporteria.putExtra("Perfil", intent.getIntExtra("Perfil", 1))
                    actividadReporteria.putExtra("Central", intent.getIntExtra("Central", 650))
                    startActivity(actividadReporteria)
                }
                catch (e : Exception){
                    e.printStackTrace()
//                ////Log.w("MainActivity", "Error en botón editornfc")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*fun BuscarDatosNFC(){
        val database : File = applicationContext.getDatabasePath(dbHandler.DATABASE_NAME)
        val txtJSONElemento = findViewById<TextView>(R.id.txtJSONElemento)
        val spElementos = findViewById<Spinner>(R.id.spElementos)
        val txtIdAnalog = findViewById<TextView>(R.id.txtAnalogMRID)
        val txtAMRID_NFC = findViewById<TextView>(R.id.txtAnalogMRID_NFC)
        val txtAnalogName = findViewById<TextView>(R.id.chkAliasNameEscogido)

        if (!database.exists()){
            dbHandler.readableDatabase
            if(copyDatabase(this)){
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
            var MRID : CharSequence = txtAMRID_NFC.getText()
            var elanalog : analog = dbHandler.getAnalog(this, MRID.toString()) //verAnalogValues(MRID.toString())

            var unidad : Int = 0

            if (MRID.substring(1, 3) == "650"){
                unidad = 0
            }
            if (MRID.substring(1, 3) == "651"){
                unidad = 1
            }
            if (MRID.substring(1, 3) == "652"){
                unidad = 2
            }
            if (MRID.substring(1, 3) == "653"){
                unidad = 3
            }

            txtJSONElemento.text = "{\n" +
                    "    \"MRID\": ${elanalog.AMRID},\n" +
                    "    \"Name\": \"${elanalog.Name}\",\n" +
                    "    \"Unidad\": $unidad,\n" +
                    "    \"NFC\": ${elanalog.IdNFC}\n" +
                    "}"

            ////Log.w("MainActivity", "Datos cargados")
        }
        catch (e: Exception){
            e.printStackTrace()
            ////Log.w("MainActivity", "Error en carga de datos")
        }
    }*/

    private fun initViews() {
        spElementos = findViewById(R.id.spSubSistema)
        lstAnalog = findViewById(R.id.lstAnalog)
        txtJSONElementos = findViewById(R.id.txtJSONElemento)
    }

    /*private fun initNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }*/

    private fun showWriteFragment() {
        isWrite = true
        mNfcWriteFragment = supportFragmentManager.findFragmentByTag(FragmentWrite.TAG) as? FragmentWrite
        if (mNfcWriteFragment == null) {
            mNfcWriteFragment = FragmentWrite.newInstance()
        }
        mNfcWriteFragment?.show(supportFragmentManager, FragmentWrite.TAG)
    }

    private fun showReadFragment() {
        try {
            mNfcReadFragment = supportFragmentManager.findFragmentByTag(fragment_read.TAG) as? fragment_read
            if (mNfcReadFragment == null) {
                mNfcReadFragment = fragment_read.newInstance()!!
            }
            mNfcReadFragment?.show(supportFragmentManager, fragment_read.TAG)
        }
        catch (e : java.lang.Exception){
            ////Log.d("#FragmentRead", "Error Fragement read")
            e.printStackTrace()
        }
    }

    override fun onDialogDisplayed() {
        isDialogDisplayed = true
    }

    override fun onDialogDismissed() {
        isDialogDisplayed = false
        isWrite = false
        val sp = findViewById<Spinner>(R.id.spSistemasIPT)
        verElementosSistema( sp.getItemAtPosition(sp.selectedItemPosition).toString().substringBefore(" -").toInt())
    }

    override fun onResume() {
        super.onResume()
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val nfcIntentFilter = arrayOf(techDetected, tagDetected, ndefDetected)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE
        )
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            nfcIntentFilter,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    //@Suppress("DEPRECATION")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        try{
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            //Log.d("La TAG NFC --> " + tag.toString(), "onNewIntent: " + intent.action)
            if (tag != null) {
                val ndef = Ndef.get(tag)
                // Si ndef is null formatear
                val empty = byteArrayOf()
                if (ndef == null){
                    ndef?.format(empty)
                    //Log.d("La TAG NFC bajo --> " + tag.toString(), "onNewIntent: " + intent.action)
                }
                if (isDialogDisplayed) {
                    if (isWrite) {
                        //TODO: colocar el código específico de cada etiqueta
                        val messageToWrite: String = txtJSONElementos?.text.toString()
                        //TODO: escribir codigo para actualizar el campo id_nfc de cada elemento en elementosJSON
                        mNfcWriteFragment =
                            supportFragmentManager.findFragmentByTag(FragmentWrite.TAG) as FragmentWrite
                        mNfcWriteFragment?.onNfcDetected(ndef, messageToWrite, elementosJSON, tag)!!
                        elementosJSON = JSONArray()
                    } else {
                        mNfcReadFragment =
                            supportFragmentManager.findFragmentByTag(fragment_read.TAG) as fragment_read
                        mNfcReadFragment?.onNfcDetected(ndef)
                    }
                }
            } else {
                //Log.d("La TAG NFC --> " + tag.toString(), "onNewIntent: " + intent.action)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    private fun verElementosSistema(id : Int) {
        Toast.makeText(applicationContext, "Pidiendo datos", Toast.LENGTH_SHORT).show()
        //val analoglist : ArrayList<analog> = dbHandler.getAnalogList(this)
        val elementos : ArrayList<JSONObject> = dbHandler.getElementosList(this, id)
        val txtJSONElemento : TextView = findViewById(R.id.txtJSONElemento)

        val adapteranalog = AnalogAdapter(
            applicationContext,
            elementos,
            object : AnalogAdapter.OnItemClickListener {
                override fun onItemClick(texto: String, elJSON : JSONObject, agregar : Boolean) {
                    agregarElemento(texto, elJSON, agregar)
                    txtJSONElemento.text = elementosJSON.toString(2)
                }
            })
        val rv : RecyclerView = findViewById(R.id.lstAnalogList)

        rv.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        rv.adapter = adapteranalog
    }

    private fun agregarElemento(texto: String, elJSON: JSONObject, agregar: Boolean) {
        val tmpJASON = JSONObject()
        //tmpJASON.put("St", id)
        //tmpJASON.put("Od", elJSON.get("OrdenMedicion"))
        if (agregar){
            tmpJASON.put("ID", elJSON.get("MRID"))
            tmpJASON.put("tp", elJSON.get("id_tipo"))
            elementosJSON.put(tmpJASON)
        } else {
            tmpJASON.put("ID", elJSON.get("MRID"))
            loop@ for (item in 0 until elementosJSON.length()){
                if(elementosJSON.getJSONObject(item).get("ID") == tmpJASON.get("ID")){
                    elementosJSON.remove(item)
                    break@loop
                }
            }
        }
    }

    private fun verElementosSubSistema(id : Int) {
        Toast.makeText(applicationContext, "Pidiendo datos", Toast.LENGTH_SHORT).show()
        //val analoglist : ArrayList<analog> = dbHandler.getAnalogList(this)
        val elementos : ArrayList<JSONObject> = dbHandler.getElementosListSub(this, id)
        val txtJSONElemento : TextView = findViewById(R.id.txtJSONElemento)
        val adapteranalog = AnalogAdapter(
            applicationContext,
            elementos,
            object : AnalogAdapter.OnItemClickListener {
                override fun onItemClick(texto: String, elJSON : JSONObject, agregar : Boolean) {
                    /*val tmpJASON = JSONObject()
                    //tmpJASON.put("St", id)
                    //tmpJASON.put("Od", elJSON.get("OrdenMedicion"))
                    if (agregar){
                        tmpJASON.put("ID", elJSON.get("MRID"))
                        tmpJASON.put("tp", elJSON.get("id_tipo"))
                        elementosJSON.put(tmpJASON)
                    } else {
                        tmpJASON.put("ID", elJSON.get("MRID"))
                        loop@ for (item in 0 until elementosJSON.length()){
                            if(elementosJSON.getJSONObject(item).get("ID") == tmpJASON.get("ID")){
                                elementosJSON.remove(item)
                                break@loop
                            }
                        }
                    }*/
                    agregarElemento(texto, elJSON, agregar)
                    txtJSONElemento.text = elementosJSON.toString(2)
                }
            })
        val rv : RecyclerView = findViewById(R.id.lstAnalogList)
        rv.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        rv.adapter = adapteranalog
    }

/*
    private fun copyDatabase(context: Context): Boolean {
        return try {
            val outFileName: String = dbHandler.DBPath + dbHandler.DATABASE_NAME

            // copiando BD
            val `is` = context.assets.open(dbHandler.DATABASE_NAME)
            val os = FileOutputStream(outFileName)

            val buffer = ByteArray(1024)
            while (`is`.read(buffer) > 0) {
                os.write(buffer)
                ////Log.d("#DB", "writing>>")
            }

            os.flush()
            os.close()
            `is`.close()

            ////Log.d("MainActivity", "DB copied")
            true
        } catch (e: java.lang.Exception) {
            ////Log.d("MainActivity", "DB not copied")
            e.printStackTrace()
            false
        }
    }
*/
    /*private fun enableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        // here we are setting up receiving activity for a foreground dispatch
        // thus if activity is already started it will take precedence over any other activity or app
        // with the same intent filters
        val intent = Intent(applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
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
    }*/

    /*private fun disableForegroundDispatch(activity: AppCompatActivity, adapter: NfcAdapter?) {
        adapter?.disableForegroundDispatch(activity)
    }*/
}