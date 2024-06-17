package app.app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

class DataInputThresholds : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_input_thresholds, container, false)
        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener{ finishSetup(view) }
        view.findViewById<Button>(R.id.button_back_setup).setOnClickListener { NavHostFragment.findNavController(this@DataInputThresholds).popBackStack() }
        return view
    }

    private fun finishSetup(view: View)
    {
        view.findNavController().navigate(R.id.action_dataInputThresholds_to_mainActivity1)
        val sharedPreference = this.activity?.getSharedPreferences(
            SHARED_PREFS_NAME,
            AppCompatActivity.MODE_PRIVATE
        )
        val editor = sharedPreference?.edit()
        editor?.putBoolean("first_time_launch", false)?.commit()
        Log.e("TAG", "SETUP " + sharedPreference?.getBoolean("first_time_launch", true).toString())


    }
}