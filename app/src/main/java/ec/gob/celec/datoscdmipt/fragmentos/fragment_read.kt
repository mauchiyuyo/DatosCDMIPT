package ec.gob.celec.datoscdmipt.fragmentos

import android.content.Context
import android.nfc.FormatException
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ec.gob.celec.datoscdmipt.nfc.EditornfcActivity
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.listeners.Listener
import java.io.IOException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [fragment_read.newInstance] factory method to
 * create an instance of this fragment.
 */
class fragment_read : DialogFragment() {
    companion object{
        val TAG: String = fragment_read::class.java.getSimpleName()

        fun newInstance(): fragment_read? {
            return fragment_read()
        }
    }

    private var mTvMessage: TextView? = null
    private var mListener: Listener? = null

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_read, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        mTvMessage = view.findViewById<View>(R.id.tv_message) as TextView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as EditornfcActivity
        mListener!!.onDialogDisplayed()
    }

    override fun onDetach() {
        super.onDetach()
        mListener!!.onDialogDismissed()
    }

    fun onNfcDetected(ndef: Ndef) {
        readFromNFC(ndef)
    }

    private fun readFromNFC(ndef: Ndef) {
        try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            val message = String(ndefMessage.records[0].payload)
            Log.d(TAG, "readFromNFC: $message")
            mTvMessage!!.text = message
            ndef.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
    }
}