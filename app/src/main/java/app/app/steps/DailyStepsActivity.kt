package app.app.steps

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.app.MainActivity
import app.app.R
import app.app.utils.TimeValueFormatter
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyStepsActivity : AppCompatActivity() {

    private lateinit var currentDateTextView: TextView
    private lateinit var stepsPieChart: PieChart
    private lateinit var progressBar: ProgressBar
    private lateinit var calendar: Calendar
    private lateinit var backButton: Button
    private lateinit var accessToken: String
    private lateinit var dailyButton: Button
    private lateinit var weeklyButton: Button
    private lateinit var monthlyButton: Button
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_steps)

        currentDateTextView = findViewById(R.id.currentDateTextView)
        stepsPieChart = findViewById(R.id.stepsPieChart)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)
        weeklyButton = findViewById(R.id.weeklyButton)
        monthlyButton = findViewById(R.id.monthlyButton)

        calendar = Calendar.getInstance()
        accessToken = intent.getStringExtra("accesstoken").toString()

        updateDate()
        setupPieChart()

        fetchStepsData(accessToken, getFormattedDate(calendar))

        //handler.post(fetchDataRunnable)

        weeklyButton.setOnClickListener {
            startActivity(Intent(this, WeeklyStepsActivity::class.java))
        }

        monthlyButton.setOnClickListener {
            startActivity(Intent(this, MonthlyStepsActivity::class.java))
        }
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun updateDate() {
        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        currentDateTextView.text = formattedDate
    }


    private fun updateGraphData(currentSteps: Float, stepsGoal: Int) {
        progressBar.visibility = View.GONE

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(currentSteps, "Current Steps"))
        entries.add(PieEntry((stepsGoal - currentSteps).coerceAtLeast(0f), "Remaining Steps"))

        val colors = ArrayList<Int>()
        colors.add(Color.GREEN)
        colors.add(Color.GRAY)

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors

        val pieData = PieData(dataSet)
        pieData.setDrawValues(false)

        stepsPieChart.data = pieData
        stepsPieChart.invalidate()
    }
    //todo fix the circle to be more user friendly
    //todo add text for current steps and steps goal
    private fun setupPieChart() {
        stepsPieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            setDrawEntryLabels(false)
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
        }
    }


    private fun fetchStepsData(accessToken: String, date: String) {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
        }

        val client = OkHttpClient()
        val endpoint = "https://api.fitbit.com/1/user/-/activities/date/$date.json"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Log.d("FitbitStepsNetwork", "Response: network error")
                    Toast.makeText(this@DailyStepsActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val (currentSteps, stepsGoal) = parseStepsData(responseBodyString)

                        runOnUiThread {
                            updateGraphData(currentSteps, stepsGoal)
                        }

                        Log.d("FitbitStepsDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchStepsData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@DailyStepsActivity,
                            "Error retrieving steps data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun parseStepsData(responseBodyString: String): Pair<Float, Int> {
        var currentSteps = 0f
        var stepsGoal = 0
        try {
            val jsonObject = JSONObject(responseBodyString)
            // Extract current steps
            currentSteps = jsonObject.getJSONObject("summary").getInt("steps").toFloat()
            // Extract steps goal
            stepsGoal = jsonObject.getJSONObject("goals").getJSONObject("goals").getInt("steps")
        } catch (e: Exception) {
            Log.e("ParseStepsData", "Error parsing steps data: ${e.message}")
        }
        return Pair(currentSteps, stepsGoal)
    }

    private fun getFormattedDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchStepsData(accessToken, getFormattedDate(calendar))
            handler.postDelayed(this, 10 * 60 * 1000) // Fetch every 10 minutes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(fetchDataRunnable)
    }
}
