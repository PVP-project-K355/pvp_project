package app.app

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateAxisValueFormatter : ValueFormatter() {

    private val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        // Convert the float value (representing hours since epoch) to milliseconds
        val millis = (value * 60 * 60 * 1000).toLong()
        // Convert milliseconds to Date object
        val date = Date(millis)
        // Format the Date object to the desired format
        return hourFormat.format(date)
    }
}