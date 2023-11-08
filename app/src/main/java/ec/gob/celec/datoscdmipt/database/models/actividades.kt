package ec.gob.celec.datoscdmipt.database.models

import java.sql.Date
import java.sql.Time

class actividades {
    var id : Int? = null
    var fecha : String? = null
    var descripcion : String = "Lectura de datos y mediciones"
    var horainicio : String? = null
    var horafin : String? = null
    var id_sistema : Int = 8
    var idplanificacion_ipt : Int = 0
    var operador : String = ""
    var sincronizado : Int = 1
    var observacion : String = ""
    var idobservaciones : Int = 1
    var idusuario : Int = 0
    var diainicio : String? = null
    var diafinal : String? = null

}