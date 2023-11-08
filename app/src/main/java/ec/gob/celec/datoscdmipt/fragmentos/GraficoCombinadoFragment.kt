package ec.gob.celec.datoscdmipt.fragmentos

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import ec.gob.celec.datoscdmipt.PrincipalActivity
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.models.actividades
import ec.gob.celec.datoscdmipt.database.models.analogvalue_enj
import ec.gob.celec.datoscdmipt.database.models.stringvalue_enj
import ec.gob.celec.datoscdmipt.helpers.CustomMarker
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private var ARG_LISTAVALORESNUEVOS = JSONArray()
private var ARG_TIPODATO : Int = 0
private var MAX_X_VALUE : Float = 7f
private var MAX_Y_VALUE = 50f
private var MIN_Y_VALUE = 5f
private var MAX_Y_Graf = 0f

/**
 * A simple [Fragment] subclass.
 * Use the [GraficoCombinadoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GraficoCombinadoFragment : Fragment() {


    private var listvaloresnuevos: JSONArray ? = null
    private var tipodato : Int ? = null
    private var ymaximo : Double = 0.0
    private var yminimo : Double = 0.0
    private var xmaximo : Double = 0.0
    private var ymaxgraf : Double = 0.0
    //private var param2: String? = null
    private var SET_LABEL = "Lecturas del Elemento"
    private var FECHASHORAS = ArrayList<String>() //arrayOf(actividades().fecha + " " + actividades().horainicio)

    private var chart: CombinedChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            listvaloresnuevos = ARG_LISTAVALORESNUEVOS
            tipodato = ARG_TIPODATO
            ymaximo = MAX_Y_VALUE.toDouble()
            yminimo = MIN_Y_VALUE.toDouble()
            ymaxgraf = MAX_Y_Graf.toDouble()

            //param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            ec.gob.celec.datoscdmipt.R.layout.fragment_grafico,
            container,
            false
        )

        chart = view.findViewById<CombinedChart>(R.id.chartGraficoCombinado)
        chart!!.description.textColor = Color.WHITE
        chart?.getLegend()!!.textColor = Color.WHITE
        chart?.getLegend()!!.textSize = 12f
        chart!!.drawOrder = arrayOf(
            DrawOrder.BAR, DrawOrder.LINE
        )

        val data: CombinedData = createChartData()
        configureChartAppearance()
        prepareChartData(data)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        arguments?.let {
            listvaloresnuevos = ARG_LISTAVALORESNUEVOS
            tipodato = ARG_TIPODATO
            ymaximo = MAX_Y_VALUE.toDouble()
            yminimo = MIN_Y_VALUE.toDouble()
            //param2 = it.getString(ARG_PARAM2)
        }
        /*arguments?.getBoolean("REPLACE WITH A STRING CONSTANT")?.let {
            isMyBoolean = it
        }*/
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GraficoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(
            listaAnalogValues: ArrayList<analogvalue_enj>?,
            listaStringValues: ArrayList<stringvalue_enj>?,
            tipo: Int, yminimo: Double, ymaximo: Double
        ) =
            GraficoFragment().apply {
                arguments = Bundle().apply {
                    ARG_TIPODATO = tipo
                    ARG_LISTAVALORESNUEVOS = JSONArray()
                    MAX_Y_VALUE = ymaximo.toFloat()
                    MIN_Y_VALUE = yminimo.toFloat()
                    MAX_Y_Graf = ymaximo.toFloat()

                    var tmpmaximo : Double = 0.0
                    var tmpminimo : Double = 0.0
                    when(tipo){
                        1 -> {
                            for (i in 0 until listaAnalogValues!!.size) {
                                var objJason = JSONObject()

                                if (listaAnalogValues[i].ValueOrig > tmpmaximo) tmpmaximo =
                                    listaAnalogValues[i].ValueOrig
                                if (listaAnalogValues[i].ValueOrig < tmpminimo) tmpminimo =
                                    listaAnalogValues[i].ValueOrig

                                objJason.put("MRID", listaAnalogValues[i].ANALOG_MRID)
                                objJason.put("Loctimestamp", listaAnalogValues[i].Loctimestamp)
                                objJason.put("ValueOrig", listaAnalogValues[i].ValueOrig)
                                objJason.put("InsertUser", listaAnalogValues[i].InsertUser)
                                objJason.put("IdActividad", listaAnalogValues[i].IdActividad)

                                ARG_LISTAVALORESNUEVOS.put(objJason)
                            }
                            MAX_Y_VALUE = tmpmaximo.toFloat()
                            MIN_Y_VALUE = tmpminimo.toFloat()
                        }
                        2 -> {
                            for (i in 0 until listaStringValues!!.size) {
                                var objJason = JSONObject()

                                objJason.put("MRID", listaStringValues[i].STRING_MRID)
                                objJason.put("Loctimestamp", listaStringValues[i].Loctimestamp)
                                objJason.put("ValueOrig", listaStringValues[i].ValueOrig)
                                objJason.put("InsertUser", listaStringValues[i].InsertUser)
                                objJason.put("IdActividad", listaStringValues[i].IdActividad)

                                ARG_LISTAVALORESNUEVOS.put(objJason)
                            }
                        }
                    }
                    //putString(ARG_PARAM1, param1)
                    //putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun configureChartAppearance() {
        chart?.getDescription()?.setEnabled(false)
        chart?.setDrawValueAboveBar(false)
        chart?.setGridBackgroundColor(Color.WHITE)
        chart?.setNoDataTextColor(Color.WHITE)
        chart?.setBackgroundColor(Color.DKGRAY)
//        chart?.clearValues()
        chart?.getAxisLeft()!!.setTextColor(Color.WHITE)
        chart?.getAxisLeft()!!.textSize =14f
        chart?.getXAxis()!!.setTextColor(Color.WHITE)
        chart?.getXAxis()!!.textSize =14f
        chart?.getLegend()!!.setTextColor(Color.WHITE)
        chart?.getDescription()!!.setTextColor(Color.WHITE)

        val xAxis: XAxis = chart!!.getXAxis()
        xAxis.setAvoidFirstLastClipping(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(FECHASHORAS)

        xAxis.setLabelRotationAngle(90f)
        xAxis.granularity = 1f

        val axisLeft: YAxis = chart!!.getAxisLeft()
        axisLeft.granularity = 10f //MAX_Y_VALUE + (MAX_Y_VALUE/5) //
        axisLeft.axisMinimum = 0f
        /*val axisRight: YAxis = chart!!.getAxisRight()
        axisRight.granularity = 10f
        axisRight.axisMinimum = 0f*/
    }

    private fun createChartData(): CombinedData {
        val values: ArrayList<BarEntry> = ArrayList()
        val valuelast: ArrayList<BarEntry> = ArrayList()
        var avalue : JSONObject //analogvalue_enj
        //var valoresx = ArrayList<String>()
        var actividad: actividades
        val dataSets: ArrayList<IBarDataSet> = ArrayList()
        //obtiene el maximo en y
        var comdatasets = CombinedData()

        try{
//            chart?.clearValues()

            Log.d("En grafico", listvaloresnuevos!!.length().toString())

            for (i in 0 until listvaloresnuevos!!.length()-1){
                avalue = listvaloresnuevos!![i] as JSONObject
                var tmp : Double = avalue.get("ValueOrig") as Double
                val x = i.toFloat()
                val y: Float =
                    tmp.toFloat()  //randomFloatBetween(MIN_Y_VALUE, MAX_Y_VALUE)

                actividad = PrincipalActivity.dbHandler.getActividad(
                    this.requireContext(),
                    avalue.get("Loctimestamp") as String
                )

                //valoresx.add(avalue.get("Loctimestamp") as String)
                FECHASHORAS.add(actividad.fecha + " " + actividad.horainicio)

                if(avalue.get("IdActividad") != 0){
                    // obtiene la actividad y coge la fecha y la hora
                } else {
                    // verifica la fecha y hora de período según el loctimestamp
                }
                values.add(BarEntry(x, y))
            }
            /*
            * obtiene el dato para la última columna
            * */
            avalue = listvaloresnuevos!![listvaloresnuevos!!.length() - 1] as JSONObject
            var tmp : Double = avalue.get("ValueOrig") as Double
            val x = (listvaloresnuevos!!.length()-1).toFloat()
            val y: Float =
                tmp.toFloat()  //randomFloatBetween(MIN_Y_VALUE, MAX_Y_VALUE)

            actividad = PrincipalActivity.dbHandler.getActividad(
                this.requireContext(),
                avalue.get("Loctimestamp") as String
            )

            //valoresx.add(avalue.get("Loctimestamp") as String)
            FECHASHORAS.add(actividad.fecha + " " + actividad.horainicio)

            if(avalue.get("IdActividad") != 0){
                // obtiene la actividad y coge la fecha y la hora
            } else {
                // verifica la fecha y hora de período según el loctimestamp
            }
            valuelast.add(BarEntry(x, y))

/*        for (i in 0 until MAX_X_VALUE.toInt()) {
            val x = i.toFloat()
            val y: Float = randomFloatBetween(MIN_Y_VALUE, MAX_Y_VALUE)
            values.add(BarEntry(x, y))
        }*/

            val set1 = BarDataSet(values, SET_LABEL)
            val set2 = BarDataSet(valuelast, "")

            set2.setColor(Color.rgb(60, 220, 78))

            // cargaría aquí la línea para el gráfico combinado

            dataSets.add(set1)
            dataSets.add(set2)
            //dataSets.setBarWidth(0.9f)

        } catch (e: Exception){
            e.printStackTrace()
        }

        var d = BarData(dataSets)
        d.setBarWidth(0.9f)
        //d.setFitBars(true)

        comdatasets.setData(d)
        comdatasets.setData(generateLineData())

        return comdatasets //BarData(dataSets)

    }

    private fun prepareChartData(data: CombinedData) {
        data.setValueTextSize(12f)
//        chart?.clearValues()
        val markerView = CustomMarker(this.requireContext(), R.layout.marker_view)

        //data.setBarWidth(0.9f)
        chart!!.data = data
        chart!!.setNoDataText("Sin lecturas!")
        chart!!.animateX(1000, Easing.EaseInElastic)
        chart!!.setTouchEnabled(true)
        chart!!.setPinchZoom(true)
        chart!!.marker = markerView
        var ll : LimitLine
        ll = LimitLine(MAX_Y_Graf.toFloat(), "Máximo: " + MAX_Y_Graf.toString())

        ll.setLineWidth(4f)
        ll.setTextSize(12f)

        chart!!.xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        chart!!.xAxis.setDrawGridLines(false)
        //chart!!.xAxis.setCenterAxisLabels(true)

        //chart!!.setFitBars(true)
        //chart!!.getXAxis().setLabelCount(FECHASHORAS.size, true)
        chart!!.setMaxVisibleValueCount(10)
        chart!!.getAxisLeft().addLimitLine(ll)
        if(MAX_Y_Graf > MAX_Y_VALUE) {
            chart!!.getAxisLeft().axisMaximum = MAX_Y_Graf + (MAX_Y_Graf /5)
        } else {
            chart!!.getAxisLeft().axisMaximum = MAX_Y_VALUE + (MAX_Y_VALUE /5)
        }

        chart!!.getAxisLeft().setLabelCount(FECHASHORAS.size + 2, true)
        chart!!.getAxisRight().isEnabled = false
        chart!!.invalidate()

    }

    fun randomFloatBetween(min: Float, max: Float): Float {
        val r = Random()
        return min + r.nextFloat() * (max - min)
    }

    private fun gerarateBarCharData() : BarData? {
        var d = BarData()

        return d
    }

    private fun generateLineData(): LineData? {
        val values: ArrayList<Entry> = ArrayList()
        //val valuelast: ArrayList<BarEntry> = ArrayList()
        var avalue : JSONObject //analogvalue_enj
        //var valoresx = ArrayList<String>()
        //var actividad = actividades()

        val d = LineData()
        //val entries: ArrayList<Entry> = ArrayList()
        //for (index in 0 until count) entries.add(Entry(index + 0.5f, getRandom(15, 5)))
        for (i in 0 until listvaloresnuevos!!.length()){
            avalue = listvaloresnuevos!![i] as JSONObject
            var tmp : Double = avalue.get("ValueOrig") as Double
            val x = i.toFloat()
            val y: Float =
                tmp.toFloat()  //randomFloatBetween(MIN_Y_VALUE, MAX_Y_VALUE)

            /*actividad = PrincipalActivity.dbHandler.getActividad(
                this.requireContext(),
                avalue.get("Loctimestamp") as String
            )

            //valoresx.add(avalue.get("Loctimestamp") as String)
            FECHASHORAS.add(actividad.fecha + " " + actividad.horainicio)*/

            if(avalue.get("IdActividad") != 0){
                // obtiene la actividad y coge la fecha y la hora
            } else {
                // verifica la fecha y hora de período según el loctimestamp
            }
            values.add(Entry(x, y))
        }
        val set = LineDataSet(values, "Tendencia")
        set.color = Color.rgb(240, 238, 70)
        set.lineWidth = 2.5f
        set.setCircleColor(Color.rgb(240, 238, 70))
        set.circleRadius = 5f
        set.fillColor = Color.rgb(240, 238, 70)
        set.mode = LineDataSet.Mode.CUBIC_BEZIER
        set.setDrawValues(true)
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(240, 238, 70)
        set.axisDependency = YAxis.AxisDependency.LEFT
        d.addDataSet(set)
        return d
    }
}