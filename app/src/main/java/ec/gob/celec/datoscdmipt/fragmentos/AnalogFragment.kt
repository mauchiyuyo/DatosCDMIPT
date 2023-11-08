package ec.gob.celec.datoscdmipt.fragmentos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.models.analog

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private var ARG_ELANALOG = analog()
private var ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnalogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalogFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var elanalog: analog? = null
    private var param2: String? = null

    var txtValMinimo : EditText? =null
    var txtValMaximo : EditText? =null
    var txtValNuevo : EditText? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            elanalog = ARG_ELANALOG
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(
            ec.gob.celec.datoscdmipt.R.layout.fragment_analog,
            container,
            false
        )

        txtValMinimo = view.findViewById<EditText>(R.id.txtValMinimo)
        txtValMaximo = view.findViewById<EditText>(R.id.txtValMaximo)
        txtValNuevo = view.findViewById<EditText>(R.id.txtValNuevo)


        return view //inflater.inflate(R.layout.fragment_analog, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param elanalog Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnalogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(elanalog: analog, param2: String) =
            AnalogFragment().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                    ARG_ELANALOG = elanalog
                    putString(ARG_PARAM2, param2)

                }

            }
    }
}