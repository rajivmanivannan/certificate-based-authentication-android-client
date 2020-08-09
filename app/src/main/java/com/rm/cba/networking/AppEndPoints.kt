package com.rm.cba.networking

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface AppEndPoints {
    @GET("/home")
    fun getCurrentUserData(): Call<ResponseBody>
}