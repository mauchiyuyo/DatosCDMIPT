package ec.gob.celec.datoscdmipt.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
import android.nfc.tech.Ndef
import android.widget.Toast
import java.io.IOException

@Suppress("DEPRECATION")
class NfcManagerOwn(private val activity: Activity) {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private var nfcPendingIntent: PendingIntent? = null
    private var nfcIntentFilters: Array<IntentFilter>? = null

    fun enableNfc() {
        nfcPendingIntent = PendingIntent.getActivity(
            activity,
            0,
            Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndef.addDataType("*/*")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Error al agregar tipo de MIME.", e)
        }
        this.nfcIntentFilters = arrayOf(ndef)
        nfcAdapter?.enableForegroundDispatch(activity, this.nfcPendingIntent,
            this.nfcIntentFilters, null)
    }

    fun disableNfc() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun readFromIntent(intent: Intent): String? {
        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val ndefMessages: List<NdefMessage> = parcelables?.map { it as NdefMessage } as List<NdefMessage>
            val ndefRecord: NdefRecord = ndefMessages[0].records[0]
            val payload: ByteArray = ndefRecord.payload
            return String(payload)
        }
        return null
    }

    fun writeToTag(message: String, tag: Tag) {
        val ndefMessage = NdefMessage(
            arrayOf(
                NdefRecord.createTextRecord("en", message)
            )
        )
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                Toast.makeText(activity, "Mensaje escrito en la etiqueta NFC.", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IOException) {
                Toast.makeText(
                    activity,
                    "Error al escribir en la etiqueta NFC.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: FormatException) {
                Toast.makeText(
                    activity,
                    "Error al escribir en la etiqueta NFC.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}