package app.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

class DataInputThresholds : Fragment() {

    lateinit var inputMinrate : EditText
    lateinit var inputMaxrate : EditText
    lateinit var inputSteps : EditText

    lateinit var inputMinrateUnits : TextView
    lateinit var inputMaxrateUnits : TextView
    lateinit var inputStepsUnits : TextView

    var FORM_VALIDATION = false

    private var minrate = ""
    private var maxrate = ""
    private var steps = ""

    private var mToast: Toast? = null
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_input_thresholds, container, false)
        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener{ finishSetup(view) }
        view.findViewById<Button>(R.id.button_back_setup).setOnClickListener { NavHostFragment.findNavController(this@DataInputThresholds).popBackStack() }

        inputMinrate = view.findViewById<EditText>(R.id.inputMinrate)
        inputMaxrate = view.findViewById<EditText>(R.id.inputMaxrate)
        inputSteps = view.findViewById<EditText>(R.id.inputSteps)

        inputMinrateUnits = view.findViewById<TextView>(R.id.inputMinrateUnits)
        inputMaxrateUnits = view.findViewById<TextView>(R.id.inputMaxrateUnits)
        inputStepsUnits = view.findViewById<TextView>(R.id.inputStepsUnits)

        inputMinrate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                minrate = validateInput(inputMinrate, inputMinrateUnits, view)
                validateForm(view)
            }
        })

        inputMaxrate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                maxrate = validateInput(inputMaxrate, inputMaxrateUnits, view)
                validateForm(view)
            }
        })
        inputSteps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                steps = validateInput(inputSteps, inputStepsUnits, view)
                validateForm(view)
            }
        })

        return view
    }

    private fun validateInput(editText: EditText, inputView: TextView, view: View) : String
    {
        val valueString = editText.text.toString()

        if(valueString == "")
            inputView.setBackgroundResource(R.drawable.gradient_red_pressed)
        else{
            inputView.setBackgroundResource(R.drawable.gradient_green_light_pressed)
        }

        return valueString
    }

    private fun validateForm(view: View)
    {
        if( minrate != "" && maxrate != "" && steps != "" && inputMinrate.text.toString().toDouble() < inputMaxrate.text.toString().toDouble())
        {
            view.findViewById<Button>(R.id.button_next_setup).setBackgroundResource(R.drawable.button_blue_soft)
            FORM_VALIDATION = true
        }
        else
        {
            view.findViewById<Button>(R.id.button_next_setup).setBackgroundResource(R.drawable.button_white)
            FORM_VALIDATION = false
        }
    }

    private fun finishSetup(view: View)
    {
        if(FORM_VALIDATION)
        {
            dbHelper.addThreshold(
                Threshold(
                    1,
                    inputMinrate.text.toString().toInt(),
                    inputMaxrate.text.toString().toInt(),
                    inputSteps.text.toString().toInt()
                )
            )
            view.findNavController().navigate(R.id.action_dataInputThresholds_to_mainActivity1)
            val sharedPreference = this.activity?.getSharedPreferences(
                SHARED_PREFS_NAME,
                AppCompatActivity.MODE_PRIVATE
            )
            val editor = sharedPreference?.edit()
            editor?.putBoolean("first_time_launch", false)?.commit()
            Log.e("TAG", "SETUP " + sharedPreference?.getBoolean("first_time_launch", true).toString())
        }
        else
        {
            mToast?.cancel()
            mToast = Toast.makeText(requireContext(), "Please enter your thresholds.", Toast.LENGTH_LONG)
            mToast?.show()
        }
    }
}