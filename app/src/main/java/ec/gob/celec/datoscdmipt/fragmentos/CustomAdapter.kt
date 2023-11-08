package ec.gob.celec.datoscdmipt.fragmentos

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ec.gob.celec.datoscdmipt.R
import org.json.JSONObject

// CustomAdapter.kt
class CustomAdapter(context: Context, items: List<JSONObject>) : ArrayAdapter<JSONObject>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = LayoutInflater.from(context)
            itemView = inflater.inflate(R.layout.fragment_item_list, parent, false)
        }

        val currentItem = getItem(position)

        val txtIPTid = itemView?.findViewById<TextView>(R.id.txtIPTid)
        txtIPTid?.text = currentItem?.getString("MRID")

        val txtNombreIPT = itemView?.findViewById<TextView>(R.id.txtNombreIPT)
        txtNombreIPT?.text = currentItem?.getString("ALIASNAME")

        return itemView!!
    }
}