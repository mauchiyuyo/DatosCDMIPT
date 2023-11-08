package ec.gob.celec.datoscdmipt.sincronizador


class Sincronizador {

    // valor de sincronización
    // 0 - está sincronizado
    // 1 - insertado en local
    // 2 - insertado en servidor
    // 3 - actualizado en local
    // 4 - actualizado en servidor
    // selecciona todos los registros que tengan "sincronizado" diferente de 0
    // convierte el resultado en un array de json
    // si es 1 envía el comando de inserción - arrayjson y tabla a insertar
    // si es 3 envía el comando de actualización - arrayjson y tabla
    // recibe respuesta desde el servidor
    // receptor de datos
    // recibe un arrayjson, si es 2 o 4 y tabla a insertar o actualizar
    // ejecuta el insert o update según la tabla recibida
    // envía respuesta al servidor

    companion object{
        private const val TAG = "Sincronizador"
    }
/*
    fun SincronizaBD(datos: JSONArray, tabla: String, tipo: Int) : Boolean{
        //var conexion : Boolean
        var sincronizado : Boolean

        when (tipo){
            1 -> { // inserta lo local en servidor
                sincronizado = true
            }
            3 -> { // actualiza lo local en servidor
                sincronizado = true
            }
            else -> {
                sincronizado = false
            }
        }
        return  sincronizado
    }
*/
    fun VerificaConexion () : Boolean {
        var conectado : Boolean

        // proceso para verificar conexión a servidor
        conectado = true

        return conectado

    }

}