package ec.gob.celec.datoscdmipt.login

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.gson.JsonObject
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.models.extensions.*
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.CurrentAccountCallback
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.SignOutCallback
import com.microsoft.identity.client.exception.*
import ec.gob.celec.datoscdmipt.PrincipalActivity
import ec.gob.celec.datoscdmipt.R
import ec.gob.celec.datoscdmipt.database.helper.DatabaseOpenHelper
import ec.gob.celec.datoscdmipt.database.models.usuario
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class LoginADActivity : AppCompatActivity() {
    companion object{
        var laCuenta : IAccount? = null
        var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
        var currentUserTextView: TextView? = null
        /* UI & Debugging Variables */
        var signInButton: Button? = null
        var signOutButton: Button? = null
        var callGraphApiInteractiveButton: Button? = null
        var callGraphApiSilentButton: Button? = null
        var logTextView: TextView? = null

        fun performOperationOnSignOut() {
            //val signOutText = "Signed Out."
            currentUserTextView!!.text = ""
            //Toast.makeText(this, signOutText, Toast.LENGTH_SHORT).show()
        }

        fun updateUI(account: IAccount?) {
            if (account != null) {
                signInButton!!.isEnabled = false
                signOutButton!!.isEnabled = true
                callGraphApiInteractiveButton!!.isEnabled = true
                callGraphApiSilentButton!!.isEnabled = true
                currentUserTextView!!.text = account.username
            } else {
                signInButton!!.isEnabled = true
                signOutButton!!.isEnabled = false
                callGraphApiInteractiveButton!!.isEnabled = false
                callGraphApiSilentButton!!.isEnabled = false
                currentUserTextView!!.text = ""
                logTextView!!.text = ""
            }
        }

    }
    private val SCOPES = arrayOf("Files.Read")

    /* Azure AD v2 Configs */
    private val AUTHORITY = "https://login.microsoftonline.com/common"

    //private val TAG = MainActivity::class.java.simpleName

    private var dbHandlerLogin = DatabaseOpenHelper(this, "datoscentral.db", null, 1)

    lateinit var preferencias : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setTheme(R.style.DarkTheme)
        setContentView(R.layout.activity_login_a_d)

        Log.d("********* Login - linea 92", "pasa")

        preferencias =  PreferenceManager.getDefaultSharedPreferences(applicationContext)

        Log.d("********* Login - linea 98", "pasa")
        initializeUI()

        PublicClientApplication.createSingleAccountPublicClientApplication(
            applicationContext,
            R.raw.auth_config_single_account, object : ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    Log.d("********* Login - linea 105", "pasa")
                    mSingleAccountApp = application
                    loadAccount()
                }

                override fun onError(exception: MsalException) {
                    Log.d("********* Login - linea 111", "error")
                    Log.d("********* Exception: ", exception.toString())
                    displayError(exception)
                }
            }
        )

    }

    //When app comes to the foreground, load existing account to determine if user is signed in
    private fun loadAccount() {
        if (mSingleAccountApp == null) {
            return
        }
        mSingleAccountApp!!.getCurrentAccountAsync(object : CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                // You can use the account data to update your UI or your app database.
                updateUI(activeAccount)
                /*
                * llama a principal pq ya está ingresado
                * */
                if (laCuenta != null){
                    val elUsuario = laCuenta!!.username //: CharSequence = txtUsuario.getText()
                    //var elPassword : CharSequence = txtPassword.getText()

                    cargaDatosUsuario(elUsuario, activeAccount)

                } //else {
                    //updateUI(null)
                //}
                /*
                *
                *
                * */
            }

            override fun onAccountChanged(
                priorAccount: IAccount?,
                currentAccount: IAccount?
            ) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    performOperationOnSignOut()
                }
            }

            override fun onError(exception: MsalException) {
                displayError(exception)
            }
        })
    }

    fun cargaDatosUsuario(elUsuario: String, activeAccount : IAccount?){
        /*
        var usuario : usuario = dbHandlerLogin.getUsuario(applicationContext, elUsuario.substringBefore("@"))
        var laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var fecha = Date()

        if (usuario.usuario != "") {
            // el usuario existe en la base de datos
            // buscar en active directory y enviar password
            //Log.w("Login", "Usuario encontrado")
            val actividadPrincipal = Intent(applicationContext, PrincipalActivity::class.java)
            actividadPrincipal.putExtra("Usuario", usuario.usuario)
            actividadPrincipal.putExtra("Perfil", usuario.idPerfil)
            actividadPrincipal.putExtra("Central", usuario.idCentral)
            actividadPrincipal.putExtra("TimeStamp", laFechaT.format(fecha))
            //actividadPrincipal.putExtra("laCuenta", lacuenta)
            //Log.w("Login", "A principal")
            startActivity(actividadPrincipal)
            //super.finish()
            updateUI(activeAccount)
        }
        else{
            Toast.makeText(applicationContext, "Usuario No Registrado, \nsolicítelo al jefe de operación", Toast.LENGTH_SHORT).show()
            mSingleAccountApp!!.signOut()
            updateUI(null)
            return
        }
        */
        val database : File = applicationContext.getDatabasePath(dbHandlerLogin.nameDatabase)
        if (!database.exists()){
            dbHandlerLogin.readableDatabase
            if(dbHandlerLogin.copyDatabase(applicationContext)){
                Toast.makeText(applicationContext, "BD Copiada", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(
                    applicationContext,
                    "Error en la Copia de BD",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        try {

            val usuario : usuario = dbHandlerLogin.getUsuario( elUsuario.substringBefore("@"))
            val laFechaT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fecha = Date()

            if (usuario.usuario != "") {
                // el usuario existe en la base de datos
                // buscar en active directory y enviar password
                //Log.w("Login", "Usuario encontrado")
                val prefEditor = preferencias.edit()
                val actividadPrincipal = Intent(applicationContext, PrincipalActivity::class.java)
                actividadPrincipal.putExtra("Usuario", usuario.usuario)
                actividadPrincipal.putExtra("Perfil", usuario.idPerfil)
                actividadPrincipal.putExtra("Central", usuario.idCentral)
                actividadPrincipal.putExtra("TimeStamp", laFechaT.format(fecha))
                prefEditor.putString("elUsuario", laCuenta!!.username)
                prefEditor.putInt("Perfil", usuario.idPerfil)
                prefEditor.putInt("Central", usuario.idCentral)
                prefEditor.apply()
                //actividadPrincipal.putExtra("laCuenta", lacuenta)
                //Log.w("Login", "A principal")
                updateUI(activeAccount)
                startActivity(actividadPrincipal)
                //super.finish()

            }
            else{
                Toast.makeText(applicationContext, "Usuario No Registrado, \nsolicítelo al jefe de operación", Toast.LENGTH_SHORT).show()
                mSingleAccountApp!!.signOut()
                updateUI(null)
                return
            }
        }
        catch (e: Exception){
            e.printStackTrace()
            //Log.w("LoginActivity", "Error en carga de datos")
        }
    }

    private fun initializeUI() {
        signInButton = findViewById(R.id.signIn)
        callGraphApiSilentButton = findViewById(R.id.callGraphSilent)
        callGraphApiInteractiveButton = findViewById(R.id.callGraphInteractive)
        signOutButton = findViewById(R.id.clearCache)
        logTextView = findViewById(R.id.txt_log)
        currentUserTextView = findViewById(R.id.current_user)

        //Sign in user
        signInButton!!.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            mSingleAccountApp!!.signIn(
                this,
                null,
                SCOPES,
                getAuthInteractiveCallback()
            )
        })

        //Sign out user
        signOutButton!!.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            mSingleAccountApp!!.signOut(object : SignOutCallback {
                override fun onSignOut() {
                    updateUI(null)
                    performOperationOnSignOut()
                }

                override fun onError(exception: MsalException) {
                    displayError(exception)
                }
            })
        })

        //Interactive
        callGraphApiInteractiveButton!!.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            mSingleAccountApp!!.acquireToken(
                this,
                SCOPES,
                getAuthInteractiveCallback()
            )
        })

        //Silent
        callGraphApiSilentButton!!.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            mSingleAccountApp!!.acquireTokenSilentAsync(SCOPES, AUTHORITY,
                getAuthSilentCallback()
            )
        })
    }

    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                ////Log.d(TAG, "Successfully authenticated")
                /* Update UI */updateUI(authenticationResult.account)
                /* call graph */callGraphAPI(authenticationResult)

                /*
                *
                *
                *
                * */


                laCuenta = authenticationResult.account

                //prefEditor.putStringSet("laCuenta", laCuenta.toString())
                val elUsuario = laCuenta!!.username //: CharSequence = txtUsuario.getText()
                //var elPassword : CharSequence = txtPassword.getText()
                cargaDatosUsuario(elUsuario, laCuenta)
                /*
                *
                *
                *
                * */

            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                ////Log.d(TAG, "Authentication failed: $exception")
                displayError(exception)
            }

            override fun onCancel() {
                /* User canceled the authentication */
                ////Log.d(TAG, "User cancelled login.")
            }
        }
    }

    private fun getAuthSilentCallback(): SilentAuthenticationCallback {
        return object : SilentAuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                ////Log.d(TAG, "Successfully authenticated")
                callGraphAPI(authenticationResult)
            }

            override fun onError(exception: MsalException) {
                ////Log.d(TAG, "Authentication failed: $exception")
                displayError(exception)
            }
        }
    }

    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        val accessToken = authenticationResult.accessToken
        val graphClient = GraphServiceClient
            .builder()
            .authenticationProvider { request ->
                ////Log.d(TAG, "Authenticating request," + request.requestUrl)
                request.addHeader("Authorization", "Bearer $accessToken")
            }
            .buildClient()
        graphClient
            .me()
            .drive()
            .buildRequest()[object : ICallback<Drive> {
            override fun success(drive: Drive) {
                ////Log.d(TAG, "Found Drive " + drive.id)
                displayGraphResult(drive.rawObject)
            }

            override fun failure(ex: ClientException?) {
                if (ex != null) {
                    displayError(ex)
                }
            }
        }]
    }

    private fun displayError(exception: Exception) {
        logTextView!!.text = exception.toString()
    }

    private fun displayGraphResult(graphResponse: JsonObject) {
        logTextView!!.text = graphResponse.toString()
    }





}