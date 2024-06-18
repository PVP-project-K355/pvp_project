package app.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment

class SettingsThresholdPage : Fragment() {

    private lateinit var minSteps: EditText
    private lateinit var minRate: EditText
    private lateinit var maxRate: EditText
    private lateinit var save: Button
    private lateinit var settings: Button

    private lateinit var dbHelper: DBHelper

    lateinit var inputMinrateUnits : TextView
    lateinit var inputMaxrateUnits : TextView
    lateinit var inputStepsUnits : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings_threshold_page, container, false)

        // Initialize DBHelper
        dbHelper = DBHelper(requireContext())

        // Find views by ID
        minSteps = view.findViewById(R.id.stepsGoalIn)
        minRate = view.findViewById(R.id.minRateIn)
        maxRate = view.findViewById(R.id.maxRateIn)
        save = view.findViewById(R.id.save)
        settings = view.findViewById(R.id.goToSettings)

        // Set click listeners
        save.setOnClickListener {
            saveThreshold()
        }

        settings.setOnClickListener {
            navigateBackToSettings()
        }

        //Load threshold data
        loadThreshold()

        return view
    }

    private fun saveThreshold() {
        val steps = minSteps.text.toString().toIntOrNull()
        val miniRate = minRate.text.toString().toIntOrNull()
        val maxiRate = maxRate.text.toString().toIntOrNull()

        if (steps != null && miniRate != null && maxiRate != null) {
            val threshold = Threshold(
                id = 1,
                minRate = miniRate,
                maxRate = maxiRate,
                stepsGoal = steps
            )

            if (dbHelper.getThreshold(1) == null) {
                dbHelper.addThreshold(threshold)
            } else {
                dbHelper.updateThreshold(threshold)
            }

            showToast("Data saved successfully")
            navigateBackToSettings()
        } else {
            // Inform the user that input is invalid
            showToast("Invalid input. Please enter valid data.")
        }
    }

    private fun loadThreshold() {
        val threshold = dbHelper.getThreshold(1)
        if (threshold != null) {
            minSteps.setText(threshold.stepsGoal.toString())
            minRate.setText(threshold.minRate.toString())
            maxRate.setText(threshold.maxRate.toString())
        }
    }

    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateBackToSettings() {
        NavHostFragment.findNavController(this).navigate(R.id.settings)
    }
}
