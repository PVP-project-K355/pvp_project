package app.app.heartrate

class HeartRateData {
    // Add properties to match the JSON response structure
    data class ActivitiesHeart(
        val activitiesHeart: List<ActivitiesHeartIntraday>,
        val activitiesHeartIntraday: ActivitiesHeartIntraday
    )

    data class ActivitiesHeartIntraday(
        val dataset: List<DatasetItem>,
        val datasetInterval: Int,
        val datasetType: String
    )

    data class HeartRateZone(
        val caloriesOut: Double,
        val max: Int,
        val min: Int,
        val minutes: Int,
        val name: String
    )

    data class DatasetItem(
        val time: String,
        val value: Int
    )
}
