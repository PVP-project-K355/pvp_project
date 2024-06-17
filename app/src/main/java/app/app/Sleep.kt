package app.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Sleep : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var sleepDurationTextView: TextView
    private lateinit var sleepPieChart: PieChart
    private lateinit var calendar: Calendar
    private lateinit var currentDateTextView: TextView
    private lateinit var deepSleepDurationTextView : TextView
    private lateinit var remSleepDurationTextView : TextView
    private lateinit var lightSleepDurationTextView : TextView
    private lateinit var sleepGoalTextView: TextView
    private lateinit var accessToken: String
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dbHelper: DBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sleep, container, false)
        view.findViewById<Button>(R.id.maingpage_button).setOnClickListener {
            NavHostFragment.findNavController(this).popBackStack()
        }
        deepSleepDurationTextView = view.findViewById(R.id.deepSleepDurationTextView)
        sleepGoalTextView = view.findViewById(R.id.sleepGoalTextView)
        remSleepDurationTextView = view.findViewById(R.id.remSleepDurationTextView)
        lightSleepDurationTextView = view.findViewById(R.id.lightSleepDurationTextView)
        currentDateTextView = view.findViewById(R.id.currentDateTextView)
        sleepPieChart = view.findViewById(R.id.sleepPieChart)
        progressBar = view.findViewById(R.id.progressBar)
        //progressBar = view.findViewById(R.id.progressBar)
        calendar = Calendar.getInstance()
        //progressBar = view.findViewById(R.id.progressBar)
        sleepDurationTextView = view.findViewById(R.id.sleepDurationTextView)
        //sleepChart = view.findViewById(R.id.sleepChart)

        dbHelper = DBHelper(requireContext())

        var token = dbHelper.getValidAccessToken()
        if (token != null) {
            accessToken = token.toString()
        }
        updateDate()
        setupPieChart()

        fetchSleepData(accessToken, getFormattedDate(calendar))

        return view
    }

    private fun updateDate() {
        val formattedDate =
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        currentDateTextView.text = formattedDate
    }

    private fun updateGraphData(totalMinutesAsleep: Int, deepMinutes: Int, lightMinutes: Int, remMinutes: Int, wakeMinutes: Int) {
        val entries = ArrayList<PieEntry>()


        entries.add(PieEntry(totalMinutesAsleep.toFloat(), "Total Minutes Asleep"))
        entries.add(PieEntry((8*60-totalMinutesAsleep).toFloat(), "Sleep goal"))

        val colors = ArrayList<Int>()
        colors.add(Color.GREEN)
        colors.add(Color.GRAY)

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors

        val pieData = PieData(dataSet)
        pieData.setDrawValues(false)

        sleepPieChart.data = pieData
        sleepPieChart.invalidate()
        val totalHours = totalMinutesAsleep / 60
        val totalMinutes = totalMinutesAsleep % 60
        val deep_hours=deepMinutes/60
        val deep_minutes=deepMinutes%60
        val light_hours=lightMinutes/60
        val light_minutes=lightMinutes%60
        val rem_hours=remMinutes/60
        val rem_minutes=remMinutes%60
        sleepGoalTextView.text = "Sleep goal: 8 hours"
        sleepDurationTextView.text = "Total sleep: $totalHours h. $totalMinutes min."
        deepSleepDurationTextView.text = "Deep sleep: $deep_hours h. $deep_minutes min."
        remSleepDurationTextView.text = "Rem sleep: $rem_hours h. $rem_minutes min."
        lightSleepDurationTextView.text = "Light sleep: $light_hours h. $light_minutes min."
    }


    private fun setupPieChart() {
        sleepPieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            setDrawEntryLabels(false)
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleColor(Color.TRANSPARENT)
        }
    }

    private fun fetchSleepData(accessToken: String, date: String) {
        progressBar.visibility = View.VISIBLE

        val client = OkHttpClient()
        val endpoint = "https://api.fitbit.com/1/user/-/sleep/date/$date.json"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                progressBar.visibility = View.GONE
                Log.d("FitbitSleepNetwork", "Response: network error")
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                handler.post {
                    progressBar.visibility = View.GONE
                }

                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val sleepData = parseSleepData(responseBodyString)

                        handler.post {
                            updateGraphData(sleepData.totalMinutesAsleep, sleepData.deepMinutes, sleepData.lightMinutes, sleepData.remMinutes, sleepData.wakeMinutes)
                        }
                    } catch (e: Exception) {
                        Log.e("FetchSleepData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    handler.post {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error retrieving sleep data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    fun parseSleepData(responseBodyString: String): SleepData {
        val jsonObject = JSONObject(responseBodyString)
        val sleepSummary = jsonObject.getJSONObject("summary").getJSONObject("stages")

        val deepMinutes = sleepSummary.getInt("deep")
        val lightMinutes = sleepSummary.getInt("light")
        val remMinutes = sleepSummary.getInt("rem")
        val wakeMinutes = sleepSummary.getInt("wake")
        val totalMinutesAsleep = deepMinutes+lightMinutes+remMinutes
        //val totalTimeInBed = sleepSummary.getInt("totalTimeInBed")
        Log.d("FitbitSleepNetwork", "Response: $responseBodyString")
        Log.d("FitbitSleepNetwork", "Deep Minutes: $deepMinutes")
        Log.d("FitbitSleepNetwork", "Light Minutes: $lightMinutes")
        Log.d("FitbitSleepNetwork", "REM Minutes: $remMinutes")
        Log.d("FitbitSleepNetwork", "Wake Minutes: $wakeMinutes")
        Log.d("FitbitSleepNetwork", "Total Minutes Asleep: $totalMinutesAsleep")
        //Log.d("FitbitSleepNetwork", "Total Time In Bed: $totalTimeInBed")
        Log.d("FitbitSleepNetwork", "Response: $responseBodyString")

        return SleepData(
            deepMinutes,
            lightMinutes,
            remMinutes,
            wakeMinutes,
            totalMinutesAsleep,
        )
    }

    data class SleepData(
        val deepMinutes: Int,
        val lightMinutes: Int,
        val remMinutes: Int,
        val wakeMinutes: Int,
        val totalMinutesAsleep: Int
    )

    private fun getFormattedDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    private val fetchDataRunnable = object : Runnable {
        override fun run() {
            fetchSleepData(accessToken, getFormattedDate(calendar))
            handler.postDelayed(this, 10 * 60 * 1000) // Fetch every 10 minutes
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(fetchDataRunnable)
    }
}


