package app.app

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment


class DataInputPersonal : Fragment() {

    lateinit var buttonNext : Button
    lateinit var inputWeight : EditText
    lateinit var inputHeight : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonNext = view.findViewById<Button>(R.id.button_next_setup)
        buttonNext.setBackgroundResource(R.drawable.button_white)

        inputWeight = view.findViewById<EditText>(R.id.inputWeight)

        inputHeight = view.findViewById<EditText>(R.id.inputHeight)

        inputWeight.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateWeight(view)
            }
        })

        inputHeight.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                validateHeight(view)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_input_personal, container, false)
        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener{view.findNavController().navigate(R.id.action_dataInputPersonal_to_dataInputThresholds)}
        view.findViewById<Button>(R.id.button_back_setup).setOnClickListener { NavHostFragment.findNavController(this@DataInputPersonal).popBackStack() }
        return view
    }

    private fun validateWeight(view: View)
    {
        val value = view.findViewById<EditText>(R.id.inputWeight).text.toString().toIntOrNull()

        if(value != null)
            view.findViewById<TextView>(R.id.inputWeightUnits).setBackgroundResource(R.drawable.gradient_green_light_pressed)
        else
            view.findViewById<TextView>(R.id.inputWeightUnits).setBackgroundResource(R.drawable.gradient_red_pressed)

        validateForm(view)
    }

    private fun validateHeight(view: View)
    {
        val value = view.findViewById<EditText>(R.id.inputHeight).text.toString().toIntOrNull()

        if(value != null)
            view.findViewById<TextView>(R.id.inputHeightUnits).setBackgroundResource(R.drawable.gradient_green_light_pressed)
        else
            view.findViewById<TextView>(R.id.inputHeightUnits).setBackgroundResource(R.drawable.gradient_red_pressed)

        validateForm(view)
    }

    private fun validateForm(view: View)
    {
        if(true)
        {
            view.findViewById<Button>(R.id.button_next_setup).setBackgroundResource(R.drawable.button_blue_soft)
        }
    }
}