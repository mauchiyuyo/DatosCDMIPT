@file:Suppress("DEPRECATION")

package ec.gob.celec.datoscdmipt.nfc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.adapters.EtiquetasAdapter
import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
class EditorEtiquetasNFCActivity : AppCompatActivity() {

    private lateinit var nfcManager: NfcManagerOwn
    //private lateinit var binding : ActivityEditorEtiquetasNfcBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_editor_etiquetas_nfc)
        nfcManager = NfcManagerOwn(this)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rcvEtiquetasNFC)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EtiquetasAdapter(getEtiquetas())
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableNfc()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableNfc()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val message = nfcManager.readFromIntent(intent)
        if (message != null) {
            // Mostrar el mensaje en un TextView o hacer algo con el mensaje
            //textView.text = message
            // revisar formato de contenido
            // si tiene los datos antiguos entonces
            if(message[0].toString() == "{"){
                // si empieza con llave tiene los json
                val jsonString = "[$message]"
                @Serializable
                data class objJSON (val ID : Int, val tp : Int)
                //val objetosJSON = Json.decodeFromString<objJSON>(jsonString)
                val listOfStrings = Gson().fromJson(jsonString, mutableListOf<String>().javaClass)

                // marca en la lista los elementos encontrados
                listOfStrings.forEach { jsonObject ->
                    // hacer algo con jsonObject
                    val asObject = Gson().fromJson(jsonObject, objJSON::class.java)
                    val elID = asObject.ID
                    val elTP = asObject.tp

                    // TODO: cargar la lista de elementos en base al sistema al que pertenecen lo códigos encontrados en la etiqueta
                    // TODO: cargar todos los elementos correspondientes al sistema escogido en la lista desplegable


                }
            } else {
                // no empieza con llave entonces verifica si es codigo valido

            }

            // TODO: activa el botón para grabar el nuevo código en la etiqueta y
            // en los elementos el nuevo id de la etiqueta
            // si está vacía presenta un mensaje
            // si tiene formato nuevo entonces
            // marca los elementos que tienen el código
            // los busca en la tabla elementosxetiqueta
            // si hubo cambios manda a sincronizar las bases de datos
        }
    }

    // en el caso de querer escribir una etiqueta NFC
    /*fun writeTag(view: View){
        val message = "Hello NFC"
        // en message se debe poner el nuevo id de la etiqueta generado
        // el código se conforma de
        // id de la central, viene del usuario o de la variable global
        // el id del sistema al que se va a etiquetar, para IPT se crea el sistema IPT
        // un número secuencial calculado según las etiquetas previas
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            nfcManager.writeToTag(message, tag)
        } else {
            Toast.makeText(this, "No se ha encontrado una etiqueta NFC", Toast.LENGTH_SHORT).show()
        }
    }*/

    // cargar las etiquetas en listado
    private fun getEtiquetas(): ArrayList<String> {

        // obtener desde base de datos

        return ArrayList()

    }

    // cargar elementos por etiqueta

    // agregar elemento a etiqueta

    // escribir datos en etiqueta NFC

    // editar elementos de etiqueta NFC

    //
}