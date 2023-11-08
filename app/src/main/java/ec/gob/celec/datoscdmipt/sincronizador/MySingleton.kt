package ec.gob.celec.datoscdmipt.sincronizador

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class MySingleton(context: Context) {
    companion object {
        /*
        @Volatile: meaning that writes to this field are immediately made visible to other threads.
         */
        @Volatile
        private var instance: MySingleton? = null

        @Synchronized
        fun getInstance(context: Context) = instance?: synchronized(this){
            instance?: MySingleton(context)
        }
    }

    init {

    }

    /*
    by lazy: requestQueue won't be initialized until this method gets called
     */
    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    @Synchronized
    fun getmInstance(context: Context?): MySingleton? {
        if (instance == null) {
            instance = MySingleton(context!!)
        }
        return instance
    }

    fun <T> addToRequestQueue(req: Request<T>){
        requestQueue.add(req)
    }
}