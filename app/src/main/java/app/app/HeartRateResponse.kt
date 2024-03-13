package app.app

import com.google.gson.annotations.SerializedName

data class HeartRateResponse(
    @SerializedName("activities-heart") val activitiesHeart: List<HeartRateActivity>,
    @SerializedName("activities-heart-intraday") val activitiesHeartIntraday: HeartRateIntradayData
)

data class HeartRateActivity(
    val dateTime: String,
    val value: HeartRateValues
)

data class HeartRateValues(
    val customHeartRateZones: List<Any>, // Customize if needed
    val heartRateZones: List<HeartRateZone>,
    val restingHeartRate: Int
)

data class HeartRateZone(
    val caloriesOut: Double,
    val max: Int,
    val min: Int,
    val minutes: Int,
    val name: String
)

data class HeartRateIntradayData(
    val dataset: List<HeartRateDataSet>,
    val datasetInterval: Int,
    val datasetType: String
)

data class HeartRateDataSet(
    val time: String,
    val value: Int
)
