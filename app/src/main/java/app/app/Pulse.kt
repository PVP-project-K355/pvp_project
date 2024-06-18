package app.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.NavHostFragment

import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import app.app.heartrate.HeartRateData
import app.app.utils.TimeValueFormatter

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import org.json.JSONObject
import java.io.IOException

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import android.telephony.SmsManager

class Pulse : Fragment() {

    private lateinit var currentDateTextView: TextView
    private lateinit var prevDayButton: Button
    private lateinit var nextDayButton: Button
    private lateinit var graphView: LineChart
    private lateinit var summaryTextView: TextView
    private lateinit var calendar: Calendar
    private lateinit var dayAverageTextView: TextView
    private lateinit var weekAverageTextView: TextView
    private lateinit var accessToken: String
    private lateinit var progressBar: ProgressBar
    private var status = 0
    //test button
    private lateinit var testButton: Button
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_pulse, container, false)
        view.findViewById<Button>(R.id.maingpage_button).setOnClickListener {
            NavHostFragment.findNavController(this@Pulse).popBackStack(R.id.mainpage, false) }

        dbHelper = DBHelper(requireContext())

        var token = dbHelper.getValidAccessToken()
        if(token!=null){
            accessToken = token.toString()
        }


        currentDateTextView = view.findViewById(R.id.currentDateTextView)
        prevDayButton = view.findViewById(R.id.prevDayButton)
        nextDayButton = view.findViewById(R.id.nextDayButton)
        graphView = view.findViewById(R.id.graphView)
        summaryTextView = view.findViewById(R.id.summaryTextView)
        //dayAverageTextView = view.findViewById(R.id.dayAverageTitleTextView)
        //weekAverageTextView = view.findViewById(R.id.weekAverageTitleTextView)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        calendar = Calendar.getInstance()
        //db insert button
        testButton = view.findViewById(R.id.buttonTest)
        updateDate()
        setupLineChart()

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

        testButton.setOnClickListener {

        }
        launchHRDCoroutine()

        return view
    }
    private fun launchHRDCoroutine() {
        // delay in ms between repeats of the iterations in the main endless while loop
        val fetchLockoutTime = 60000L
        // some thread wizardry to define what thread we're gonna run on
        val scope = MainScope()
        scope.launch {
            fetchHRDPeriodic(fetchLockoutTime)
        }

    }

    private suspend fun fetchHRDPeriodic(delay:Long){
        var index = 0
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        while (true) {
            val dateUTC = OffsetDateTime.now(ZoneOffset.UTC).format(formatter)
            Log.d("Perioic coroutine", "Fetch coroutine loop ${index}")
            index += 1
            //Uncomment line below to run fetch to db operation on coroutine
            fetchUpdateHRD(accessToken, dateUTC)
            delay(delay)
        }
    }

    private fun insertIntoDBAndCheckHRD(entries: List<HeartRateData.DatasetItem> = listOf(), date: String){
        // list of all the newly fetched values
        val listOfFetchValues = mutableListOf<HeartRate>()

        //Gets earliest possible unix timestamp for current day
        val earliestDate = Instant.parse("${date}T00:00:00Z")
        val earliestUnixTimestamp = earliestDate.epochSecond
        // Entry value parsing for insertion into the database
        for(entry in entries){
            val time = entry.time
            val dateVariable = Instant.parse("${date}T${time}Z")
            val unixTimestamp = dateVariable.epochSecond
            listOfFetchValues.add(HeartRate(heartRate = entry.value, date = unixTimestamp))
        }
        //finds the newest entry to get the latest timestamp
        val newestEntry = listOfFetchValues.last()
        //gets all the values from the earliest stored one for this day to the newest stored one up to newest entry timestamp
        val currentDBEntryList = dbHelper.getHeartRateEntries(earliestUnixTimestamp, newestEntry.date)

        // finds the amount of new values added in the new list to be inserted into the db
        // -- works kinda but is scuffed af, for reference of how not to do it don't use
        /*val size_diff = list_new.size - list_current.size
        for(index in list_new.size-size_diff until list_new.size){
            val entry = list_new.get(index)
            dbHelper.insertHeartRate(entry.date, entry.heart_rate)
        }*/

        //makes a list of all the timestamps in the database
        val datesInDB = currentDBEntryList.map { it.date }

        //removes all the duplicate values from the fetched list using timestamp list for comparison
        listOfFetchValues.removeAll {
            it.date in datesInDB
        }

        //insert the new values
        //TODO possible optimisation to make a bulk value inserter in db helper class
        for(entry in listOfFetchValues){
            Log.d("DBvalue", "timestamp: ${entry.date} Heartrate: ${entry.heartRate}")
            dbHelper.insertHeartRate(entry.date, entry.heartRate)
        }

        testHRDThresholds(date, listOfFetchValues)
    }

    private fun testHRDThresholds(date: String, entries: MutableList<HeartRate>){
        if (!entries.isEmpty()){
            var sentMesg = false

            val currentTimeStamp = System.currentTimeMillis() / 1000
            val currentMinTestTimestampThreshold = currentTimeStamp - 3600
            var threshold : Threshold?
            if(dbHelper.getThreshold(1)!= null){
                threshold = dbHelper.getThreshold(1)
            }
            else{
                threshold = Threshold(id = 1, minRate = 10, maxRate = 100, stepsGoal = 30000)
            }

            for(entry in entries){
                //check if value is within the last 1h
                if (entry.date > currentMinTestTimestampThreshold){
                    if (threshold != null) {
                        if (entry.heartRate >= threshold.maxRate || entry.heartRate <= threshold.minRate){
                            if (!sentMesg){
                                sentMesg = true
                                try {
                                    val message = "Warning, Heart rate past set limits detected"
                                    val allContacts = dbHelper.getAllContacts()
                                    val smsManager:SmsManager = requireContext().getSystemService(SmsManager::class.java)

                                    if (allContacts.isNotEmpty()){
                                        allContacts.forEach{ c ->
                                            smsManager.sendTextMessage(c.phoneNumber, null, message, null, null)
                                        }
                                        Log.d("msg send", "MSG sent")
                                    }
                                    else{
                                        println("Contacts not found")
                                    }

                                } catch (e: Exception) {
                                    Log.d("failed message send", e.message.toString())
                                }

                            }
                            else{
                                Log.d("Threshold limit", "timestamp: ${entry.date} Heartrate: ${entry.heartRate}")
                            }
                        }
                    }
                }
            }
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
                Log.d("FitbitHeartRateNetwork", "Response: network error")
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val heartRateEntries = parseHRD(responseBodyString)

                        insertIntoDBAndCheckHRD(heartRateEntries, date)

                        Log.d("FitbitHeartRateDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchHeartRateData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
//                    progressBar.visibility = View.GONE
//                    Toast.makeText(
//                        requireContext(),
//                        "Error retrieving heart rate data",
//                        Toast.LENGTH_SHORT
//                    ).show()
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
        requireActivity().runOnUiThread {
                 progressBar.visibility = View.GONE
             }
    }

    private fun fetchHeartRateData(accessToken: String, date: String) {
        requireActivity().runOnUiThread {
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
                progressBar.visibility = View.GONE
                Log.d("FitbitHeartRateNetwork", "Response: network error")
                Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body != null) {
                    try {
                        val responseBodyString = response.body!!.string()
                        val heartRateEntries = parseHeartRateData(responseBodyString)

                        updateGraphData(heartRateEntries)

                        Log.d("FitbitHeartRateDATA", "Response: $responseBodyString")
                    } catch (e: Exception) {
                        Log.e("FetchHeartRateData", "Error handling response: ${e.message}")
                    } finally {
                        response.body?.close()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
//                        Toast.makeText(
//                        requireContext(),
//                        "Error retrieving heart rate data",
//                        Toast.LENGTH_SHORT
//                        ).show()
                    }
                    Log.d("FitbitHeartRateDATA", "fucked up")

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
            requireActivity().runOnUiThread {
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