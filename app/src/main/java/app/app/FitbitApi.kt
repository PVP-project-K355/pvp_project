package app.app

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface FitbitApi {
    @GET("/1/user/-/activities/heart/date/today/1d.json")
    fun getHeartRateData(@Header("Authorization") accessToken: String): Call<HeartRateResponse>
}
