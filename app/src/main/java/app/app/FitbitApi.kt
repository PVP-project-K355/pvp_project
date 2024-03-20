//package app.app
//import android.util.Log
//import org.json.JSONObject
//import java.text.SimpleDateFormat
//import java.util.*
//import kotlin.collections.ArrayList
//import kotlin.concurrent.thread
//
//// Define the date range
//val startDateString = "2024-01-01"
//val endDateString = "2024-01-03"
//
//// Convert start and end dates to Date objects
//val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//val startDate = sdf.parse(startDateString)
//val endDate = sdf.parse(endDateString)
//
//// Generate date array
//val dateArray = ArrayList<Date>()
//val calendar = Calendar.getInstance()
//val calendar.time = startDate
//
//class calendar {
//
//}
//while (calendar.time.before(endDate)) {
//    dateArray.add(calendar.time)
//    calendar.add(Calendar.DATE, 1)
//}
//
//// Format date to yyyy-MM-dd
//val dayList = dateArray.map { date -> sdf.format(date) }
//
//Log.d("DayRange", dayList.toString())
//
//// Simulating asynchronous network call
//thread {
//    val accessToken = "YourAccessToken"
//    val header = mapOf("Authorization" to "Bearer $accessToken")
//
//    val dfAll = mutableListOf<JSONObject>()
//
//    dayList.forEach { singleDay ->
//        val response = getHeartRateData(singleDay, header)
//        try {
//            val json = JSONObject(response)
//            val dataset = json.getJSONObject("activities-heart-intraday").getJSONArray("dataset")
//            for (i in 0 until dataset.length()) {
//                dfAll.add(dataset.getJSONObject(i))
//            }
//        } catch (e: Exception) {
//            Log.e("Error", "No data for the given date $singleDay")
//        }
//    }
//
//    // Resampling data (here I'm just printing the result)
//    val summaryData = resampleData(dfAll)
//    Log.d("SummaryData", summaryData.toString())
//}
//
//// Function to get heart rate data from API
//fun getHeartRateData(date: String, header: Map<String, String>): String {
//    // Simulating API call, replace this with your actual network request
//    // Dummy JSON response
//    return """
//        {
//            "activities-heart-intraday": {
//                "dataset": [
//                    {"time": "00:00", "value": 70},
//                    {"time": "00:01", "value": 72},
//                    {"time": "00:02", "value": 75},
//                    {"time": "00:03", "value": 80}
//                    // Add more data as needed
//                ]
//            }
//        }
//    """.trimIndent()
//}
//
//// Function to resample data
//fun resampleData(data: List<JSONObject>): List<Pair<String, Double>> {
//    // Dummy resampling logic, replace with your actual resampling algorithm
//    val resampledData = mutableListOf<Pair<String, Double>>()
//    data.forEach { obj ->
//        val time = obj.getString("time")
//        val value = obj.getInt("value").toDouble()
//        resampledData.add(time to value)
//    }
//    return resampledData
//}