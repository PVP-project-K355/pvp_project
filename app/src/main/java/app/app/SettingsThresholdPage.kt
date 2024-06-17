package app.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsThresholdPage.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsThresholdPage : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var minSteps: EditText
    private lateinit var minRate: EditText
    private lateinit var maxRate: EditText
    private lateinit var save: Button
    private lateinit var cancel: Button
    private lateinit var settings: Button

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
        cancel = view.findViewById(R.id.cancel)
        settings = view.findViewById(R.id.goToSettings)

        // Set click listeners
        save.setOnClickListener {
            saveThreshold()
        }

        cancel.setOnClickListener {
            loadThreshold()
            showToast("Changes have been canceled")
            navigateBackToSettings()
        }

        settings.setOnClickListener {
            navigateToSettingsPage()
        }

        // Load threshold data
        loadThreshold()

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsThresholdPage.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsThresholdPage().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
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

    private fun navigateToSettingsPage() {
        NavHostFragment.findNavController(this).navigate(R.id.settings)
    }
}
