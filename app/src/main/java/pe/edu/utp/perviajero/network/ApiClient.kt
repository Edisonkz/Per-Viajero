package pe.edu.utp.perviajero.network

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

// Cliente HTTP Retrofit oficial conectado a la API REST pública y servicios de enrutamiento
object ApiClient {

    private const val BASE_URL = "https://raw.githubusercontent.com/Edisonkz/Per-Viajero/main/"
    private const val OSRM_BASE_URL = "https://router.project-osrm.org/"
    private const val CACHE_SIZE_BYTES = 10L * 1024L * 1024L // 10 MB

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun isOnline(): Boolean {
        val context = appContext ?: return true
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getCacheSizeBytes(): Long {
        val cacheDir = appContext?.cacheDir?.let { File(it, "http_cache") } ?: return 0L
        if (!cacheDir.exists()) return 0L
        return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private val networkInterceptor = Interceptor { chain ->
        if (!isOnline()) {
            throw IOException("No hay conexión a internet (dispositivo offline)")
        }
        chain.proceed(chain.request())
    }

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(networkInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)

        appContext?.cacheDir?.let { dir ->
            builder.cache(Cache(File(dir, "http_cache"), CACHE_SIZE_BYTES))
        }

        val isDebug = appContext?.let {
            (it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } ?: false
        if (isDebug) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val osrmRetrofit by lazy {
        val osrmClient = okHttpClient.newBuilder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(OSRM_BASE_URL)
            .client(osrmClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: TourismApiService by lazy {
        retrofit.create(TourismApiService::class.java)
    }

    val osrmService: OsrmApiService by lazy {
        osrmRetrofit.create(OsrmApiService::class.java)
    }
}
