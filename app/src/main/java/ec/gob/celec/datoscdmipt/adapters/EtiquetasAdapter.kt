package ec.gob.celec.datoscdmipt.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ec.gob.celec.datoscdmipt.R

class EtiquetasAdapter(private val ListaEtiquetas: ArrayList<String>) : RecyclerView.Adapter<EtiquetasViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtiquetasViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EtiquetasViewHolder(layoutInflater.inflate(R.layout.etiquetasnfc, parent, false))
    }

    override fun onBindViewHolder(holder: EtiquetasViewHolder, position: Int) {
        //val item = ListaEtiquetas[position]
    }

    override fun getItemCount(): Int = ListaEtiquetas.size

}