package ec.gob.celec.datoscdmipt.database.models

import java.sql.Timestamp

class analogvalue_enj {

    // propiedades
    var ANALOG_MRID : Int = 0
    var Loctimestamp : String = ""
    var ValueOrig : Double = 0.0
    var ValueEdit : Double = 0.0
    var InsertUser : String = ""
    var InsertTimestamp : String = ""
    var UpdateUser : String = ""
    var UpdateTimestamp : String = ""
    var IdActividad : Int = 0
    var Origen : String = ""
    var Sincronizado : Int = 0
    var Observacion : String = ""
    var IdObservaciones : Int = 1
}