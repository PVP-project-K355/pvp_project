package app.app
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DataInputPage : AppCompatActivity() {

    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_input)

        // Initialize views
        heightInput = findViewById(R.id.heightInput)
        weightInput = findViewById(R.id.weightInput)
        ageInput = findViewById(R.id.ageInput)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        // Set click listener for saveButton
        saveButton.setOnClickListener {
            saveUserData()
        }
        // Navigate back to the main page
        backButton.setOnClickListener {
            finish()
        }
        // Load user data into EditText fields (if applicable)
        loadUserData()
    }

    private fun saveUserData() {
        // Retrieve user input from EditText fields
        val height = heightInput.text.toString().toFloatOrNull()
        val weight = weightInput.text.toString().toFloatOrNull()
        val age = ageInput.text.toString().toIntOrNull()

        // Validate input
        if (height != null && weight != null && age != null) {
            // Save user data to SharedPreferences, database, or any other storage mechanism
            // Example: Saving to SharedPreferences
            val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putFloat("height", height)
                putFloat("weight", weight)
                putInt("age", age)
                apply()
            }
            // Display toast message to notify the user
            showToast("Data saved successfully")
        } else {
            // Inform the user that input is invalid
            showToast("Invalid input. Please enter valid data.")
        }
    }

    private fun loadUserData() {
        // Load user data from SharedPreferences, database, or any other storage mechanism
        // Example: Loading from SharedPreferences
        val sharedPrefs = getSharedPreferences("UserData", MODE_PRIVATE)
        val savedHeight = sharedPrefs.getFloat("height", 0f)
        val savedWeight = sharedPrefs.getFloat("weight", 0f)
        val savedAge = sharedPrefs.getInt("age", 0)

        // Populate EditText fields with loaded data
        heightInput.setText(savedHeight.toString())
        weightInput.setText(savedWeight.toString())
        ageInput.setText(savedAge.toString())
    }
    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
