package com.rm.cba.networking
import com.rm.cba.app.AppConstants.Companion.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * ServiceBuilder
 * <p>
 * Service to perform the retrofit network calls.
 */
object ServiceBuilder {
    fun <T> buildService(client: OkHttpClient, service: Class<T>): T {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(service)
    }
}
