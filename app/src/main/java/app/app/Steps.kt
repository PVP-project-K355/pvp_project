package app.app

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


class StepsFragment : Fragment() {

    private lateinit var currentDateTextView: TextView
    private lateinit var stepsPieChart: PieChart
    private lateinit var progressBar: ProgressBar
    private lateinit var calendar: Calendar
    private lateinit var accessToken: String
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dbHelper: DBHelper
    private lateinit var stepCounterTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_steps, container, false)
        view.findViewById<Button>(R.id.maingpage_button).setOnClickListener { NavHostFragment.findNavController(this@StepsFragment).popBackStack(R.id.mainpage, false) }
        currentDateTextView = view.findViewById(R.id.currentDateTextView)
        stepsPieChart = view.findViewById(R.id.stepsPieChart)
        progressBar = view.findViewById(R.id.progressBar)

        calendar = Calendar.getInstance()

        dbHelper = DBHelper(requireContext())

        var token = dbHelper.getValidAccessToken()
        if (token!=null){
            accessToken = token.toString()
        }
        updateDate()
        setupPieChart()
        //updateStepCounter(currentSteps);
        fetchStepsData(accessToken, getFormattedDate(calendar))
        stepCounterTextView = view.findViewById(R.id.step_counter_text_view);

        // Update step counter based on fetched data
        //updateStepCounter(currentSteps);
        return view
    }
    private fun updateStepCounter(currentSteps: Int, stepsGoal: Int) {
        stepCounterTextView.text = String.format("%d/%d Steps", currentSteps, stepsGoal)
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
        updateStepCounter(currentSteps.toInt(), stepsGoal) // Update counter based on current steps

    }

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
        progressBar.visibility = View.VISIBLE

        val client = OkHttpClient()
        val endpoint = "https://api.fitbit.com/1/user/-/activities/date/$date.json"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                progressBar.visibility = View.GONE
                Log.d("FitbitStepsNetwork", "Response: network error")
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val (currentSteps, stepsGoal) = parseStepsData(responseBodyString)

                        handler.post {
                            updateGraphData(currentSteps, stepsGoal)
                            updateStepCounter(currentSteps.toInt(), stepsGoal)
                        }

                        Log.d("FitbitStepsDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchStepsData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Error retrieving steps data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun parseStepsData(responseBodyString: String): Pair<Float, Int> {
        var currentSteps = 0f
        var stepsGoal = 0
        try {
            val jsonObject = JSONObject(responseBodyString)
            currentSteps = jsonObject.getJSONObject("summary").getInt("steps").toFloat()
            stepsGoal = jsonObject.getJSONObject("goals").getInt("steps")
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

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(fetchDataRunnable)
    }
}
