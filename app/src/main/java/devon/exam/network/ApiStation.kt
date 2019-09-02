package devon.exam.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object ApiStation {

    private val BASE_URL = "https://jsonplaceholder.typicode.com/"

    private const val TIMEOUT_CONNECT = 30L
    private const val TIMEOUT_READ = 30L
    private const val TIMEOUT_WRITE = 30L

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    private val TAG = javaClass.simpleName

    private val retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { Log.d(TAG, it) }
            .setLevel(HttpLoggingInterceptor.Level.BASIC)

        val client = OkHttpClient.Builder().run {
            connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
            readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
            writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS)
            addInterceptor(loggingInterceptor)
            cache(HttpCache.getInstance())
            build()
        }

        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(client)
            .build()
    }
}