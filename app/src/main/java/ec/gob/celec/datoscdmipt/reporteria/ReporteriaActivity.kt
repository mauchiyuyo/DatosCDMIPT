package ec.gob.celec.datoscdmipt.reporteria

//import com.google.common.collect.Table
//import ec.gob.celec.datoscdmipt.nfc.EditornfcActivity
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter


//import org.json.JSONObject

@Suppress("DEPRECATION")
class ReporteriaActivity : AppCompatActivity() {
    private var valorUsuario : String = ""
    private var valorPerfil : Int = 0
    private var valorCentral : Int = 0

    companion object {
        lateinit var dbHandler : DatabaseOpenHelper
        //lateinit var table : TableLayout
    }

    @Deprecated("Clase descontinuada FLAG_FULLSCREEN")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_reporteria)
        val spRptSistemas = findViewById<Spinner>(R.id.spRptSistemas)
        val cmdRpt24Horas = findViewById<Button>(R.id.cmdRpt24Horas)
        val cmdRpt72Horas = findViewById<Button>(R.id.cmdRpt72Horas)
        val cmdRptExpXLS = findViewById<Button>(R.id.cmdRptExportar)

        dbHandler = DatabaseOpenHelper(this, null, null, 1)
        valorUsuario = intent.getStringExtra("Usuario").toString()
        valorPerfil = intent.getIntExtra("Perfil", 1)
        valorCentral = intent.getIntExtra("Central", 650)
        this.title = "DatosCdM - Reportes - Usuario: $valorUsuario"

        llenarSpinnerSistemas(0)
        //TODO: corregir la pantalla de reporteria agregando centrales y ubicaciones

        spRptSistemas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //var sistema = dbHandler.getSistema(position)
                val posicion : Int = spRptSistemas.getItemAtPosition(position).toString().substringBefore(" -").toInt()
                //verElementosSistema(posicion)
                llenarTablaSistemas(posicion, 24)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        cmdRpt24Horas.setOnClickListener {
            val posicion : Int = spRptSistemas.getItemAtPosition(spRptSistemas.selectedItemPosition).toString().substringBefore(" -").toInt()
            //verElementosSistema(posicion)
            llenarTablaSistemas(posicion, 24)
        }

        cmdRpt72Horas.setOnClickListener {
            val posicion : Int = spRptSistemas.getItemAtPosition(spRptSistemas.selectedItemPosition).toString().substringBefore(" -").toInt()
            //verElementosSistema(posicion)
            llenarTablaSistemas(posicion, 72)
        }

        cmdRptExpXLS.setOnClickListener {
            exportTheDB()
        }
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

    private fun llenarSpinnerSistemas(id_ubicacion_ipt : Int){
        val sistemas = dbHandler.getListSistemas(id_ubicacion_ipt)
        val nombres = ArrayList<String>()

        for(i in 0 until sistemas.size){
            nombres.add(sistemas[i].id.toString() + " - " + sistemas[i].descripcion)
        }
        val spElementos = findViewById<Spinner>(R.id.spRptSistemas)
        val spinnerAdaptador = ArrayAdapter(this, android.R.layout.simple_list_item_1, nombres)
        spElementos.adapter = spinnerAdaptador
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun exportTheDB() {
        val myFile: File
        val cal: Calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val timeStampDB = sdf.format(cal.time)

        val cmbSistema = findViewById<Spinner>(R.id.spRptSistemas)
        val nFile = cmbSistema.selectedItem.toString().trim().substring(0,10)
        try {
            myFile = File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/${nFile}_${timeStampDB}.csv")
            myFile.createNewFile()
            val fOut = FileOutputStream(myFile)
            val myOutWriter = OutputStreamWriter(fOut)

            val cabtable = findViewById<TableLayout>(R.id.tblCabReporte)
            val table = findViewById<TableLayout>(R.id.tblReporte)
            //val row = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_aire_comprimida_s_f6,null) as TableRow
            //val rowHead : TableRow //

            var cadena : String

            for(i in 0 until cabtable.childCount){
                val view = cabtable.getChildAt(i)
                val row = view as TableRow
                cadena = ""
                for(j in 0 until row.childCount){
                    val celda = row.getChildAt(j) as TextView
                    cadena = cadena + celda.text + ", "
                }
                myOutWriter.append(cadena)
                myOutWriter.append("\n")
            }

            for(i in 0 until table.childCount){
                val view = table.getChildAt(i)
                val row = view as TableRow
                cadena = ""
                for(j in 0 until row.childCount){
                    val celda = row.getChildAt(j) as TextView
                    cadena = cadena + celda.text + ", "
                }
                myOutWriter.append(cadena)
                myOutWriter.append("\n")
            }
            myOutWriter.close()
            fOut.close()
            //if (copiado > 0) {
                Toast.makeText(applicationContext, "Exportación Completa", Toast.LENGTH_SHORT).show()
                //copiado = 0
            //}

        } catch (se: IOException) {
            Log.e(javaClass.simpleName, "Could not create or Open the database")
            Log.d("--------- Error exportación", se.toString())
            Toast.makeText(applicationContext, "Exportación Fallida", Toast.LENGTH_SHORT).show()
        } finally {
        }
    }

    @SuppressLint("Range", "SimpleDateFormat", "SetTextI18n")
    fun llenarTablaSistemas(posicion : Int, periodo : Int){
        val rowHead : TableRow //
        val elementos : Cursor
        val cabtable = findViewById<TableLayout>(R.id.tblCabReporte)
        val table = findViewById<TableLayout>(R.id.tblReporte)
        cabtable.removeAllViews()
        table.removeAllViews()
        val rootCabecera = findViewById<TableRow>(R.id.tblCabRepRow) //findViewById<LinearLayout>(R.id.lltTablaReporte)
        val rootCuerpo = findViewById<ScrollView>(R.id.scrvReporteria)

        when(posicion){
            1 -> { // AireComprimidaSF6
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_aire_comprimida_s_f6, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtACSF6Fecha) as TextView).text=("Fecha")
                (rowHead.findViewById<View>(R.id.txtACSF6Hora) as TextView).text=("Hora")
                (rowHead.findViewById<View>(R.id.txtACSF6CompPrinc) as TextView).text=("Comp Princ")
                (rowHead.findViewById<View>(R.id.txtACSF6HM1) as TextView).text=("HMI 1")
                (rowHead.findViewById<View>(R.id.txtACSF6HM2) as TextView).text=("HMI 2")
                (rowHead.findViewById<View>(R.id.txtACSF6Acum1) as TextView).text=("Acum 1")
                (rowHead.findViewById<View>(R.id.txtACSF6Acum2) as TextView).text=("Acum 2")
                (rowHead.findViewById<View>(R.id.txtACSF6Acum3) as TextView).text=("Acum 3")
                (rowHead.findViewById<View>(R.id.txtACSF6U1FaseA) as TextView).text=("U1 Fase 1")
                (rowHead.findViewById<View>(R.id.txtACSF6U1FaseB) as TextView).text=("U1 Fase 2")
                (rowHead.findViewById<View>(R.id.txtACSF6U1FaseC) as TextView).text=("U1 Fase 3")
                (rowHead.findViewById<View>(R.id.txtACSF6U1PosicionTAP) as TextView).text=("U1 Posición TAP")
                (rowHead.findViewById<View>(R.id.txtACSF6U2FaseA) as TextView).text=("U2 Fase 1")
                (rowHead.findViewById<View>(R.id.txtACSF6U2FaseB) as TextView).text=("U2 Fase 2")
                (rowHead.findViewById<View>(R.id.txtACSF6U2FaseC) as TextView).text=("U2 Fase 3")
                (rowHead.findViewById<View>(R.id.txtACSF6U2PosicionTAP) as TextView).text=("U2 Posición TAP")
                (rowHead.findViewById<View>(R.id.txtACSF6U3FaseA) as TextView).text=("U3 Fase 1")
                (rowHead.findViewById<View>(R.id.txtACSF6U3FaseB) as TextView).text=("U3 Fase 2")
                (rowHead.findViewById<View>(R.id.txtACSF6U3FaseC) as TextView).text=("U3 Fase 3")
                (rowHead.findViewById<View>(R.id.txtACSF6U3PosicionTAP) as TextView).text=("U3 Posición TAP")
                //rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                //rowHead.textDirection = ""
                //cabtable!!.removeAllViews()
                cabtable!!.addView(rowHead)
                dbHandler.openDatabase()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                elementos = dbHandler.getRptSistema(posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    //var contador = 0
                    do {

                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_aire_comprimida_s_f6,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtACSF6Fecha) as TextView).text =
                            output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtACSF6Hora) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("HoraInicio"))
                        (row.findViewById<View>(R.id.txtACSF6CompPrinc) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("AirComp_Principal"))
                        (row.findViewById<View>(R.id.txtACSF6HM1) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("AirComp_HMI_1"))
                        (row.findViewById<View>(R.id.txtACSF6HM2) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("AirComp_HMI_2"))
                        (row.findViewById<View>(R.id.txtACSF6Acum1) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("AirComp_Acum_1"))
                        (row.findViewById<View>(R.id.txtACSF6Acum2) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("AirComp_Acum_2"))
                        (row.findViewById<View>(R.id.txtACSF6Acum3) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("AirComp_Acum_3"))
                        (row.findViewById<View>(R.id.txtACSF6U1FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U1_SF6_FA"))
                        (row.findViewById<View>(R.id.txtACSF6U1FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U1_SF6_FB"))
                        (row.findViewById<View>(R.id.txtACSF6U1FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U1_SF6_FC"))
                        (row.findViewById<View>(R.id.txtACSF6U1PosicionTAP) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U1_TAP"))
                        (row.findViewById<View>(R.id.txtACSF6U2FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U2_SF6_FA"))
                        (row.findViewById<View>(R.id.txtACSF6U2FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U2_SF6_FB"))
                        (row.findViewById<View>(R.id.txtACSF6U2FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U2_SF6_FC"))
                        (row.findViewById<View>(R.id.txtACSF6U2PosicionTAP) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U2_TAP"))
                        (row.findViewById<View>(R.id.txtACSF6U3FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U3_SF6_FA"))
                        (row.findViewById<View>(R.id.txtACSF6U3FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U3_SF6_FB"))
                        (row.findViewById<View>(R.id.txtACSF6U3FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U3_SF6_FC"))
                        (row.findViewById<View>(R.id.txtACSF6U3PosicionTAP) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("U3_TAP"))
                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table!!.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table!!.requestLayout()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                dbHandler.closeDatabase()
            }
            2 -> { //

            }
            3 -> { // SistemaOleohidraulico
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_oleohidraulico, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtSistOleoFecha) as TextView).text = ("Fecha")
                (rowHead.findViewById<View>(R.id.txtSistOleoHora) as TextView).text = ("Hora")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfPresSist) as TextView).text = ("U1_VE_PresSist")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfPresNitro) as TextView).text = ("U1_VE_PresNitro")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfNivAceiteCuba) as TextView).text=("U1_VE_NivAceiteCuba")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfNivAceiteAcum) as TextView).text=("U1_VE_NivAceiteAcum")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfPresAperValEsf) as TextView).text=("U1_VE_PresAperVE")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfPresRetSelloTrabajo) as TextView).text=("U1_VE_PresRetSelloTraba")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1ValEsfPresSetSelloMtto) as TextView).text=("U1_VE_PresRetSelloMtto")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1RegVelPresSist) as TextView).text=("U1_RV_PresSist")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1RegVelPresNitro) as TextView).text=("U1_RV_PresNitro")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1RegVelNivAceiteAcum) as TextView).text=("U1_RV_NivAceiteAcum")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1RegVelTempCubaAceite) as TextView).text=("U1_RV_TempCubaAceite")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1RegVelPresEntradaDist) as TextView).text=("U1_RV_PresEntDistri")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1RegVelPresFosoTurbi) as TextView).text=("U1_RV_PresFosoTurbi")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1InyApertIny1) as TextView).text=("U1_ApeIny1")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1InyApertIny2) as TextView).text=("U1_ApeIny2")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1InyApertIny3) as TextView).text=("U1_ApeIny3")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1InyApertIny4) as TextView).text=("U1_ApeIny4")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1InyApertIny5) as TextView).text=("U1_ApeIny5")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1InyApertIny6) as TextView).text=("U1_ApeIny6")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefApertDef1) as TextView).text=("U1_ApeDef1")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefApertDef2) as TextView).text=("U1_ApeDef2")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefApertDef3) as TextView).text=("U1_ApeDef3")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefApertDef4) as TextView).text=("U1_ApeDef4")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefApertDef5) as TextView).text=("U1_ApeDef5")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefApertDef6) as TextView).text=("U1_ApeDef6")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1DefPotUni) as TextView).text=("U1_Def_PotUni")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1FrenosPresTanq) as TextView).text=("U1_Freno_PresTanq")
                (rowHead.findViewById<View>(R.id.txtSistOleoU1FrenosPresSist) as TextView).text=("U1_Freno_PresSist")

                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfPresSist) as TextView).text = ("U2_VE_PresSist")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfPresNitro) as TextView).text = ("U2_VE_PresNitro")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfNivAceiteCuba) as TextView).text=("U2_VE_NivAceiteCuba")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfNivAceiteAcum) as TextView).text=("U2_VE_NivAceiteAcum")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfPresAperValEsf) as TextView).text=("U2_VE_PresAperVE")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfPresRetSelloTrabajo) as TextView).text=("U2_VE_PresRetSelloTraba")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2ValEsfPresSetSelloMtto) as TextView).text=("U2_VE_PresRetSelloMtto")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2RegVelPresSist) as TextView).text=("U2_RV_PresSist")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2RegVelPresNitro) as TextView).text=("U2_RV_PresNitro")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2RegVelNivAceiteAcum) as TextView).text=("U2_RV_NivAceiteAcum")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2RegVelTempCubaAceite) as TextView).text=("U2_RV_TempCubaAceite")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2RegVelPresEntradaDist) as TextView).text=("U2_RV_PresEntDistri")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2RegVelPresFosoTurbi) as TextView).text=("U2_RV_PresFosoTurbi")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2InyApertIny1) as TextView).text=("U2_ApeIny1")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2InyApertIny2) as TextView).text=("U2_ApeIny2")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2InyApertIny3) as TextView).text=("U2_ApeIny3")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2InyApertIny4) as TextView).text=("U2_ApeIny4")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2InyApertIny5) as TextView).text=("U2_ApeIny5")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2InyApertIny6) as TextView).text=("U2_ApeIny6")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefApertDef1) as TextView).text=("U2_ApeDef1")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefApertDef2) as TextView).text=("U2_ApeDef2")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefApertDef3) as TextView).text=("U2_ApeDef3")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefApertDef4) as TextView).text=("U2_ApeDef4")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefApertDef5) as TextView).text=("U2_ApeDef5")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefApertDef6) as TextView).text=("U2_ApeDef6")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2DefPotUni) as TextView).text=("U2_Def_PotUni")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2FrenosPresTanq) as TextView).text=("U2_Freno_PresTanq")
                (rowHead.findViewById<View>(R.id.txtSistOleoU2FrenosPresSist) as TextView).text=("U2_Freno_PresSist")

                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfPresSist) as TextView).text = ("U3_VE_PresSist")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfPresNitro) as TextView).text = ("U3_VE_PresNitro")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfNivAceiteCuba) as TextView).text=("U3_VE_NivAceiteCuba")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfNivAceiteAcum) as TextView).text=("U3_VE_NivAceiteAcum")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfPresAperValEsf) as TextView).text=("U3_VE_PresAperVE")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfPresRetSelloTrabajo) as TextView).text=("U3_VE_PresRetSelloTraba")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3ValEsfPresSetSelloMtto) as TextView).text=("U3_VE_PresRetSelloMtto")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3RegVelPresSist) as TextView).text=("U3_RV_PresSist")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3RegVelPresNitro) as TextView).text=("U3_RV_PresNitro")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3RegVelNivAceiteAcum) as TextView).text=("U3_RV_NivAceiteAcum")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3RegVelTempCubaAceite) as TextView).text=("U3_RV_TempCubaAceite")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3RegVelPresEntradaDist) as TextView).text=("U3_RV_PresEntDistri")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3RegVelPresFosoTurbi) as TextView).text=("U3_RV_PresFosoTurbi")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3InyApertIny1) as TextView).text=("U3_ApeIny1")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3InyApertIny2) as TextView).text=("U3_ApeIny2")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3InyApertIny3) as TextView).text=("U3_ApeIny3")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3InyApertIny4) as TextView).text=("U3_ApeIny4")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3InyApertIny5) as TextView).text=("U3_ApeIny5")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3InyApertIny6) as TextView).text=("U3_ApeIny6")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefApertDef1) as TextView).text=("U3_ApeDef1")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefApertDef2) as TextView).text=("U3_ApeDef2")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefApertDef3) as TextView).text=("U3_ApeDef3")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefApertDef4) as TextView).text=("U3_ApeDef4")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefApertDef5) as TextView).text=("U3_ApeDef5")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefApertDef6) as TextView).text=("U3_ApeDef6")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3DefPotUni) as TextView).text=("U3_Def_PotUni")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3FrenosPresTanq) as TextView).text=("U3_Freno_PresTanq")
                (rowHead.findViewById<View>(R.id.txtSistOleoU3FrenosPresSist) as TextView).text=("U3_Freno_PresSist")

                rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                table!!.addView(rowHead)
                dbHandler.openDatabase()
                //Log.d("###Posicion", posicion.toString())
                //Log.d("###Posicion", periodo.toString())
                elementos = dbHandler.getRptSistema( posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    do {
                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_oleohidraulico,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtSistOleoFecha) as TextView).text = output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtSistOleoHora) as TextView).text = elementos.getString(elementos.getColumnIndex("HoraInicio"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_PresSist"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfPresNitro) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_PresNitro"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfNivAceiteCuba) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_NivAceiteCuba"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfNivAceiteAcum) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_NivAceiteAcum"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfPresAperValEsf) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_PresAperVE"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfPresRetSelloTrabajo) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_PresRetSelloTraba"))
                        (row.findViewById<View>(R.id.txtSistOleoU1ValEsfPresSetSelloMtto) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_VE_PresRetSelloMtto"))
                        (row.findViewById<View>(R.id.txtSistOleoU1RegVelPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_RV_PresSist"))
                        (row.findViewById<View>(R.id.txtSistOleoU1RegVelPresNitro) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_RV_PresNitro"))
                        (row.findViewById<View>(R.id.txtSistOleoU1RegVelNivAceiteAcum) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_RV_NivAceiteAcum"))
                        (row.findViewById<View>(R.id.txtSistOleoU1RegVelTempCubaAceite) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_RV_TempCubaAceite"))
                        (row.findViewById<View>(R.id.txtSistOleoU1RegVelPresEntradaDist) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_RV_PresEntDistri"))
                        (row.findViewById<View>(R.id.txtSistOleoU1RegVelPresFosoTurbi) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_RV_PresFosoTurbi"))
                        (row.findViewById<View>(R.id.txtSistOleoU1InyApertIny1) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeIny1"))
                        (row.findViewById<View>(R.id.txtSistOleoU1InyApertIny2) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeIny2"))
                        (row.findViewById<View>(R.id.txtSistOleoU1InyApertIny3) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeIny3"))
                        (row.findViewById<View>(R.id.txtSistOleoU1InyApertIny4) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeIny4"))
                        (row.findViewById<View>(R.id.txtSistOleoU1InyApertIny5) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeIny5"))
                        (row.findViewById<View>(R.id.txtSistOleoU1InyApertIny6) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeIny6"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefApertDef1) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeDef1"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefApertDef2) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeDef2"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefApertDef3) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeDef3"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefApertDef4) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeDef4"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefApertDef5) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeDef5"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefApertDef6) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_ApeDef6"))
                        (row.findViewById<View>(R.id.txtSistOleoU1DefPotUni) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_Def_PotUni"))
                        (row.findViewById<View>(R.id.txtSistOleoU1FrenosPresTanq) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_Freno_PresTanq"))
                        (row.findViewById<View>(R.id.txtSistOleoU1FrenosPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U1_Freno_PresSist"))

                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_PresSist"))
                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfPresNitro) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_PresNitro"))
                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfNivAceiteCuba) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_NivAceiteCuba"))
                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfNivAceiteAcum) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_NivAceiteAcum"))
                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfPresAperValEsf) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_PresAperVE"))
                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfPresRetSelloTrabajo) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_PresRetSelloTraba"))
                        (row.findViewById<View>(R.id.txtSistOleoU2ValEsfPresSetSelloMtto) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_VE_PresRetSelloMtto"))
                        (row.findViewById<View>(R.id.txtSistOleoU2RegVelPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_RV_PresSist"))
                        (row.findViewById<View>(R.id.txtSistOleoU2RegVelPresNitro) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_RV_PresNitro"))
                        (row.findViewById<View>(R.id.txtSistOleoU2RegVelNivAceiteAcum) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_RV_NivAceiteAcum"))
                        (row.findViewById<View>(R.id.txtSistOleoU2RegVelTempCubaAceite) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_RV_TempCubaAceite"))
                        (row.findViewById<View>(R.id.txtSistOleoU2RegVelPresEntradaDist) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_RV_PresEntDistri"))
                        (row.findViewById<View>(R.id.txtSistOleoU2RegVelPresFosoTurbi) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_RV_PresFosoTurbi"))
                        (row.findViewById<View>(R.id.txtSistOleoU2InyApertIny1) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeIny1"))
                        (row.findViewById<View>(R.id.txtSistOleoU2InyApertIny2) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeIny2"))
                        (row.findViewById<View>(R.id.txtSistOleoU2InyApertIny3) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeIny3"))
                        (row.findViewById<View>(R.id.txtSistOleoU2InyApertIny4) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeIny4"))
                        (row.findViewById<View>(R.id.txtSistOleoU2InyApertIny5) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeIny5"))
                        (row.findViewById<View>(R.id.txtSistOleoU2InyApertIny6) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeIny6"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefApertDef1) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeDef1"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefApertDef2) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeDef2"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefApertDef3) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeDef3"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefApertDef4) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeDef4"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefApertDef5) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeDef5"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefApertDef6) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_ApeDef6"))
                        (row.findViewById<View>(R.id.txtSistOleoU2DefPotUni) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_Def_PotUni"))
                        (row.findViewById<View>(R.id.txtSistOleoU2FrenosPresTanq) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_Freno_PresTanq"))
                        (row.findViewById<View>(R.id.txtSistOleoU2FrenosPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U2_Freno_PresSist"))

                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_PresSist"))
                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfPresNitro) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_PresNitro"))
                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfNivAceiteCuba) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_NivAceiteCuba"))
                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfNivAceiteAcum) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_NivAceiteAcum"))
                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfPresAperValEsf) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_PresAperVE"))
                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfPresRetSelloTrabajo) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_PresRetSelloTraba"))
                        (row.findViewById<View>(R.id.txtSistOleoU3ValEsfPresSetSelloMtto) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_VE_PresRetSelloMtto"))
                        (row.findViewById<View>(R.id.txtSistOleoU3RegVelPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_RV_PresSist"))
                        (row.findViewById<View>(R.id.txtSistOleoU3RegVelPresNitro) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_RV_PresNitro"))
                        (row.findViewById<View>(R.id.txtSistOleoU3RegVelNivAceiteAcum) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_RV_NivAceiteAcum"))
                        (row.findViewById<View>(R.id.txtSistOleoU3RegVelTempCubaAceite) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_RV_TempCubaAceite"))
                        (row.findViewById<View>(R.id.txtSistOleoU3RegVelPresEntradaDist) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_RV_PresEntDistri"))
                        (row.findViewById<View>(R.id.txtSistOleoU3RegVelPresFosoTurbi) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_RV_PresFosoTurbi"))
                        (row.findViewById<View>(R.id.txtSistOleoU3InyApertIny1) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeIny1"))
                        (row.findViewById<View>(R.id.txtSistOleoU3InyApertIny2) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeIny2"))
                        (row.findViewById<View>(R.id.txtSistOleoU3InyApertIny3) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeIny3"))
                        (row.findViewById<View>(R.id.txtSistOleoU3InyApertIny4) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeIny4"))
                        (row.findViewById<View>(R.id.txtSistOleoU3InyApertIny5) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeIny5"))
                        (row.findViewById<View>(R.id.txtSistOleoU3InyApertIny6) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeIny6"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefApertDef1) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeDef1"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefApertDef2) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeDef2"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefApertDef3) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeDef3"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefApertDef4) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeDef4"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefApertDef5) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeDef5"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefApertDef6) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_ApeDef6"))
                        (row.findViewById<View>(R.id.txtSistOleoU3DefPotUni) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_Def_PotUni"))
                        (row.findViewById<View>(R.id.txtSistOleoU3FrenosPresTanq) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_Freno_PresTanq"))
                        (row.findViewById<View>(R.id.txtSistOleoU3FrenosPresSist) as TextView).text = elementos.getString(elementos.getColumnIndex("U3_Freno_PresSist"))

                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table.requestLayout()
                dbHandler.closeDatabase()
            }
            4 -> { // NivelesAceiteCubaCojinetes
/*
Niv_Aceite_Coj_Emp_U1
Niv_Aceite_Coj_Sup_U1
Niv_Aceite_Coj_Inf_U1
Niv_Aceite_Coj_Turb_U1
Niv_Aceite_Coj_Emp_U2
Niv_Aceite_Coj_Sup_U2
Niv_Aceite_Coj_Inf_U2
Niv_Aceite_Coj_Turb_U2
Niv_Aceite_Coj_Emp_U3
Niv_Aceite_Coj_Sup_U3
Niv_Aceite_Coj_Inf_U3
Niv_Aceite_Coj_Turb_U3
*
* */
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_niveles_aceite_cuba_cojinetes, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU1Fecha) as TextView).text=("Fecha")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU1Hora) as TextView).text=("Hora")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU1CojEmpNivAceite) as TextView).text=("Niv_Aceite_Coj_Emp_U1")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU1CojSupNivAceite) as TextView).text=("Niv_Aceite_Coj_Sup_U1")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU1CojInfNivAceite) as TextView).text=("Niv_Aceite_Coj_Inf_U1")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU1CojTurbNivAceite) as TextView).text=("Niv_Aceite_Coj_Turb_U1")

                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU2CojEmpNivAceite) as TextView).text=("Niv_Aceite_Coj_Emp_U2")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU2CojSupNivAceite) as TextView).text=("Niv_Aceite_Coj_Sup_U2")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU2CojInfNivAceite) as TextView).text=("Niv_Aceite_Coj_Inf_U2")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU2CojTurbNivAceite) as TextView).text=("Niv_Aceite_Coj_Turb_U2")

                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU3CojEmpNivAceite) as TextView).text=("Niv_Aceite_Coj_Emp_U3")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU3CojSupNivAceite) as TextView).text=("Niv_Aceite_Coj_Sup_U3")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU3CojInfNivAceite) as TextView).text=("Niv_Aceite_Coj_Inf_U3")
                (rowHead.findViewById<View>(R.id.txtNivAceCubaCojU3CojTurbNivAceite) as TextView).text=("Niv_Aceite_Coj_Turb_U3")
                //rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                //rowHead.textDirection = ""
                cabtable!!.addView(rowHead)
                dbHandler.openDatabase()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                elementos = dbHandler.getRptSistema( posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    do {
                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_niveles_aceite_cuba_cojinetes,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtNivAceCubaCojU1Fecha) as TextView).text =
                            output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU1Hora) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("HoraInicio"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU1CojEmpNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Emp_U1"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU1CojSupNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Sup_U1"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU1CojInfNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Inf_U1"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU1CojTurbNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Turb_U1"))

                        (row.findViewById<View>(R.id.txtNivAceCubaCojU2CojEmpNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Emp_U2"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU2CojSupNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Sup_U2"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU2CojInfNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Inf_U2"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU2CojTurbNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Turb_U2"))

                        (row.findViewById<View>(R.id.txtNivAceCubaCojU3CojEmpNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Emp_U3"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU3CojSupNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Sup_U3"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU3CojInfNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Inf_U3"))
                        (row.findViewById<View>(R.id.txtNivAceCubaCojU3CojTurbNivAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("Niv_Aceite_Coj_Turb_U3"))
                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table!!.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table!!.requestLayout()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                dbHandler.closeDatabase()
            }
            5 -> { // TrafoPrincipal
/*
* 
* U1_BombaInter
U1_TransNivelAceite
U1_FlujoAguaEntrada
U1_PresAguaEntrada
U1_TemAceiteInterc
U1_FAceite_Bomba1
U1_FAceite_Bomba2
U1_FAceite_Bomba3
U1_TempBarraFA
U1_TempBarraFB
U1_TempBarraFC
U1_TransTempAceite1
U1_TransTempAceite2
U1_TransTempDevanado

* U2_BombaInter
U2_TransNivelAceite
U2_FlujoAguaEntrada
U2_PresAguaEntrada
U2_TemAceiteInterc
U2_FAceite_Bomba1
U2_FAceite_Bomba2
U2_FAceite_Bomba3
U2_TempBarraFA
U2_TempBarraFB
U2_TempBarraFC
U2_TransTempAceite1
U2_TransTempAceite2
U2_TransTempDevanado

* U3_BombaInter
U3_TransNivelAceite
U3_FlujoAguaEntrada
U3_PresAguaEntrada
U3_TemAceiteInterc
U3_FAceite_Bomba1
U3_FAceite_Bomba2
U3_FAceite_Bomba3
U3_TempBarraFA
U3_TempBarraFB
U3_TempBarraFC
U3_TransTempAceite1
U3_TransTempAceite2
U3_TransTempDevanado
* 
* */
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_trafo_principal, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1Fecha) as TextView).text=("Fecha")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1Hora) as TextView).text=("Hora")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1Bomba) as TextView).text=("U1_BombaInter")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1NivelAceite) as TextView).text=("U1_TransNivelAceite")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAguaEntrada) as TextView).text=("U1_FlujoAguaEntrada")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1PresAguaEntrada) as TextView).text=("U1_PresAguaEntrada")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempAceiteInterc) as TextView).text=("U1_TemAceiteInterc")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAceiteBombInter1) as TextView).text=("U1_FAceite_Bomba1")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAceiteBombInter2) as TextView).text=("U1_FAceite_Bomba2")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAceiteBombInter3) as TextView).text=("U1_FAceite_Bomba3")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempBarraFaseA) as TextView).text=("U1_TempBarraFA")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempBarraFaseB) as TextView).text=("U1_TempBarraFB")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempBarraFaseC) as TextView).text=("U1_TempBarraFC")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempAceite1) as TextView).text=("U1_TransTempAceite1")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempAceite2) as TextView).text=("U1_TransTempAceite2")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU1TempDevanado) as TextView).text=("U1_TransTempDevanado")

                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2Bomba) as TextView).text=("U2_BombaInter")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2NivelAceite) as TextView).text=("U2_TransNivelAceite")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAguaEntrada) as TextView).text=("U2_FlujoAguaEntrada")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2PresAguaEntrada) as TextView).text=("U2_PresAguaEntrada")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempAceiteInterc) as TextView).text=("U2_TemAceiteInterc")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAceiteBombInter1) as TextView).text=("U2_FAceite_Bomba1")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAceiteBombInter2) as TextView).text=("U2_FAceite_Bomba2")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAceiteBombInter3) as TextView).text=("U2_FAceite_Bomba3")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempBarraFaseA) as TextView).text=("U2_TempBarraFA")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempBarraFaseB) as TextView).text=("U2_TempBarraFB")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempBarraFaseC) as TextView).text=("U2_TempBarraFC")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempAceite1) as TextView).text=("U2_TransTempAceite1")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempAceite2) as TextView).text=("U2_TransTempAceite2")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU2TempDevanado) as TextView).text=("U2_TransTempDevanado")

                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3Bomba) as TextView).text=("U3_BombaInter")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3NivelAceite) as TextView).text=("U3_TransNivelAceite")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAguaEntrada) as TextView).text=("U3_FlujoAguaEntrada")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3PresAguaEntrada) as TextView).text=("U3_PresAguaEntrada")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempAceiteInterc) as TextView).text=("U3_TemAceiteInterc")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAceiteBombInter1) as TextView).text=("U3_FAceite_Bomba1")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAceiteBombInter2) as TextView).text=("U3_FAceite_Bomba2")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAceiteBombInter3) as TextView).text=("U3_FAceite_Bomba3")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempBarraFaseA) as TextView).text=("U3_TempBarraFA")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempBarraFaseB) as TextView).text=("U3_TempBarraFB")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempBarraFaseC) as TextView).text=("U3_TempBarraFC")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempAceite1) as TextView).text=("U3_TransTempAceite1")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempAceite2) as TextView).text=("U3_TransTempAceite2")
                (rowHead.findViewById<View>(R.id.txtTrafoPrincipalU3TempDevanado) as TextView).text=("U3_TransTempDevanado")
                //rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                //rowHead.textDirection = ""
                cabtable!!.addView(rowHead)
                dbHandler.openDatabase()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                elementos = dbHandler.getRptSistema(posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    do {
                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_trafo_principal,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1Fecha) as TextView).text =
                            output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1Hora) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("HoraInicio"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1Bomba) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_BombaInter"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1NivelAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TransNivelAceite"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAguaEntrada) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_FlujoAguaEntrada"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1PresAguaEntrada) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_PresAguaEntrada"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempAceiteInterc) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TemAceiteInterc"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAceiteBombInter1) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_FAceite_Bomba1"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAceiteBombInter2) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_FAceite_Bomba2"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1FlujoAceiteBombInter3) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_FAceite_Bomba3"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempBarraFaseA) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TempBarraFA"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempBarraFaseB) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TempBarraFB"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempBarraFaseC) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TempBarraFC"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempAceite1) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TransTempAceite1"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempAceite2) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TransTempAceite2"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU1TempDevanado) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U1_TransTempDevanado"))

                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2Bomba) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_BombaInter"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2NivelAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TransNivelAceite"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAguaEntrada) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_FlujoAguaEntrada"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2PresAguaEntrada) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_PresAguaEntrada"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempAceiteInterc) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TemAceiteInterc"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAceiteBombInter1) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_FAceite_Bomba1"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAceiteBombInter2) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_FAceite_Bomba2"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2FlujoAceiteBombInter3) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_FAceite_Bomba3"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempBarraFaseA) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TempBarraFA"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempBarraFaseB) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TempBarraFB"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempBarraFaseC) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TempBarraFC"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempAceite1) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TransTempAceite1"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempAceite2) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TransTempAceite2"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU2TempDevanado) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U2_TransTempDevanado"))

                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3Bomba) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_BombaInter"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3NivelAceite) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TransNivelAceite"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAguaEntrada) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_FlujoAguaEntrada"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3PresAguaEntrada) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_PresAguaEntrada"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempAceiteInterc) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TemAceiteInterc"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAceiteBombInter1) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_FAceite_Bomba1"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAceiteBombInter2) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_FAceite_Bomba2"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3FlujoAceiteBombInter3) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_FAceite_Bomba3"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempBarraFaseA) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TempBarraFA"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempBarraFaseB) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TempBarraFB"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempBarraFaseC) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TempBarraFC"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempAceite1) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TransTempAceite1"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempAceite2) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TransTempAceite2"))
                        (row.findViewById<View>(R.id.txtTrafoPrincipalU3TempDevanado) as TextView).text=
                            elementos.getString(elementos.getColumnIndex("U3_TransTempDevanado"))

                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table!!.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table!!.requestLayout()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                dbHandler.closeDatabase()
            }
            6 -> { // TransformadoresSSAA
/*
* 
* TSA_1_FA
TSA_1_FB
TSA_1_FC
TSA_2_FA
TSA_2_FB
TSA_2_FC
TSA_3_FA
TSA_3_FB
TSA_3_FC
TSA_4_FA
TSA_4_FB
TSA_4_FC
TE_1_FA
TE_1_FB
TE_1_FC
TE_2_FA
TE_2_FB
TE_2_FC
TE_3_FA
TE_3_FB
TE_3_FC
* 
* */
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_transformadores_s_s_a_a, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtTrafoSSAAFecha) as TextView).text=("Fecha")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAAHora) as TextView).text=("Hora")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA1FaseA) as TextView).text=("TSA_1_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA1FaseB) as TextView).text=("TSA_1_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA1FaseC) as TextView).text=("TSA_1_FC")

                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA2FaseA) as TextView).text=("TSA_2_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA2FaseB) as TextView).text=("TSA_2_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA2FaseC) as TextView).text=("TSA_2_FC")

                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA3FaseA) as TextView).text=("TSA_3_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA3FaseB) as TextView).text=("TSA_3_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA3FaseC) as TextView).text=("TSA_3_FC")

                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA4FaseA) as TextView).text=("TSA_4_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA4FaseB) as TextView).text=("TSA_4_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATSA4FaseC) as TextView).text=("TSA_4_FC")

                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE1FaseA) as TextView).text=("TE_1_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE1FaseB) as TextView).text=("TE_1_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE1FaseC) as TextView).text=("TE_1_FC")

                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE2FaseA) as TextView).text=("TE_2_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE2FaseB) as TextView).text=("TE_2_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE2FaseC) as TextView).text=("TE_2_FC")

                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE3FaseA) as TextView).text=("TE_3_FA")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE3FaseB) as TextView).text=("TE_3_FB")
                (rowHead.findViewById<View>(R.id.txtTrafoSSAATE3FaseC) as TextView).text=("TE_3_FC")

                //rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                //rowHead.textDirection = ""
                cabtable!!.addView(rowHead)
                dbHandler.openDatabase()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                elementos = dbHandler.getRptSistema( posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    do {
                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_transformadores_s_s_a_a,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtTrafoSSAAFecha) as TextView).text =
                            output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtTrafoSSAAHora) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("HoraInicio"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA1FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_1_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA1FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_1_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA1FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_1_FC"))

                        (row.findViewById<View>(R.id.txtTrafoSSAATSA2FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_2_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA2FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_2_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA2FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_2_FC"))

                        (row.findViewById<View>(R.id.txtTrafoSSAATSA3FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_3_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA3FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_3_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA3FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_3_FC"))

                        (row.findViewById<View>(R.id.txtTrafoSSAATSA4FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_4_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA4FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_4_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATSA4FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TSA_4_FC"))

                        (row.findViewById<View>(R.id.txtTrafoSSAATE1FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_1_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATE1FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_1_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATE1FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_1_FC"))

                        (row.findViewById<View>(R.id.txtTrafoSSAATE2FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_2_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATE2FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_2_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATE2FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_2_FC"))

                        (row.findViewById<View>(R.id.txtTrafoSSAATE3FaseA) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_3_FA"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATE3FaseB) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_3_FB"))
                        (row.findViewById<View>(R.id.txtTrafoSSAATE3FaseC) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("TE_3_FC"))

                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table!!.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table!!.requestLayout()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                dbHandler.closeDatabase()
            }
            7 -> { // Mag.Enfri.Unidades
/*
*
*














* Circu_Agua_U02
Bomba_U02
Pres_Agu_Ra_U02
Flu_Agu_Ent_02
Pre_Ent_02
V_R_P_Ent_U02
V_R_P_Sal_U02
F_A_Ent_U02
F_A_Sal_U02
Dis_Ent_U02
Dis_Sal_U02
F_A_S_E_U02
F_A_S_S_U02
Rad_Pre_Ent_A_U02
Flu_Agu_Ent_U02

* Circu_Agua_U03
Bomba_U03
Pres_Agu_Ra_U03
Flu_Agu_Ent_03
Pre_Ent_03
V_R_P_Ent_U03
V_R_P_Sal_U03
F_A_Ent_U03
F_A_Sal_U03
Dis_Ent_U03
Dis_Sal_U03
F_A_S_E_U03
F_A_S_S_U03
Rad_Pre_Ent_A_U03
Flu_Agu_Ent_U03
*
* */
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_mag_enfri_unidades, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtSistEnfPSFecha) as TextView).text=("Fecha")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSHora) as TextView).text=("Hora")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Circu_Agua_U01) as TextView).text=("Circu_Agua_U01")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Bomba_U01) as TextView).text=("Bomba_U01")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Pres_Agu_Ra_U01) as TextView).text=("Pres_Agu_Ra_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Flu_Agu_Ent_01) as TextView).text=("Flu_Agu_Ent_01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Pre_Ent_01) as TextView).text=("Pre_Ent_01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1V_R_P_Ent_U01) as TextView).text=("V_R_P_Ent_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1V_R_P_Sal_U01) as TextView).text=("V_R_P_Sal_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1F_A_Ent_U01) as TextView).text=("F_A_Ent_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1F_A_Sal_U01) as TextView).text=("F_A_Sal_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Dis_Ent_U01) as TextView).text=("Dis_Ent_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Dis_Sal_U01) as TextView).text=("Dis_Sal_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1F_A_S_E_U01) as TextView).text=("F_A_S_E_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1F_A_S_S_U01) as TextView).text=("F_A_S_S_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Rad_Pre_Ent_A_U01) as TextView).text=("Rad_Pre_Ent_A_U01" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU1Flu_Agu_Ent_U01) as TextView).text=("Flu_Agu_Ent_U01" )

                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Circu_Agua_U02) as TextView).text=("Circu_Agua_U02")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Bomba_U02) as TextView).text=("Bomba_U02")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Pres_Agu_Ra_U02) as TextView).text=("Pres_Agu_Ra_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Flu_Agu_Ent_02) as TextView).text=("Flu_Agu_Ent_02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Pre_Ent_02) as TextView).text=("Pre_Ent_02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2V_R_P_Ent_U02) as TextView).text=("V_R_P_Ent_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2V_R_P_Sal_U02) as TextView).text=("V_R_P_Sal_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2F_A_Ent_U02) as TextView).text=("F_A_Ent_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2F_A_Sal_U02) as TextView).text=("F_A_Sal_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Dis_Ent_U02) as TextView).text=("Dis_Ent_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Dis_Sal_U02) as TextView).text=("Dis_Sal_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2F_A_S_E_U02) as TextView).text=("F_A_S_E_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2F_A_S_S_U02) as TextView).text=("F_A_S_S_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Rad_Pre_Ent_A_U02) as TextView).text=("Rad_Pre_Ent_A_U02" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU2Flu_Agu_Ent_U02) as TextView).text=("Flu_Agu_Ent_U02" )

                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Circu_Agua_U03) as TextView).text=("Circu_Agua_U03")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Bomba_U03) as TextView).text=("Bomba_U03")
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Pres_Agu_Ra_U03) as TextView).text=("Pres_Agu_Ra_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Flu_Agu_Ent_03) as TextView).text=("Flu_Agu_Ent_03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Pre_Ent_03) as TextView).text=("Pre_Ent_03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3V_R_P_Ent_U03) as TextView).text=("V_R_P_Ent_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3V_R_P_Sal_U03) as TextView).text=("V_R_P_Sal_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3F_A_Ent_U03) as TextView).text=("F_A_Ent_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3F_A_Sal_U03) as TextView).text=("F_A_Sal_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Dis_Ent_U03) as TextView).text=("Dis_Ent_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Dis_Sal_U03) as TextView).text=("Dis_Sal_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3F_A_S_E_U03) as TextView).text=("F_A_S_E_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3F_A_S_S_U03) as TextView).text=("F_A_S_S_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Rad_Pre_Ent_A_U03) as TextView).text=("Rad_Pre_Ent_A_U03" )
                (rowHead.findViewById<View>(R.id.txtSistEnfPSU3Flu_Agu_Ent_U03) as TextView).text=("Flu_Agu_Ent_U03" )

                //rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                //rowHead.textDirection = ""
                cabtable!!.addView(rowHead)
                dbHandler.openDatabase()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                elementos = dbHandler.getRptSistema( posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    do {
                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_mag_enfri_unidades,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtSistEnfPSFecha) as TextView).text =
                            output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtSistEnfPSHora) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("HoraInicio"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Circu_Agua_U01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Circu_Agua_U01"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Bomba_U01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Bomba_U01"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Pres_Agu_Ra_U01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Pres_Agu_Ra_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Flu_Agu_Ent_01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("Flu_Agu_Ent_01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Pre_Ent_01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("Pre_Ent_01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1V_R_P_Ent_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("V_R_P_Ent_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1V_R_P_Sal_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("V_R_P_Sal_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1F_A_Ent_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("F_A_Ent_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1F_A_Sal_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("F_A_Sal_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Dis_Ent_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("Dis_Ent_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Dis_Sal_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("Dis_Sal_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1F_A_S_E_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("F_A_S_E_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1F_A_S_S_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("F_A_S_S_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Rad_Pre_Ent_A_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("Rad_Pre_Ent_A_U01" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU1Flu_Agu_Ent_U01) as TextView).text =
                             elementos.getString(elementos.getColumnIndex("Flu_Agu_Ent_U01" ))

                        (row.findViewById<View>(R.id.txtSistEnfPSU2Circu_Agua_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Circu_Agua_U02"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Bomba_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Bomba_U02"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Pres_Agu_Ra_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Pres_Agu_Ra_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Flu_Agu_Ent_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Flu_Agu_Ent_02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Pre_Ent_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Pre_Ent_02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2V_R_P_Ent_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("V_R_P_Ent_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2V_R_P_Sal_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("V_R_P_Sal_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2F_A_Ent_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_Ent_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2F_A_Sal_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_Sal_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Dis_Ent_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Dis_Ent_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Dis_Sal_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Dis_Sal_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2F_A_S_E_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_S_E_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2F_A_S_S_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_S_S_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Rad_Pre_Ent_A_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Rad_Pre_Ent_A_U02" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU2Flu_Agu_Ent_U02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Flu_Agu_Ent_U02" ))

                        (row.findViewById<View>(R.id.txtSistEnfPSU3Circu_Agua_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Circu_Agua_U03"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Bomba_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Bomba_U03"))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Pres_Agu_Ra_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Pres_Agu_Ra_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Flu_Agu_Ent_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Flu_Agu_Ent_03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Pre_Ent_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Pre_Ent_03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3V_R_P_Ent_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("V_R_P_Ent_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3V_R_P_Sal_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("V_R_P_Sal_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3F_A_Ent_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_Ent_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3F_A_Sal_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_Sal_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Dis_Ent_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Dis_Ent_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Dis_Sal_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Dis_Sal_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3F_A_S_E_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_S_E_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3F_A_S_S_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("F_A_S_S_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Rad_Pre_Ent_A_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Rad_Pre_Ent_A_U03" ))
                        (row.findViewById<View>(R.id.txtSistEnfPSU3Flu_Agu_Ent_U03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Flu_Agu_Ent_U03" ))


                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table!!.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table!!.requestLayout()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                dbHandler.closeDatabase()
            }
            8 -> { // NingunSistema

            }
            9 -> { // Temp_Pres_Flu_Inter_Calor
/*
*
* Coj_Emp_Temp_Ent_01
Coj_Emp_Temp_Sal_01
Coj_Emp_Temp_M1_01
Coj_Emp_Temp_M2_01
Coj_Emp_Flu_Ag_01
Coj_GSup_Temp_Ent_01
Coj_GSup_Temp_Sal_01
Coj_GSup_Temp_M1_01
Coj_GSup_Temp_M2_01
Coj_GSup_Flu_Ag_01
Coj_GInf_Temp_Ent_01
Coj_GInf_Temp_Sal_01
Coj_GInf_Temp_M1_01
Coj_GInf_Temp_M2_01
Coj_GInf_Flu_Ag_01
Coj_GTur_Temp_Ent_01
Coj_GTur_Temp_Sal_01
Coj_GTur_Temp_M1_01
Coj_GTur_Temp_M2_01
Coj_GTur_Flu_Ag_01
*
Coj_Emp_Temp_Ent_02
Coj_Emp_Temp_Sal_02
Coj_Emp_Temp_M1_02
Coj_Emp_Temp_M2_02
Coj_Emp_Flu_Ag_02
Coj_GSup_Temp_Ent_02
Coj_GSup_Temp_Sal_02
Coj_GSup_Temp_M1_02
Coj_GSup_Temp_M2_02
Coj_GSup_Flu_Ag_02
Coj_GInf_Temp_Ent_02
Coj_GInf_Temp_Sal_02
Coj_GInf_Temp_M1_02
Coj_GInf_Temp_M2_02
Coj_GInf_Flu_Ag_02
Coj_GTur_Temp_Ent_02
Coj_GTur_Temp_Sal_02
Coj_GTur_Temp_M1_02
Coj_GTur_Temp_M2_02
Coj_GTur_Flu_Ag_02
 */
                rowHead = LayoutInflater.from(this).inflate(R.layout.fragment_sistema_temp_pres_flu_inter_calor, rootCabecera) as TableRow
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorFecha) as TextView).text=("Fecha")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorHora) as TextView).text=("Hora")

                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Ent_01) as TextView).text=("Coj_Emp_Temp_Ent_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Sal_01) as TextView).text=("Coj_Emp_Temp_Sal_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M1_01) as TextView).text=("Coj_Emp_Temp_M1_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M2_01) as TextView).text=("Coj_Emp_Temp_M2_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Flu_Ag_01) as TextView).text=("Coj_Emp_Flu_Ag_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Ent_01) as TextView).text=("Coj_GSup_Temp_Ent_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Sal_01) as TextView).text=("Coj_GSup_Temp_Sal_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M1_01) as TextView).text=("Coj_GSup_Temp_M1_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M2_01) as TextView).text=("Coj_GSup_Temp_M2_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Flu_Ag_01) as TextView).text=("Coj_GSup_Flu_Ag_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Ent_01) as TextView).text=("Coj_GInf_Temp_Ent_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Sal_01) as TextView).text=("Coj_GInf_Temp_Sal_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M1_01) as TextView).text=("Coj_GInf_Temp_M1_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M2_01) as TextView).text=("Coj_GInf_Temp_M2_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Flu_Ag_01) as TextView).text=("Coj_GInf_Flu_Ag_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Ent_01) as TextView).text=("Coj_GTur_Temp_Ent_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Sal_01) as TextView).text=("Coj_GTur_Temp_Sal_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M1_01) as TextView).text=("Coj_GTur_Temp_M1_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M2_01) as TextView).text=("Coj_GTur_Temp_M2_01")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Flu_Ag_01) as TextView).text=("Coj_GTur_Flu_Ag_01")

                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Ent_02) as TextView).text=("Coj_Emp_Temp_Ent_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Sal_02) as TextView).text=("Coj_Emp_Temp_Sal_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M1_02) as TextView).text=("Coj_Emp_Temp_M1_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M2_02) as TextView).text=("Coj_Emp_Temp_M2_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Flu_Ag_02) as TextView).text=("Coj_Emp_Flu_Ag_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Ent_02) as TextView).text=("Coj_GSup_Temp_Ent_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Sal_02) as TextView).text=("Coj_GSup_Temp_Sal_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M1_02) as TextView).text=("Coj_GSup_Temp_M1_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M2_02) as TextView).text=("Coj_GSup_Temp_M2_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Flu_Ag_02) as TextView).text=("Coj_GSup_Flu_Ag_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Ent_02) as TextView).text=("Coj_GInf_Temp_Ent_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Sal_02) as TextView).text=("Coj_GInf_Temp_Sal_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M1_02) as TextView).text=("Coj_GInf_Temp_M1_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M2_02) as TextView).text=("Coj_GInf_Temp_M2_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Flu_Ag_02) as TextView).text=("Coj_GInf_Flu_Ag_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Ent_02) as TextView).text=("Coj_GTur_Temp_Ent_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Sal_02) as TextView).text=("Coj_GTur_Temp_Sal_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M1_02) as TextView).text=("Coj_GTur_Temp_M1_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M2_02) as TextView).text=("Coj_GTur_Temp_M2_02")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Flu_Ag_02) as TextView).text=("Coj_GTur_Flu_Ag_02")

                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Ent_03) as TextView).text=("Coj_Emp_Temp_Ent_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Sal_03) as TextView).text=("Coj_Emp_Temp_Sal_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M1_03) as TextView).text=("Coj_Emp_Temp_M1_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M2_03) as TextView).text=("Coj_Emp_Temp_M2_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Flu_Ag_03) as TextView).text=("Coj_Emp_Flu_Ag_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Ent_03) as TextView).text=("Coj_GSup_Temp_Ent_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Sal_03) as TextView).text=("Coj_GSup_Temp_Sal_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M1_03) as TextView).text=("Coj_GSup_Temp_M1_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M2_03) as TextView).text=("Coj_GSup_Temp_M2_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Flu_Ag_03) as TextView).text=("Coj_GSup_Flu_Ag_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Ent_03) as TextView).text=("Coj_GInf_Temp_Ent_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Sal_03) as TextView).text=("Coj_GInf_Temp_Sal_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M1_03) as TextView).text=("Coj_GInf_Temp_M1_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M2_03) as TextView).text=("Coj_GInf_Temp_M2_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Flu_Ag_03) as TextView).text=("Coj_GInf_Flu_Ag_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Ent_03) as TextView).text=("Coj_GTur_Temp_Ent_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Sal_03) as TextView).text=("Coj_GTur_Temp_Sal_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M1_03) as TextView).text=("Coj_GTur_Temp_M1_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M2_03) as TextView).text=("Coj_GTur_Temp_M2_03")
                (rowHead.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Flu_Ag_03) as TextView).text=("Coj_GTur_Flu_Ag_03")


                //rowHead.isHorizontalScrollBarEnabled = true
                //rowHead.textDirection = ""
                //rowHead.textDirection = ""
                cabtable!!.addView(rowHead)
                dbHandler.openDatabase()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                elementos = dbHandler.getRptSistema(posicion, periodo)
                //Log.d("Elementos", elementos.toString())

                if (elementos.count > 0) {
                    elementos.moveToFirst()
                    do {
                        val row = LayoutInflater.from(this).inflate(
                            R.layout.fragment_sistema_temp_pres_flu_inter_calor,
                            rootCuerpo
                        ) as TableRow
                        val parser = SimpleDateFormat("yyyy-MM-dd")
                        val formatter = SimpleDateFormat("dd MMM")
                        //val output: String = formatter.format(parser.parse("2018-12-14T09:55:00"))
                        val output: String = formatter.format(parser.parse(elementos.getString(elementos.getColumnIndex("Fecha"))))

                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorFecha) as TextView).text =
                            output //elementos.getString(elementos.getColumnIndex("Fecha"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorHora) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("HoraInicio"))

                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Ent_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_Ent_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Sal_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_Sal_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M1_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_M1_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M2_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_M2_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Flu_Ag_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Flu_Ag_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Ent_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_Ent_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Sal_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_Sal_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M1_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_M1_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M2_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_M2_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Flu_Ag_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Flu_Ag_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Ent_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_Ent_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Sal_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_Sal_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M1_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_M1_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M2_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_M2_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Flu_Ag_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Flu_Ag_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Ent_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_Ent_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Sal_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_Sal_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M1_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_M1_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M2_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_M2_01"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Flu_Ag_01) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Flu_Ag_01"))

                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Ent_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_Ent_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Sal_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_Sal_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M1_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_M1_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M2_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_M2_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Flu_Ag_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Flu_Ag_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Ent_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_Ent_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Sal_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_Sal_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M1_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_M1_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M2_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_M2_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Flu_Ag_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Flu_Ag_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Ent_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_Ent_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Sal_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_Sal_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M1_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_M1_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M2_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_M2_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Flu_Ag_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Flu_Ag_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Ent_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_Ent_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Sal_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_Sal_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M1_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_M1_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M2_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_M2_02"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Flu_Ag_02) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Flu_Ag_02"))

                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Ent_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_Ent_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_Sal_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_Sal_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M1_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_M1_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Temp_M2_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Temp_M2_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_Emp_Flu_Ag_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_Emp_Flu_Ag_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Ent_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_Ent_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_Sal_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_Sal_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M1_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_M1_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Temp_M2_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Temp_M2_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GSup_Flu_Ag_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GSup_Flu_Ag_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Ent_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_Ent_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_Sal_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_Sal_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M1_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_M1_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Temp_M2_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Temp_M2_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GInf_Flu_Ag_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GInf_Flu_Ag_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Ent_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_Ent_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_Sal_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_Sal_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M1_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_M1_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Temp_M2_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Temp_M2_03"))
                        (row.findViewById<View>(R.id.txtSistPresFluInterCalorCoj_GTur_Flu_Ag_03) as TextView).text =
                            elementos.getString(elementos.getColumnIndex("Coj_GTur_Flu_Ag_03"))

                        //(row.findViewById<View>(R.id.attrib_name) as TextView).text=((i+1).toString())
                        //(row.findViewById<View>(R.id.attrib_value) as TextView).text=(temps[i].toString())
                        table!!.addView(row)
                    } while (elementos.moveToNext())
                    elementos.close()
                }
                table!!.requestLayout()
                findViewById<RelativeLayout>(R.id.loadingPanel).visibility = View.VISIBLE
                dbHandler.closeDatabase()
            }
            10 -> {

            }
            else -> {

            }
        }
    }



}