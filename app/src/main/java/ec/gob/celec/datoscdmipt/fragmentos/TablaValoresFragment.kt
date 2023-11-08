package ec.gob.celec.datoscdmipt.fragmentos

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.models.StringValueAdapterJSon
import ec.gob.celec.datoscdmipt.database.models.analogvalue_enj
import ec.gob.celec.datoscdmipt.database.models.stringvalue_enj
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private var ARG_LISTAVALORESNUEVOS = JSONArray()
private var ARG_TIPODATO : Int = 0

/**
 * A simple [Fragment] subclass.
 * Use the [TablaValoresFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TablaValoresFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var listvaloresnuevos: JSONArray? = null
    private var tipodato : Int ? = null
    //private var param2: String? = null
    private var MAX_X_VALUE : Float = 7f
    private var MAX_Y_VALUE = 50f
    private var MIN_Y_VALUE = 5f
    private var SET_LABEL = "Lecturas del Elemento"
    private var FECHASHORAS = ArrayList<String>() //arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

    var rv : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listvaloresnuevos = ARG_LISTAVALORESNUEVOS
            tipodato = ARG_TIPODATO
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val adapterstringvalues =
            listvaloresnuevos?.let { StringValueAdapterJSon(this.requireContext(), it) }

        val view: View = inflater.inflate(
            ec.gob.celec.datoscdmipt.R.layout.fragment_tabla_valores,
            container,
            false
        )

        rv = view.findViewById(R.id.lstAnalog)

        rv!!.layoutManager = LinearLayoutManager(this.requireContext(), RecyclerView.VERTICAL, false)
        rv!!.adapter = adapterstringvalues
        //return inflater.inflate(R.layout.fragment_tabla_valores, container, false)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.let {
            listvaloresnuevos = ARG_LISTAVALORESNUEVOS
            tipodato = ARG_TIPODATO
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
         * @return A new instance of fragment TablaValoresFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(listaAnalogValues: ArrayList<analogvalue_enj>?, listaStringValues: ArrayList<stringvalue_enj>?, tipo: Int) =
            TablaValoresFragment().apply {
                arguments = Bundle().apply {
                    ARG_TIPODATO = tipo
                    when(tipo){
                        1 -> {
                            for(i in 0 until listaAnalogValues!!.size){
                                var objJason = JSONObject()

                                objJason.put("MRID", listaAnalogValues[i].ANALOG_MRID)
                                objJason.put("Loctimestamp", listaAnalogValues[i].Loctimestamp)
                                objJason.put("ValueOrig", listaAnalogValues[i].ValueOrig)
                                objJason.put("InsertUser", listaAnalogValues[i].InsertUser)
                                objJason.put("IdActividad", listaAnalogValues[i].IdActividad)

                                ARG_LISTAVALORESNUEVOS.put(objJason)
                            }
                        }
                        2 -> {
                            Log.d("Lista String", listaStringValues!!.size.toString())
                            ARG_LISTAVALORESNUEVOS = JSONArray()
                            for(i in 0 until listaStringValues.size){
                                var objJason = JSONObject()
                                Log.d("Item", listaStringValues[i].STRING_MRID.toString())
                                objJason.put("MRID", listaStringValues[i].STRING_MRID)
                                objJason.put("Loctimestamp", listaStringValues[i].Loctimestamp)
                                objJason.put("ValueOrig", listaStringValues[i].ValueOrig)
                                objJason.put("InsertUser", listaStringValues[i].InsertUser)
                                objJason.put("IdActividad", listaStringValues[i].IdActividad)

                                ARG_LISTAVALORESNUEVOS.put(objJason)
                            }
                        }
                    }
                }
            }
    }
}