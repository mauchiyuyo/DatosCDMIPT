package ec.gob.celec.datoscdmipt.fragmentos

import android.annotation.SuppressLint
import android.content.Context
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.listeners.Listener
import ec.gob.celec.datoscdmipt.nfc.EditornfcActivity
import ec.gob.celec.datoscdmipt.nfc.MIME_TEXT_PLAIN
import org.json.JSONArray
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentWrite.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentWrite : DialogFragment() {
    companion object{
        val TAG: String = FragmentWrite::class.java.simpleName

        fun newInstance(): FragmentWrite {
            return FragmentWrite()
        }
    }

    private var mTvMessage: TextView? = null
    private var mProgress: ProgressBar? = null
    private var mListener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_write, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        mTvMessage = view.findViewById<View>(R.id.tv_message) as TextView
        mProgress = view.findViewById<View>(R.id.progress) as ProgressBar
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

    fun onNfcDetected(ndef: Ndef?, messageToWrite: String, elementosJSON: JSONArray, tag: Tag) {
        mProgress!!.visibility = View.VISIBLE
        writeToNfc(ndef, messageToWrite, elementosJSON, tag)
    }

    @SuppressLint("SetTextI18n")
    fun writeToNfc(ndef: Ndef?, message: String, elementosJSON: JSONArray, tag : Tag) {
        mTvMessage!!.text = "Escribiendo en la etiqueta..."//getString(R.string.message_write_progress)
        //var ndeflocal = ndef //if (ndef == null) ndef else NdefFormatable.get(tag)

        try{
            //val empty = byteArrayOf()
            /*if(ndef == null) ndef?.writeNdefMessage(
                NdefMessage(
                    NdefRecord(
                        NdefRecord.TNF_UNKNOWN,
                        empty,
                        empty,
                        empty
                    )
                )
            )*/

            if (ndef != null) {
                try {
                    val tamanio = elementosJSON.toString().toByteArray(Charset.forName("US-ASCII")).size
                    ndef.connect()
                    //Log.d("Conectar NFC", "Conectado...")
                    if (tamanio <= ndef.maxSize) {
                        /*var elmensaje = with(message) {
                            replace("[","")
                            replace("]","")
                        }*/
                        //elmensaje = elmensaje
                        //Log.d("JASON en Write NFC", elmensaje)
                        //Log.d("JASON en Write NFC", elementosJSON.toString() + " : " + tamanio.toString() + " > " + ndef.maxSize.toString())
                        val records = ndef.ndefMessage?.records
                        for (item in 0 until records!!.size){
                            val ndfrecord = records[item]
                            if(ndfrecord.tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndfrecord.type, NdefRecord.RTD_TEXT)){
                                try {
                                    readText(ndfrecord).let { Log.d("NFC datos varios: ", it) }
                                } catch (e: UnsupportedEncodingException) {
                                    Log.e(TAG, "Unsupported Encoding", e)
                                }
                            }
                        }
                        val mimeRecord = NdefRecord.createMime(
                            MIME_TEXT_PLAIN,
                            elementosJSON.toString().replace("[","").replace("]","").toByteArray() // elmensaje.toByteArray(Charset.forName("US-ASCII"))
                        )
                        ndef.writeNdefMessage(NdefMessage(mimeRecord))
                        //Write Successful
                        mTvMessage!!.text =
                            "Etiqueta escrita exitosamente $message" //getString(R.string.message_write_success)
                        // encerar listado de elementos
                        ////////////////////////////////////////////
                        Log.d("elementosJSON.length()", elementosJSON.length().toString())
                        for (item in 0 until elementosJSON.length()){
                            // editar cada json del array
                            val elmrid = elementosJSON.getJSONObject(item).get("ID").toString()
                            val idtipo = elementosJSON.getJSONObject(item).getInt("tp")
//                        if (item < elementosJSON.length()) elementosJSON.remove(item)
                            //var actualizado = EditornfcActivity.dbHandler.updateIdNFC(message, elmrid.toInt(), idtipo)
                            val actualizado = EditornfcActivity.dbHandler.updateIdNFC(ndef.ndefMessage.records[0].id.toString(), elmrid.toInt(), idtipo)
                            //                      return elementosJSON
                            Log.d("Actualizado", actualizado.toString())
                        }
                    } else {
                        Log.d("Error tamaño -> ", tamanio.toString() + " > " + ndef.maxSize.toString())
                    }
                    ndef.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    mTvMessage!!.text = "Error en el valor de la etiqueta"//getString(R.string.message_write_error)
                } catch (e: FormatException) {
                    e.printStackTrace()
                    mTvMessage!!.text = "Error de escritura"//getString(R.string.message_write_error)
                } finally {
                    mProgress!!.visibility = View.GONE
                }
            } else {
                try{
                    val formatable = NdefFormatable.get(tag)

                    val lang = "us"
                    val textBytes: ByteArray = elementosJSON.toString().replace("[","").replace("]","").toByteArray()
                    val langBytes = lang.toByteArray(charset("US-ASCII"))
                    val langLength = langBytes.size
                    val textLength = textBytes.size
                    val payLoad = ByteArray(1 + langLength + textLength)

                    payLoad[0] = langLength.toByte()

                    System.arraycopy(langBytes, 0, payLoad, 1, langLength)
                    System.arraycopy(textBytes, 0, payLoad, 1 + langLength, textLength)

                    val recordNFC = NdefRecord(
                        NdefRecord.TNF_WELL_KNOWN,
                        NdefRecord.RTD_TEXT,
                        ByteArray(0),
                        payLoad
                    )

                    val records = arrayOf(recordNFC)

                    if (formatable != null) {
                        try {
                            formatable.connect()
                            try {
                                formatable.format(NdefMessage(records))
                                for (item in 0 until elementosJSON.length()){
                                    // editar cada json del array
                                    var elmrid : String
                                    var idtipo : Int
                                    elmrid = elementosJSON.getJSONObject(item).get("ID").toString()
                                    idtipo = elementosJSON.getJSONObject(item).getInt("tp")
//                        if (item < elementosJSON.length()) elementosJSON.remove(item)
                                    //var actualizado = EditornfcActivity.dbHandler.updateIdNFC(message, elmrid.toInt(), idtipo)
                                    val actualizado = EditornfcActivity.dbHandler.updateIdNFC(records[0].id.toString(), elmrid.toInt(), idtipo)
                                    //                      return elementosJSON
                                    Log.d("Actualizado", actualizado.toString())
                                }
                            } catch (e: Exception) {
                                // let the user know the tag refused to format
                                Log.d("Format NFC --", e.printStackTrace().toString())
                            }
                        } catch (e: Exception) {
                            // let the user know the tag refused to connect
                            Log.d("Format NFC no connect--", e.printStackTrace().toString())
                        } finally {
                            formatable.close()
                            mProgress!!.visibility = View.GONE
                        }
                    } else {
                        // let the user know the tag cannot be formatted
                        Log.d("Format NFC problem--", "e.printStackTrace().toString()")
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    mTvMessage!!.text = "Error en el valor de la etiqueta"//getString(R.string.message_write_error)
                } catch (e: FormatException) {
                    e.printStackTrace()
                    mTvMessage!!.text = "Error de escritura"//getString(R.string.message_write_error)
                } finally {
                    mProgress!!.visibility = View.GONE
                }

                //Log.w("NFC Null", "NFC NUll")
            }
        } catch (e: FormatException) {
            e.printStackTrace()
            mTvMessage!!.text = "Error de etiqueta"
        } finally {

        }

    }

    @Throws(UnsupportedEncodingException::class)
    private fun readText(record: NdefRecord): String {
        val zero : Byte = 0
        val payload = record.payload
        val textEncoding =
            if ((payload[0] and 128.toByte()) == zero) "UTF-8" else "UTF-16" // Get the Text Encoding
        val languageCodeLength = payload[0] and 51 // Get the Language Code
        return (payload.toString() + (languageCodeLength + 1).toString() + (payload.size - languageCodeLength - 1).toString() + textEncoding) // Get the Text
    }
}