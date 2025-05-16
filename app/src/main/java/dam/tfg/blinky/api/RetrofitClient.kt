package dam.tfg.blinky.api

import android.content.Context
import dam.tfg.blinky.config.AppConfig
import dam.tfg.blinky.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var tokenManager: TokenManager? = null

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

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.getServerUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }
}
