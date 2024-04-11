package app.app

// Import statements
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart // Add this import statement
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
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
// Populate the graph with blood pressure data
        populateGraphWithBloodPressureData()

        // TODO: Implement graph plotting using MPAndroidChart library

        // TODO: Calculate and display health summary

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
            xAxis.valueFormatter = DateAxisValueFormatter()
            axisLeft.axisMinimum = 0f // Assuming blood pressure cannot be negative
            axisLeft.axisMaximum = 200f // Assuming maximum blood pressure value
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun updateGraphData() {
        // TODO: Fetch data for the selected date and update the graph accordingly
        // Example data for demonstration purposes
        val entries = mutableListOf<Entry>()
        entries.add(Entry(0f, 10f))
        entries.add(Entry(1f, 20f))
        entries.add(Entry(2f, 15f))
        entries.add(Entry(3f, 25f))

        // Create a dataset with entries
        val dataSet = LineDataSet(entries, "Health Data")

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
    private fun populateGraphWithBloodPressureData() {
        // Example blood pressure data (hour of the day, blood pressure)
        val bloodPressureData = listOf(
            Pair(6f, 120f),
            Pair(8f, 130f),
            Pair(10f, 140f),
            Pair(12f, 135f),
            Pair(14f, 125f),
            Pair(16f, 130f),
            Pair(18f, 140f),
            Pair(20f, 125f),
            Pair(22f, 120f)
        )

        // Create entries for the LineDataSet
        val entries = mutableListOf<Entry>()
        bloodPressureData.forEachIndexed { index, data ->
            entries.add(Entry(data.first, data.second))
        }

        // Create a LineDataSet
        val dataSet = LineDataSet(entries, "Blood Pressure")

        // Customize the appearance of the LineDataSet
        // (e.g., color, circle color, line thickness, etc.)

        // Create a LineData object with the LineDataSet
        val lineData = LineData(dataSet)

        // Set the LineData to the LineChart
        graphView.data = lineData

        // Refresh the chart
        graphView.invalidate()
    }
}