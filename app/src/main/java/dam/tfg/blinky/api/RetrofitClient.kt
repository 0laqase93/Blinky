package dam.tfg.blinky.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object RetrofitClient {
    private var tokenManager: TokenManager? = null

    // Custom TypeAdapter for LocalDateTime
    private class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        @Throws(IOException::class)
        override fun write(out: JsonWriter, value: LocalDateTime?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(formatter.format(value))
            }
        }

        @Throws(IOException::class)
        override fun read(reader: JsonReader): LocalDateTime? {
            if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            val dateStr = reader.nextString()
            return LocalDateTime.parse(dateStr, formatter)
        }
    }

    // Create a custom Gson instance with LocalDateTime adapter
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    fun initialize(context: Context) {
        tokenManager = TokenManager(context)
        AppConfig.initialize(context)
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenManager?.getToken()

        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        chain.proceed(newRequest)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val ttlApi: TTLApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(TTLApiService::class.java)
    }

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ChatApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApiService::class.java)
    }

    val eventApi: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(EventApiService::class.java)
    }
}
