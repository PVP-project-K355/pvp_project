package app.app.utils

import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale

class TimeValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val totalSeconds = (value * 3600).toLong()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60

        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}
