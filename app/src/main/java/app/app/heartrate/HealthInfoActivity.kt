package app.app.heartrate

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.app.DBHelper
import app.app.HeartRate
import app.app.R
import app.app.utils.TimeValueFormatter
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Locale

class HealthInfoActivity : AppCompatActivity() {

    private lateinit var currentDateTextView: TextView
    private lateinit var prevDayButton: Button
    private lateinit var nextDayButton: Button
    private lateinit var graphView: LineChart
    private lateinit var summaryTextView: TextView
    private lateinit var backButton: Button
    private lateinit var calendar: Calendar
    private lateinit var dayAverageTextView: TextView
    private lateinit var weekAverageTextView: TextView
    private lateinit var accessToken: String
    private lateinit var progressBar: ProgressBar
    private var status = 0
    //test button
    private lateinit var testButton: Button
    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_info)

        currentDateTextView = findViewById(R.id.currentDateTextView)
        prevDayButton = findViewById(R.id.prevDayButton)
        nextDayButton = findViewById(R.id.nextDayButton)
        graphView = findViewById(R.id.graphView)
        summaryTextView = findViewById(R.id.summaryTextView)
        backButton = findViewById(R.id.backButton)
        dayAverageTextView = findViewById(R.id.dayAverageTitleTextView)
        weekAverageTextView = findViewById(R.id.weekAverageTitleTextView)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        calendar = Calendar.getInstance()
        //db insert button
        testButton = findViewById(R.id.buttonTest)
        updateDate()
        setupLineChart()

        accessToken = intent.getStringExtra("accesstoken").toString()

        prevDayButton.setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            progressBar.visibility = View.VISIBLE
            fetchHeartRateData(accessToken, getFormattedDate(calendar))
            updateDate()
        }

        nextDayButton.setOnClickListener {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            progressBar.visibility = View.VISIBLE
            fetchHeartRateData(accessToken, getFormattedDate(calendar))
            updateDate()
        }

        if (status == 0) {
            fetchHeartRateData(accessToken, getFormattedDate(calendar))
            status = 1
        }

        backButton.setOnClickListener {
            finish()
        }

        testButton.setOnClickListener {
            testFunc()
        }
    }

    private fun testFunc(){
        val date = "2024-05-29"
        fetchUpdateHRD(accessToken, date)
    }

    private fun insertIntoDB(entries: List<HeartRateData.DatasetItem> = listOf(), date: String){
        // list of all the newly fetched values
        val list_new = mutableListOf<HeartRate>()

        val date_earliest = Instant.parse("${date}T00:00:00Z")
        val earliest_unixtimestamp = date_earliest.epochSecond
        for(entry in entries){
            val time = entry.time
            val date_var = Instant.parse("${date}T${time}Z")
            val unixtimestamp = date_var.epochSecond
            //Log.d("DBvalue", "timestamp: $unixtimestamp Heartrate: ${entry.value}")
            list_new.add(HeartRate(heartRate = entry.value, date = unixtimestamp))
            //dbHelper.insertHeartRate(unixtimestamp, entry.value)
        }
        //finds the newest entry to get the latest timestamp
        val newest_entry = list_new.last()
        //gets all the values from the earliest stored one for this day to the newest stored one up to newest entry timestamp
        val list_current = dbHelper.getHeartRateEntries(earliest_unixtimestamp, newest_entry.date)


        // finds the amount of new values added in the new list to be inserted into the db -- works kinda but is scuffed af
        /*val size_diff = list_new.size - list_current.size
        for(index in list_new.size-size_diff until list_new.size){
            val entry = list_new.get(index)
            dbHelper.insertHeartRate(entry.date, entry.heart_rate)
        }*/
        val datesInDB = list_current.map { it.date }
        list_new.removeAll {
            it.date in datesInDB
        }

        for(entry in list_new){
            Log.d("DBvalue", "timestamp: ${entry.date} Heartrate: ${entry.heartRate}")
            dbHelper.insertHeartRate(entry.date, entry.heartRate)
        }
    }
    private fun fetchUpdateHRD(accessToken: String, date: String) {
        val client = OkHttpClient()
        val endpoint = "https://api.fitbit.com/1/user/-/activities/heart/date/$date/1d/1sec.json?timezone=UTC"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.d("FitbitHeartRateNetwork", "Response: network error")
                    Toast.makeText(this@HealthInfoActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val heartRateEntries = parseHRD(responseBodyString)

                        insertIntoDB(heartRateEntries, date)

                        Log.d("FitbitHeartRateDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchHeartRateData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@HealthInfoActivity,
                            "Error retrieving heart rate data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun parseHRD(responseBodyString: String): List<HeartRateData.DatasetItem> {
        val entries = mutableListOf<HeartRateData.DatasetItem>()

        try {
            val jsonObject = JSONObject(responseBodyString)

            val intradayData = jsonObject.getJSONObject("activities-heart-intraday")
            val dataset = intradayData.getJSONArray("dataset")

            for (i in 0 until dataset.length()) {
                val dataPoint = dataset.getJSONObject(i)
                val time = dataPoint.getString("time")
                val heartRate = dataPoint.getInt("value")
                val entry = HeartRateData.DatasetItem(time, heartRate)
                entries.add(entry)
            }


        } catch (e: Exception) {
            Log.e("ParseHeartRateData", "Error parsing heart rate data: ${e.message}")
        }

        return entries
    }

    private fun updateDate() {
        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        currentDateTextView.text = formattedDate

        nextDayButton.visibility = if (isNextDayInFuture()) View.GONE else View.VISIBLE
    }

    private fun setupLineChart() {
        graphView.apply {
            xAxis.apply {
                valueFormatter = TimeValueFormatter()
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
            }
            axisLeft.axisMinimum = 0f
            axisLeft.axisMaximum = 200f
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
        }
    }

    private fun updateGraphData(entries: List<Entry> = listOf()) {
        val displayEntries = if (entries.isEmpty()) {
            listOf(
                Entry(0f, 60f),
                Entry(1f, 65f),
                Entry(2f, 70f),
                Entry(3f, 75f)
            )
        } else {
            entries
        }

        val dataSet = LineDataSet(displayEntries, "Heart Rate Data")
        dataSet.apply {
            color = getColor(R.color.pastel_green)
            setCircleColor(getColor(R.color.pastel_green))
            setDrawValues(false)
            setDrawCircles(true)
            setDrawCircleHole(false)
            circleRadius = 3f
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(dataSet)
        graphView.data = lineData
        graphView.invalidate()
        progressBar.visibility = View.GONE
    }

    private fun fetchHeartRateData(accessToken: String, date: String) {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
        }

        val client = OkHttpClient()
        val endpoint = "https://api.fitbit.com/1/user/-/activities/heart/date/$date/1d/1sec.json"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Log.d("FitbitHeartRateNetwork", "Response: network error")
                    Toast.makeText(this@HealthInfoActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val heartRateEntries = parseHeartRateData(responseBodyString)

                        runOnUiThread {
                            updateGraphData(heartRateEntries)
                        }

                        Log.d("FitbitHeartRateDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchHeartRateData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@HealthInfoActivity,
                            "Error retrieving heart rate data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun parseHeartRateData(responseBodyString: String): List<Entry> {
        val entries = mutableListOf<Entry>()
        var restingHeartRate: Int? = null
        var minHeartRate: Int? = null
        var maxHeartRate: Int? = null

        try {
            val jsonObject = JSONObject(responseBodyString)
            val activitiesHeart = jsonObject.getJSONArray("activities-heart").getJSONObject(0)
            try {
                restingHeartRate = activitiesHeart.getJSONObject("value").getInt("restingHeartRate")
            } catch (e: Exception) {
                Log.e("ParseHeartRateData", "Error parsing resting heart rate: ${e.message}")
                restingHeartRate = 0
            }

            val intradayData = jsonObject.getJSONObject("activities-heart-intraday")
            val dataset = intradayData.getJSONArray("dataset")

            for (i in 0 until dataset.length()) {
                val dataPoint = dataset.getJSONObject(i)
                val time = dataPoint.getString("time").split(":")
                val hour = time[0].toFloat()
                val minute = time[1].toFloat()
                val second = time[2].toFloat()
                val heartRate = dataPoint.getInt("value").toFloat()

                val timeInHours = hour + (minute / 60) + (second / 3600)
                entries.add(Entry(timeInHours, heartRate))

                if (minHeartRate == null || heartRate < minHeartRate) {
                    minHeartRate = heartRate.toInt()
                }
                if (maxHeartRate == null || heartRate > maxHeartRate) {
                    maxHeartRate = heartRate.toInt()
                }
            }

            val summaryText = "Resting Heart Rate: $restingHeartRate bpm\n" +
                    "Min Heart Rate: $minHeartRate bpm\n" +
                    "Max Heart Rate: $maxHeartRate bpm"
            runOnUiThread {
                summaryTextView.text = summaryText
            }

        } catch (e: Exception) {
            Log.e("ParseHeartRateData", "Error parsing heart rate data: ${e.message}")
        }

        return entries
    }

    private fun getFormattedDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun isNextDayInFuture(): Boolean {
        val today = Calendar.getInstance()
        val nextDay = calendar.clone() as Calendar
        nextDay.add(Calendar.DAY_OF_MONTH, 1)
        return nextDay.after(today)
    }
}
