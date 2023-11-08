package ec.gob.celec.datoscdmipt.database.models

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ec.gob.celec.datoscdmipt.R
import org.json.JSONArray
import org.json.JSONObject

class AnalogValueAdapter(mCtx : Context, val analogvalues: ArrayList<analogvalue_enj>) : RecyclerView.Adapter<AnalogValueAdapter.ViewHolder>() {

    val mCtx = mCtx

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val txtAnalogMRID = itemView.findViewById<TextView>(R.id.txtAnalogMRID)
        val txtHora = itemView.findViewById<TextView>(R.id.txtHora)
        val txtFecha = itemView.findViewById<TextView>(R.id.txtFecha)
        val txtValor = itemView.findViewById<TextView>(R.id.txtValor)
        //val txtAnalogInsertTimestamp = itemView.findViewById<TextView>(R.id.txtIdNFC)
        val cmdUpdate = itemView.findViewById<Button>(R.id.cmdActualizar)
        //val cmdDelete = itemView.findViewById<Button>(R.id.cmdEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalogValueAdapter.ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.lo_analogvalues,parent,false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: AnalogValueAdapter.ViewHolder, position: Int) {
        val analogvalue : analogvalue_enj = analogvalues[position]
        //val fechaLectura = LocalDate.parse(analogvalue.Loctimestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" ))
        holder.txtAnalogMRID.text = analogvalue.ANALOG_MRID.toString()
        holder.txtFecha.text = analogvalue.Loctimestamp.toString().substringBefore(" ")
        holder.txtHora.text = analogvalue.Loctimestamp.toString().substringAfter(" ")
        holder.txtValor.text = analogvalue.ValueOrig.toString()
        //holder.txtAnalogInsertUser.text = analogvalue.InsertUser.toString()
        //holder.txtAnalogInsertTimestamp.text = analogvalue.InsertTimestamp.toString()
    }

    override fun getItemCount(): Int {
        return analogvalues.size
    }
}

class StringValueAdapter(mCtx : Context, val stringvalues: ArrayList<stringvalue_enj>) : RecyclerView.Adapter<StringValueAdapter.ViewHolder>() {

    val mCtx = mCtx

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val txtAnalogMRID = itemView.findViewById<TextView>(R.id.txtAnalogMRID)
        val txtHora = itemView.findViewById<TextView>(R.id.txtHora)
        val txtFecha = itemView.findViewById<TextView>(R.id.txtFecha)
        val txtValor = itemView.findViewById<TextView>(R.id.txtValor)
        //val txtAnalogInsertTimestamp = itemView.findViewById<TextView>(R.id.txtIdNFC)
        val cmdUpdate = itemView.findViewById<Button>(R.id.cmdActualizar)
        //val cmdDelete = itemView.findViewById<Button>(R.id.cmdEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringValueAdapter.ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.lo_analogvalues,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: StringValueAdapter.ViewHolder, position: Int) {
        val stringvalue : stringvalue_enj = stringvalues[position]
        //val fechaLectura = LocalDate.parse(analogvalue.Loctimestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" ))
        holder.txtAnalogMRID.text = stringvalue.STRING_MRID.toString()
        holder.txtFecha.text = stringvalue.Loctimestamp.toString().substringBefore(" ")
        holder.txtHora.text = stringvalue.Loctimestamp.toString().substringAfter(" ")
        holder.txtValor.text = stringvalue.ValueOrig.toString()
        //holder.txtAnalogInsertUser.text = analogvalue.InsertUser.toString()
        //holder.txtAnalogInsertTimestamp.text = analogvalue.InsertTimestamp.toString()
    }

    override fun getItemCount(): Int {
        return stringvalues.size
    }
}

class StringValueAdapterJSon(mCtx : Context, val stringvalues: JSONArray) : RecyclerView.Adapter<StringValueAdapterJSon.ViewHolder>() {

    val mCtx = mCtx

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val txtAnalogMRID = itemView.findViewById<TextView>(R.id.txtAnalogMRID)
        val txtHora = itemView.findViewById<TextView>(R.id.txtHora)
        val txtFecha = itemView.findViewById<TextView>(R.id.txtFecha)
        val txtValor = itemView.findViewById<TextView>(R.id.txtValor)
        //val txtAnalogInsertTimestamp = itemView.findViewById<TextView>(R.id.txtIdNFC)
        val cmdUpdate = itemView.findViewById<Button>(R.id.cmdActualizar)
        //val cmdDelete = itemView.findViewById<Button>(R.id.cmdEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringValueAdapterJSon.ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.lo_analogvalues,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: StringValueAdapterJSon.ViewHolder, position: Int) {
        val stringvalue : JSONObject = stringvalues.getJSONObject(position)
        //val fechaLectura = LocalDate.parse(analogvalue.Loctimestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss" ))
        holder.txtAnalogMRID.setText(stringvalue.get("MRID").toString())
        holder.txtFecha.setText(stringvalue.get("Loctimestamp").toString().substringBefore(" "))
        holder.txtHora.setText(stringvalue.get("Loctimestamp").toString().substringAfter(" "))
        holder.txtValor.setText(stringvalue.get("ValueOrig").toString())
        //holder.txtAnalogInsertUser.text = analogvalue.InsertUser.toString()
        //holder.txtAnalogInsertTimestamp.text = analogvalue.InsertTimestamp.toString()
    }

    override fun getItemCount(): Int {
        return stringvalues.length()
    }
}

//class AnalogAdapter(mCtx : Context, val analoglist: ArrayList<analog>, private var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<AnalogAdapter.ViewHolder>() {
class AnalogAdapter(mCtx : Context, val analoglist: ArrayList<JSONObject>, private var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<AnalogAdapter.ViewHolder>() {
    val mCtx = mCtx
    private var positionArray:ArrayList<Boolean> = ArrayList(analoglist.size)

    init {
        for (i in 0 until analoglist.size) {

            if(!analoglist[i].getString("IdNFC").isNullOrBlank() || analoglist[i].getString("IdNFC").isNotEmpty()){
                positionArray.add(true)
                //holder.chkElegido.isEnabled = false
            } else {
                positionArray.add(false)
//                holder.chkElegido.isEnabled = true
            }
        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val txtAnalogMRID_NFC = itemView.findViewById<TextView>(R.id.txtAnalogMRID_NFC)
        val txtAnalogName = itemView.findViewById<TextView>(R.id.chkAliasNameEscogido)
        val txtSistema = itemView.findViewById<TextView>(R.id.txtSistema)
        val chkElegido = itemView.findViewById<CheckBox>(R.id.chkAliasNameEscogido)
        var elmentoJSON = JSONObject()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalogAdapter.ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.lo_elemento,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return analoglist.size
    }

    override fun onBindViewHolder(holder: AnalogAdapter.ViewHolder, position: Int) {
        //val analogvalue : analog = analoglist[position]
        val analogvalue : JSONObject = analoglist[position]
        holder.txtAnalogMRID_NFC.text = analogvalue.getString("AMRID")
        holder.txtAnalogName.text = analogvalue.getString("AliasName")
        //holder.chkElegido.isEnabled = !(!analogvalue.getString("IdNFC").isNullOrBlank() || analogvalue.getString("IdNFC").isNotEmpty())

        when(analogvalue.getInt("id_tipo")){
            1 ->{
                holder.txtSistema.text = "1-Anlg -> " + analogvalue.getString("OrdenMedicion")
            }
            4 -> {
                holder.txtSistema.text = "4-Opc/Str -> " + analogvalue.getString("OrdenMedicion")
            }
        }

        holder.chkElegido.setOnCheckedChangeListener(null)
        holder.chkElegido.isChecked = positionArray[position]
        holder.chkElegido.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            var agregar : Boolean

            if(isChecked){
                positionArray[position] = true
                holder.elmentoJSON.put("MRID", holder.txtAnalogMRID_NFC.text)
                holder.elmentoJSON.put("AliasName",holder.txtAnalogName.text)
                holder.elmentoJSON.put("IdNFC","Si - ${holder.txtAnalogMRID_NFC.text}")
                holder.elmentoJSON.put("id_tipo", analogvalue.getInt("id_tipo") )
                holder.elmentoJSON.put("OrdenMedicion", analogvalue.getInt("OrdenMedicion"))
                agregar = true
            } else {
                positionArray[position] = false
                holder.elmentoJSON.put("MRID", holder.txtAnalogMRID_NFC.text)
                agregar = false
            }
            onItemClickListener.onItemClick(holder.elmentoJSON.toString(), holder.elmentoJSON, agregar)
        })

        /*holder.chkElegido.setOnClickListener(){
            var agregar : Boolean

            if (holder.chkElegido.isChecked()){
                // elemento seleccionado, agregarlo al objeto JSON
                holder.elmentoJSON.put("MRID", holder.txtAnalogMRID_NFC.text)
                holder.elmentoJSON.put("AliasName",holder.txtAnalogName.text)
                holder.elmentoJSON.put("IdNFC","Si - ${holder.txtAnalogMRID_NFC.text}")
                holder.elmentoJSON.put("id_tipo", analogvalue.getInt("id_tipo") )
                holder.elmentoJSON.put("OrdenMedicion", analogvalue.getInt("OrdenMedicion"))

                agregar = true
            } else {
                // elemento a quitar del objeto JSON
                holder.elmentoJSON.put("MRID", holder.txtAnalogMRID_NFC.text)
                agregar = false
            }

            onItemClickListener.onItemClick(holder.elmentoJSON.toString(), holder.elmentoJSON, agregar)
            //return@setOnClickListener
        }*/

        //holder.txtSistema.text = "Tipo: " + analogvalue.getInt("id_tipo").toString() + " Orden: " + analogvalue.getString("OrdenMedicion")

/*        if (analogvalue.getString("IdNFC") != ""){
            //holder.cmdPrevioNFC.isEnabled = false
        }*/

        /*holder.cmdPrevioNFC.setOnClickListener{
            val inflater : LayoutInflater = LayoutInflater.from(mCtx)
            //val datos : View = inflater.inflate(R.layout.lo_analog, null)
            val elementos : View = inflater.inflate(R.layout.activity_editornfc, null)

            //var txtJSONElemento : TextView = elementos.findViewById<TextView>(R.id.txtJSONElemento)

            var unidad : Int = 0
            //var valorunidad : CharSequence = analogvalue.AMRID.toString().trim()

            if (analogvalue.AMRID.toString().substring(0,3).trim() == "650"){
                unidad = 0
            }
            if (analogvalue.AMRID.toString().substring(0,3).trim() == "651"){
                unidad = 1
            }
            if (analogvalue.AMRID.toString().substring(0,3).trim() == "652"){
                unidad = 2
            }
            if (analogvalue.AMRID.toString().substring(0,3).trim() == "653"){
                unidad = 3
            }

            var elJSON = JSONObject()

            elJSON.put("MRID", analogvalue.AMRID)
            elJSON.put("Name", analogvalue.Name)
            elJSON.put("Unidad", unidad)
            elJSON.put("NFC", analogvalue.IdNFC)

            var texto : String = "{\n" +
                    "    \"MRID\": ${analogvalue.getString("AMRID")},\n" +
                    "    \"Name\": \"${analogvalue.getString("AliasName")}\",\n" +
                    "    \"TipoDato\": \"${analogvalue.getString("TipoDato")}\"\n" +
                    "}"

            ////Log.d("#Texto obtenido: ", texto)
            ////Log.d("#Valor de MRID: ", analogvalue.AMRID.toString().substring(0,3).trim())

            //txtJSONElemento.text = texto
            //onItemClickListener.onItemClick(texto, elJSON, true)
            onItemClickListener.onItemClick(texto, analogvalue, true)

            return@setOnClickListener
        }*/
    }

    interface OnItemClickListener {
        fun onItemClick(texto : String, elJSON : JSONObject, agregar : Boolean)
    }


}

class StringAdapter(mCtx : Context, val stringlist: ArrayList<JSONObject>, private var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<StringAdapter.ViewHolder>() {
    val mCtx = mCtx

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val txtAnalogMRID_NFC = itemView.findViewById<TextView>(R.id.txtAnalogMRID_NFC)
        val txtAnalogName = itemView.findViewById<TextView>(R.id.chkAliasNameEscogido)
        val txtSistema = itemView.findViewById<TextView>(R.id.txtSistema)
        val chkElegido = itemView.findViewById<CheckBox>(R.id.chkAliasNameEscogido)
        var elmentoJSON = JSONObject()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringAdapter.ViewHolder {
        val v : View = LayoutInflater.from(parent.context).inflate(R.layout.lo_elemento,parent,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return stringlist.size
    }

    override fun onBindViewHolder(holder: StringAdapter.ViewHolder, position: Int) {
        //val analogvalue : analog = analoglist[position]
        val analogvalue : JSONObject = stringlist[position]
        holder.txtAnalogMRID_NFC.text = analogvalue.getString("AMRID")
        holder.txtAnalogName.text = analogvalue.getString("AliasName")
        holder.txtSistema.text = analogvalue.getString("PathName")

/*        if (analogvalue.getString("IdNFC") != ""){
            //holder.cmdPrevioNFC.isEnabled = false
        }*/

        holder.chkElegido.setOnClickListener(){
            var agregar : Boolean

            if (holder.chkElegido.isChecked()){
                // elemento seleccionado, agregarlo al objeto JSON
                holder.elmentoJSON.put("MRID", holder.txtAnalogMRID_NFC.text)
                holder.elmentoJSON.put("AliasName",holder.txtAnalogName.text)
                holder.elmentoJSON.put("IdNFC","Si - ${holder.txtAnalogMRID_NFC.text}")
                holder.elmentoJSON.put("id_tipo", analogvalue.getInt("id_tipo") )

                agregar = true
            } else {
                // elemento a quitar del objeto JSON
                holder.elmentoJSON.put("MRID", holder.txtAnalogMRID_NFC.text)
                agregar = false
            }

            onItemClickListener.onItemClick(holder.elmentoJSON.toString(), holder.elmentoJSON, agregar)
            //return@setOnClickListener
        }

        /*holder.cmdPrevioNFC.setOnClickListener{
            val inflater : LayoutInflater = LayoutInflater.from(mCtx)
            //val datos : View = inflater.inflate(R.layout.lo_analog, null)
            val elementos : View = inflater.inflate(R.layout.activity_editornfc, null)

            //var txtJSONElemento : TextView = elementos.findViewById<TextView>(R.id.txtJSONElemento)

            var unidad : Int = 0
            //var valorunidad : CharSequence = analogvalue.AMRID.toString().trim()

            if (analogvalue.AMRID.toString().substring(0,3).trim() == "650"){
                unidad = 0
            }
            if (analogvalue.AMRID.toString().substring(0,3).trim() == "651"){
                unidad = 1
            }
            if (analogvalue.AMRID.toString().substring(0,3).trim() == "652"){
                unidad = 2
            }
            if (analogvalue.AMRID.toString().substring(0,3).trim() == "653"){
                unidad = 3
            }

            var elJSON = JSONObject()

            elJSON.put("MRID", analogvalue.AMRID)
            elJSON.put("Name", analogvalue.Name)
            elJSON.put("Unidad", unidad)
            elJSON.put("NFC", analogvalue.IdNFC)

            var texto : String = "{\n" +
                    "    \"MRID\": ${analogvalue.getString("AMRID")},\n" +
                    "    \"Name\": \"${analogvalue.getString("AliasName")}\",\n" +
                    "    \"TipoDato\": \"${analogvalue.getString("TipoDato")}\"\n" +
                    "}"

            ////Log.d("#Texto obtenido: ", texto)
            ////Log.d("#Valor de MRID: ", analogvalue.AMRID.toString().substring(0,3).trim())

            //txtJSONElemento.text = texto
            //onItemClickListener.onItemClick(texto, elJSON, true)
            onItemClickListener.onItemClick(texto, analogvalue, true)

            return@setOnClickListener
        }*/
    }

    interface OnItemClickListener {
        fun onItemClick(texto : String, elJSON : JSONObject, agregar : Boolean)
    }


}