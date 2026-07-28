package com.rs.mymap.data.api

import android.content.Context
import okhttp3.Dns
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress

object RetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/"

    private var retrofit: Retrofit? = null

    fun getDirectionsApiService(context: Context): DirectionsApiService {
        if (retrofit == null) {
            val client = OkHttpClient.Builder()
                .dns(object : Dns {
                    override fun lookup(hostname: String): List<InetAddress> {
                        // Memaksa penggunaan IPv4 untuk menghindari ECONNREFUSED pada IPv6 emulator
                        return Dns.SYSTEM.lookup(hostname).sortedBy { it.address.size }
                    }
                })
                .addInterceptor(SaveResponseInterceptor(context))
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(DirectionsApiService::class.java)
    }
}
