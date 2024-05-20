package app.app

// Import statements
import TimeValueFormatter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart // Add this import statement
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
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
import java.util.Locale
import java.util.Calendar

class HealthInfoActivity : AppCompatActivity() {

    private lateinit var currentDateTextView: TextView
    private lateinit var prevDayButton: Button
    private lateinit var nextDayButton: Button
    private lateinit var graphView: LineChart
    private lateinit var summaryTextView: TextView
    private lateinit var backButton: Button
    private lateinit var calendar: Calendar // Add Calendar instance
    private lateinit var dayAverageTextView: TextView
    private lateinit var weekAverageTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_info)

        // Initialize views
        currentDateTextView = findViewById(R.id.currentDateTextView)
        prevDayButton = findViewById(R.id.prevDayButton)
        nextDayButton = findViewById(R.id.nextDayButton)
        graphView = findViewById(R.id.graphView)
        summaryTextView = findViewById(R.id.summaryTextView)
        backButton = findViewById(R.id.backButton)
        dayAverageTextView = findViewById(R.id.dayAverageTitleTextView)
        weekAverageTextView = findViewById(R.id.weekAverageTitleTextView)

        // Initialize Calendar instance with current date
        calendar = Calendar.getInstance()

        // Set current date
        updateDate()
        // Set up LineChart
        setupLineChart()

        // Set click listener for the "Last Day" button
        prevDayButton.setOnClickListener {
            // Decrement day of the calendar
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            // Update displayed date
            updateDate()
            updateGraphData()
        }
        nextDayButton.setOnClickListener {
            // Decrement day of the calendar
            calendar.add(Calendar.DAY_OF_MONTH, +1)
            // Update displayed date
            updateDate()
            updateGraphData()
        }

        val accessTokenString = intent.getStringExtra("accestoken")
        fetchHeartRateData(accessTokenString.toString())

        // Navigate back to the main page
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun updateDate() {
        // Format the date and update the TextView
        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        currentDateTextView.text = formattedDate
    }

    private fun setupLineChart() {
        graphView.apply {
            xAxis.apply {
                valueFormatter = TimeValueFormatter()
                position = XAxis.XAxisPosition.TOP
                granularity = 1f // interval for the labels
            }
            axisLeft.axisMinimum = 0f // Assuming heart rate cannot be negative
            axisLeft.axisMaximum = 200f // Assuming maximum heart rate value
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false // Disable description text
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

        // Create a dataset with entries
        val dataSet = LineDataSet(displayEntries, "Heart Rate Data")

        // Customize dataset appearance
        dataSet.color = getColor(R.color.pastel_green)
        dataSet.setCircleColor(getColor(R.color.pastel_green))
        dataSet.setDrawValues(false)

        // Create a LineData object with the dataset
        val lineData = LineData(dataSet)

        // Set LineData to the LineChart
        graphView.data = lineData

        // Refresh the chart
        graphView.invalidate()
    }


    private fun fetchHeartRateData(accessToken: String) {
        val client = OkHttpClient()


        // Specify the endpoint to retrieve heart rate data
        val endpoint = "https://api.fitbit.com/1/user/-/activities/heart/date/today/1d/1sec.json"

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
                        val heartRateEntries = parseHeartRateData(responseBodyString)

                        runOnUiThread {
                            updateGraphData(heartRateEntries)
                        }

                        Log.d("FitbitHeartRateDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchHeartRateData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close() // Ensure response body is closed after reading
                    }
                } else {
                    runOnUiThread {
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
        try {
            val jsonObject = JSONObject(responseBodyString)
            val intradayData = jsonObject.getJSONObject("activities-heart-intraday")
            val dataset = intradayData.getJSONArray("dataset")

            for (i in 0 until dataset.length()) {
                val dataPoint = dataset.getJSONObject(i)
                val time = dataPoint.getString("time").split(":")
                val hour = time[0].toFloat()
                val minute = time[1].toFloat()
                val second = time[2].toFloat()
                val heartRate = dataPoint.getInt("value").toFloat()

                // Combine hour, minute, and second into a single float value representing time in hours
                val timeInHours = hour + (minute / 60) + (second / 3600)
                entries.add(Entry(timeInHours, heartRate))
            }
        } catch (e: Exception) {
            Log.e("ParseHeartRateData", "Error parsing heart rate data: ${e.message}")
        }

        return entries
    }

}


