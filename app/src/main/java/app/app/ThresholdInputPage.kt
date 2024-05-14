package app.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ThresholdInputPage : AppCompatActivity() {

    private lateinit var minSteps: EditText
    private lateinit var minRate: EditText
    private lateinit var maxRate: EditText
    private lateinit var save: Button
    private lateinit var cancel: Button
    private lateinit var settings: Button

    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_threshold_input)

        minSteps = findViewById(R.id.stepsGoalIn)
        minRate = findViewById(R.id.minRateIn)
        maxRate = findViewById(R.id.maxRateIn)
        save = findViewById(R.id.save)
        cancel = findViewById(R.id.cancel)
        settings = findViewById(R.id.goToSettings)

        save.setOnClickListener{
            saveThreshold()
        }

        cancel.setOnClickListener {
            loadThreshold()
            showToast("Changes have been canceled")
        }

        settings.setOnClickListener {
            val intent = Intent(this, SettingsPage::class.java)
            startActivity(intent)
        }

        loadThreshold()

    }

    private fun saveThreshold(){

        val steps = minSteps.text.toString().toIntOrNull()
        val miniRate = minRate.text.toString().toIntOrNull()
        val maxiRate = maxRate.text.toString().toIntOrNull()

        if(steps != null && miniRate != null && maxiRate != null){
            if(dbHelper.getThreshold(1)== null){
                val threshold = Threshold(
                    minRate = miniRate,
                    maxRate = maxiRate,
                    stepsGoal = steps
                )
                dbHelper.addThreshold(threshold)
            }
            else if(dbHelper.getThreshold(1)!= null){
                val threshold = Threshold(
                    id = 1,
                    minRate = miniRate,
                    maxRate = maxiRate,
                    stepsGoal = steps
                )
                dbHelper.updateThreshold(threshold)
            }
            showToast("Data saved successfully")
        }
        else{
            // Inform the user that input is invalid
            showToast("Invalid input. Please enter valid data.")
        }
    }

    private fun loadThreshold(){
        if(dbHelper.getThreshold(1)!= null){
            val threshold = dbHelper.getThreshold(1)
            minSteps.setText(threshold?.stepsGoal.toString())
            minRate.setText(threshold?.minRate.toString())
            maxRate.setText(threshold?.maxRate.toString())
        }
    }

    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}