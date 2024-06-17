package app.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.content.Intent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Edit_User_Data_Page.newInstance] factory method to
 * create an instance of this fragment.
 */
class Edit_User_Data_Page : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var birthDate: TextView
    private lateinit var nameSurname: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var backButton: Button
    private lateinit var chooseDate: Button

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit__user__data__page, container, false)

        dbHelper = DBHelper(requireContext())

        heightInput = view.findViewById(R.id.heightIn)
        weightInput = view.findViewById(R.id.weightIn)
        birthDate = view.findViewById(R.id.birthDateIn)
        nameSurname = view.findViewById(R.id.nameSurname)
        saveButton = view.findViewById(R.id.saveUser)
        cancelButton = view.findViewById(R.id.cancelUser)
        backButton = view.findViewById(R.id.goToSettingsUser)
        chooseDate = view.findViewById(R.id.chooseBirth)

        saveButton.setOnClickListener {
            saveUserData()
        }

        cancelButton.setOnClickListener {
            loadUserData()
            showToast("Changes have been canceled")
            parentFragmentManager.popBackStack() // Go back to previous fragment
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack() // Navigate back to the previous fragment
        }

        chooseDate.setOnClickListener {
            showDatePickerDialog(birthDate)
        }

        loadUserData()

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Edit_User_Data_Page().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun saveUserData() {
        val height = heightInput.text.toString().toDoubleOrNull()
        val weight = weightInput.text.toString().toDoubleOrNull()
        val birth = birthDate.text.toString()
        val nameSur = nameSurname.text.toString()
        val parts = nameSur.split(" ", limit = 2)

        if (height != null && weight != null && birth.isNotBlank() && parts.size == 2) {
            val name = parts[0]
            val surname = parts[1]

            val user = User(
                id = 1,
                name = name,
                surname = surname,
                height = height,
                weight = weight,
                birthdate = birth
            )

            if (dbHelper.getUser(1) == null) {
                dbHelper.addUser(user)
            } else {
                dbHelper.updateUser(user)
            }
            showToast("Data saved successfully")
            parentFragmentManager.popBackStack() // Go back to the previous fragment
        } else {
            showToast("Invalid input. Please enter valid data.")
        }
    }

    private fun loadUserData() {
        dbHelper.getUser(1)?.let { user ->
            nameSurname.setText("${user.name} ${user.surname}")
            heightInput.setText(user.height.toString())
            weightInput.setText(user.weight.toString())
            birthDate.text = user.birthdate
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog(selectedDateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val formattedDate = "$selectedYear-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDayOfMonth.toString().padStart(2, '0')}"
                selectedDateTextView.text = formattedDate
            },
            year, month, day)

        datePickerDialog.show()
    }
}
