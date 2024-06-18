package app.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

class DataInputPersonal : Fragment() {

    lateinit var buttonNext : Button

    lateinit var inputWeight : EditText
    lateinit var inputHeight : EditText
    lateinit var inputSurname : EditText
    lateinit var inputName : EditText

    lateinit var inputWeightUnits : TextView
    lateinit var inputHeightUnits : TextView
    lateinit var inputSurnameUnits : TextView
    lateinit var inputNameUnits : TextView

    var FORM_VALIDATION = false

    private var weight = ""
    private var height = ""
    private var name = ""
    private var surname = ""

    private var mToast: Toast? = null
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonNext = view.findViewById<Button>(R.id.button_next_setup)
        buttonNext.setBackgroundResource(R.drawable.button_white)

        inputWeight = view.findViewById<EditText>(R.id.inputWeight)
        inputHeight = view.findViewById<EditText>(R.id.inputHeight)
        inputName = view.findViewById<EditText>(R.id.inputName)
        inputSurname = view.findViewById<EditText>(R.id.inputSurname)

        inputWeightUnits = view.findViewById<TextView>(R.id.inputStepsUnits)
        inputHeightUnits = view.findViewById<TextView>(R.id.inputHeightUnits)
        inputNameUnits = view.findViewById<TextView>(R.id.inputNameUnits)
        inputSurnameUnits = view.findViewById<TextView>(R.id.inputSurnameUnits)

        name = dbHelper.getLogin(1)?.name.toString()
        surname = dbHelper.getLogin(1)?.surname.toString()
        view.findViewById<EditText>(R.id.inputName).setText(name)
        view.findViewById<EditText>(R.id.inputSurname).setText(surname)
        validateInput(inputName, inputNameUnits, view)
        validateInput(inputSurname, inputSurnameUnits, view)
        validateForm(view)

        inputName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                name = validateInput(inputName, inputNameUnits, view)
                validateForm(view)
            }
        })

        inputSurname.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                surname = validateInput(inputSurname, inputSurnameUnits, view)
                validateForm(view)
            }
        })
        inputWeight.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                weight = validateInput(inputWeight, inputWeightUnits, view)
                validateForm(view)
            }
        })

        inputHeight.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                height = validateInput(inputHeight, inputHeightUnits, view)
                validateForm(view)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_input_personal, container, false)
        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener{ next(view) }
        view.findViewById<Button>(R.id.button_back_setup).setOnClickListener { NavHostFragment.findNavController(this@DataInputPersonal).popBackStack() }
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
        if( weight != "" && height != "" && name != "" && surname != "" )
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

    private fun next(view: View)
    {
        if(FORM_VALIDATION) {
            dbHelper.addUser(
                User(
                    1,
                    inputName.text.toString(),
                    inputSurname.text.toString(),
                    inputHeight.text.toString().toDouble(),
                    inputWeight.text.toString().toDouble(),
                    ""
                )
            )
            view.findNavController().navigate(R.id.action_dataInputPersonal_to_dataInputThresholds)
        }
        else
        {
            mToast?.cancel()
            mToast = Toast.makeText(requireContext(), "Please enter your data.", Toast.LENGTH_LONG)
            mToast?.show()
        }
    }
}