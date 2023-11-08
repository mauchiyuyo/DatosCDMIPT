@file:Suppress("DEPRECATION")

package ec.gob.celec.datoscdmipt.sincronizador

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import ec.gob.celec.datoscdmipt.R

class MyFirebaseInstanceServiceID : FirebaseInstanceIdService() {


    @Deprecated("Clases descontinuadas")
    @WorkerThread
    override fun onTokenRefresh() {
        super.onTokenRefresh()
        val tokenID : String? = FirebaseInstanceId.getInstance().token

        if (tokenID != null) {
            Log.d("Token_ID", tokenID)
        }
        Log.d("TOKENID", tokenID.toString())
        val sharedPreferences = applicationContext.getSharedPreferences(resources.getString(R.string.FCM_Pref), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(resources.getString(R.string.FCM_TOKEN), tokenID)
        editor.apply()
    }
}