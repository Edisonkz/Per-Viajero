package pe.edu.utp.perviajero.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.getValue

// REQ-05 / REQ-07: Cliente HTTP Retrofit con soporte para simulación de estados de red
object ApiClient {

    private const val BASE_URL = "https://api.peruviajero.utp.pe/v1/"
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // Comprobación de estado de red en tiempo real
    fun isOnline(): Boolean {
        val context = appContext ?: return true
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Interceptor para gestionar conexión y proveer datos estructurados
    private val networkInterceptor = Interceptor { chain ->
        if (!isOnline()) {
            throw IOException("No hay conexión a internet")
        }

        val jsonMediaType = MediaType.parse("application/json; charset=utf-8")
        val responseBody = ResponseBody.create(jsonMediaType, MockData.DESTINOS_JSON)

        // Respuesta HTTP 200 con el catálogo de destinos turísticos
        Response.Builder()
            .request(chain.request())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(networkInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: TourismApiService by lazy {
        retrofit.create(TourismApiService::class.java)
    }
}

