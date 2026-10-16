package pe.edu.utp.perviajero.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

// REQ-05 / REQ-07: Cliente HTTP Retrofit oficial conectado a la API REST pública real
object ApiClient {

    // API REST pública oficial alojada en la nube (GitHub Raw CDN)
    private const val BASE_URL = "https://raw.githubusercontent.com/Edisonkz/Per-Viajero/main/"
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

    // Interceptor de red: verifica conectividad real y ejecuta llamada HTTP por internet
    private val networkInterceptor = Interceptor { chain ->
        if (!isOnline()) {
            throw IOException("No hay conexión a internet (dispositivo offline)")
        }
        chain.proceed(chain.request())
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(networkInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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
