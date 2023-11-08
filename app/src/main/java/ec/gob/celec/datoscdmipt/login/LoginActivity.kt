package ec.gob.celec.datoscdmipt.login

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import ec.gob.celec.datoscdmipt.PrincipalActivity
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.usuario
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        lateinit var dbHandlerLogin : DatabaseOpenHelper
    }

    //private val activity = this@LoginActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_login)

        Log.d("********* Login - linea 38", "pasa")

        dbHandlerLogin = DatabaseOpenHelper(this, null, null, 1)

        val cmdIngreso = findViewById<Button>(R.id.cmdIngreso)
        val txtUsuario = findViewById<TextView>(R.id.txtUsuario)
        //val txtPassword = findViewById<TextView>(R.id.txtPassword)

        this.title = "DatosCdM - Acceso al sistema"

        Log.d("********* Login - linea 48", "pasa")
        cmdIngreso.setOnClickListener {
            // revisa q exista la BD
            if (!dbHandlerLogin.verificaDB(this)) return@setOnClickListener
            try {
                val elUsuario : CharSequence = txtUsuario.getText()
                //var elPassword : CharSequence = txtPassword.getText()

                val usuario : usuario = dbHandlerLogin.getUsuario(elUsuario.toString())
                val laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val fecha = Date()

                if (usuario.usuario != "") {
                    // el usuario existe en la base de datos
                    // buscar en active directory y enviar password
                    Log.w("Login", "Usuario encontrado")
                    //if(dbHandlerLogin.loginAD(this, usuario.usuario, elPassword.toString() )){
                        //val actividadPrincipal = Intent(this, MainActivity::class.java)
                        val actividadPrincipal = Intent(this, PrincipalActivity::class.java)
                        actividadPrincipal.putExtra("Usuario", usuario.usuario)
                        actividadPrincipal.putExtra("Perfil", usuario.idPerfil)
                        actividadPrincipal.putExtra("Central", usuario.idCentral)
                        actividadPrincipal.putExtra("TimeStamp", laFechaT.format(fecha))
                        Log.w("Login", "A principal")
                        startActivity(actividadPrincipal)
                        super.finish()
                    //}
                    //else{
                    //    Toast.makeText(applicationContext, "Problema en AD", Toast.LENGTH_SHORT).show()
                    //}
                }
                else{
                    Toast.makeText(applicationContext, "Usuario No Registrado, \nsolicítelo al jefe de operación", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            catch (e: Exception){
                e.printStackTrace()
                Log.w("LoginActivity", "Error en carga de datos")
            }
        }
        // Enables Always-on
    }
/*
    private fun copyDatabase(context: Context): Boolean {
        return try {
            val outFileName: String = dbHandlerLogin.DBPath + dbHandlerLogin.DATABASE_NAME

            // copiando BD
            val `is` = context.assets.open(dbHandlerLogin.DATABASE_NAME)
            val os = FileOutputStream(outFileName)

            val buffer = ByteArray(1024)
            while (`is`.read(buffer) > 0) {
                os.write(buffer)
                Log.d("#DB", "writing>>")
            }

            os.flush()
            os.close()
            `is`.close()

            Log.d("MainActivity", "DB copied")
            true
        } catch (e: java.lang.Exception) {
            Log.d("MainActivity", "DB not copied")
            e.printStackTrace()
            false
        }
    }

*/
    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}