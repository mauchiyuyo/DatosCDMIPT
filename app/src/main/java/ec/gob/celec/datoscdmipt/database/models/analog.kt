package ec.gob.celec.datoscdmipt.database.models

class analog {

    // propiedades
    var MRID : Int = 0
    var Name : String = ""
    var PathName : String = ""
    var LocalName : String = ""
    var AliasName : String = ""
    var Description : String = ""
    var MaxValue : Double = 0.0
    var MinValue : Double = 0.0
    var IdSistema : Int = 0
    var IdEstado : Int = 0
    var IdTipo : Int = 0
    var Unidades : String = ""
    var OrdenMedicion : Int = 0
    var InsertUser : String = ""
    var InsertTimestamp : String = ""
    var UPDATE_USER : String = ""
    var UPDATE_TIMESTAMP : String = ""
    var Sincronizado : Int = 1
    var IdObservaciones : Int = 1
    var Observacion : String = "Sin Observación"
    var IdNFC : String = "650-03-36-06-09"
}