package app.app
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DataInputPage : AppCompatActivity() {

    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var birthDate: TextView
    private lateinit var nameSurname : EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: Button
    private lateinit var chooseDate: Button

    private val dbHelper = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_input)

        // Initialize views
        heightInput = findViewById(R.id.heightIn)
        weightInput = findViewById(R.id.weightIn)
        birthDate = findViewById(R.id.birthDateIn)
        nameSurname = findViewById(R.id.nameSurname)
        saveButton = findViewById(R.id.saveUser)
        cancelButton = findViewById(R.id.cancelUser)
        backButton = findViewById(R.id.goToSettingsUser)
        chooseDate = findViewById(R.id.chooseBirth)

        //Save user data
        saveButton.setOnClickListener {
            saveUserData()
        }

        cancelButton.setOnClickListener {
            loadUserData()
            showToast("Changes have been canceled")
            finish()
        }

        // Navigate back to the settings page
        backButton.setOnClickListener {
            val intent = Intent(this, SettingsPage::class.java)
            startActivity(intent)
        }

        chooseDate.setOnClickListener {
            showDatePickerDialog(birthDate)
        }

        // Load user data into EditText fields (if applicable)
        loadUserData()
    }

    private fun saveUserData() {
        // Retrieve user input from EditText fields
        val height = heightInput.text.toString().toDoubleOrNull()
        val weight = weightInput.text.toString().toDoubleOrNull()
        val birth = birthDate.text.toString()
        val nameSur = nameSurname.text.toString()
        val parts = nameSur.split(" ", limit = 3)

        // Validate input
        if (height != null && weight != null && !birth.isNullOrBlank() && parts.size == 2) {

            val name = parts[0]
            val surname = parts[1]

            if(dbHelper.getUser(1)==null){
                val user = User(
                    name = name,
                    surname = surname,
                    height = height,
                    weight = weight,
                    birthdate = birth
                )
                dbHelper.addUser(user)
            }
            else if(dbHelper.getUser(1)!=null){
                val user = User(
                    id = 1,
                    name = name,
                    surname = surname,
                    height = height,
                    weight = weight,
                    birthdate = birth
                )
                dbHelper.updateUser(user)
            }
            showToast("Data saved successfully")
            finish()
        } else {
            // Inform the user that input is invalid
            showToast("Invalid input. Please enter valid data.")
        }
    }

    private fun loadUserData() {
        if(dbHelper.getUser(1)!=null){
            val user = dbHelper.getUser(1)
            nameSurname.setText(user?.name.toString()+ " " + user?.surname.toString())
            heightInput.setText(user?.height.toString())
            weightInput.setText(user?.weight.toString())
            birthDate.setText(user?.birthdate.toString())
        }

    }
    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(selectedDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                // Update the TextView with the selected date
                val formattedDate = "${selectedYear}-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDayOfMonth.toString().padStart(2, '0')}"
                selectedDateTextView.text = formattedDate
            },
            year, month, day)

        datePickerDialog.show()
    }
}
