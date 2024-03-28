package com.test.application.mapp.models

//import com.test.application.mapp.GeoJsonObjectDto
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.test.application.mapp.AppContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ModelImp:Model {
    //private val ip:String = "192.168.1.100" //WiFi
    private val ip:String = "192.168.1.60"  //Кабель

    override suspend fun startRequest(): String? = suspendCoroutine { contAviaFreeFlying ->

        val client = OkHttpClient()

        val url = URL("http://$ip:8080/geojson")
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                contAviaFreeFlying.resume(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val resp = response.body?.string()
                contAviaFreeFlying.resume(resp)
            }
        })
    }

    override suspend fun checkInternet(): Boolean {
        return suspendCoroutine { continuation ->
            val connectivityManager = AppContext.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNet = connectivityManager.activeNetwork
            if (activeNet != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNet)
                val resp = networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                continuation.resume(resp)
            }else{
                continuation.resume(false)
            }
        }
    }
}