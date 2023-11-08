package ec.gob.celec.datoscdmipt.adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.models.etiquetas

class EtiquetasViewHolder(view: View) : RecyclerView.ViewHolder(view) {


    val etiquetanfc = view.findViewById<TextView>(R.id.tvEtiquetaNFC)

    fun render(etiqueta : etiquetas){
        etiquetanfc.text = etiqueta.identificador

    }
}