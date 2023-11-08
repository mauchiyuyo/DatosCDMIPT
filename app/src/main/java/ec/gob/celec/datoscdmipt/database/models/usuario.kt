package ec.gob.celec.datoscdmipt.database.models

class usuario {
    var id : Int = 0
    var usuario : String = ""
    var password : String = ""
    var idPerfil : Int = 0
    var idCentral : Int = 0
    var correo : String = ""
    var sincronizado : Int = 1
    var activado : Int = 1
}