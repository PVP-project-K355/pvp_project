package app.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
//import com.github.mikephil.chart.components.Axis
//import com.github.mikephil.chart.components.Legend
//import com.github.mikephil.chart.data.BarData
//import com.github.mikephil.chart.data.BarDataSet
//import com.github.mikephil.chart.data.BarEntry
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import java.io.IOException
import com.github.mikephil.charting.charts.BarChart
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Sleep : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var sleepDurationTextView: TextView
    private lateinit var sleepChart: BarChart
    private lateinit var currentDateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)

        progressBar = view.findViewById(R.id.progressBar)
        sleepDurationTextView = view.findViewById(R.id.sleepDurationTextView)
        sleepChart = view.findViewById(R.id.sleepChart)
        currentDateTextView = view.findViewById(R.id.currentDateTextView)

        setupChart()
        updateDate()

        view.findViewById<Button>(R.id.maingpage_button).setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }

        // Fetch sleep data on fragment creation
        fetchSleepData(arguments?.getString("accesstoken"))

        return view
    }

    private fun updateDate() {
        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        currentDateTextView.text = formattedDate
    }

    private fun setupChart() {
        sleepChart.apply {
            axisLeft.isEnabled = false
            axisRight.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            legend.isEnabled = false  // Disable legend here
            description.isEnabled = false
            setDrawBarShadow(false)
            setPinchZoom(false)
            setDrawValueAboveBar(true)
        }
    }

    private fun fetchSleepData(accessToken: String?) {
        if (accessToken.isNullOrEmpty()) {
            // Handle missing access token error
            return
        }

        progressBar.visibility = View.VISIBLE

        val client = OkHttpClient()
        val endpoint = "https://api.fitbit.com/1.user/-/sleep/date/${getFormattedDate()}.json"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    // Handle network error
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val sleepData = parseSleepData(responseBodyString)
                        runOnUiThread {
                            updateSleepDataUI(sleepData)
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Log.e("FetchSleepData", "Error parsing sleep data: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        // Handle error retrieving sleep data
                    }
                }
            }
        })
    }

    private fun getFormattedDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun parseSleepData(responseBodyString: String): List<SleepData> {
        val sleepDataList = mutableListOf<SleepData>()
        try {
            val jsonArray = JSONArray(responseBodyString)
            for (i in 0 until jsonArray.length()) {
                val sleepObject = jsonArray.getJSONObject(i)
                val startTime = sleepObject.getString("startTime")
                val endTime
