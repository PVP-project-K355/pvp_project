package app.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

private var FITBIT_AUTH_TOKEN = false

class LoginWatch : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonNext = view.findViewById<Button>(R.id.button_next_setup)
        if(FITBIT_AUTH_TOKEN)
            buttonNext.setBackgroundResource(R.drawable.button_blue_soft)
        else
            buttonNext.setBackgroundResource(R.drawable.button_white)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_watch, container, false)
        view.findViewById<Button>(R.id.button_fitbit).setOnClickListener{ loginWithFitbit(view) }
        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener{ next(view) }
        view.findViewById<Button>(R.id.button_back_setup).setOnClickListener {
            NavHostFragment.findNavController(this@LoginWatch).popBackStack()
        }
        return view
    }

    private fun loginWithFitbit(view: View)
    {
        FITBIT_AUTH_TOKEN = true
        view.findViewById<Button>(R.id.button_next_setup).setBackgroundResource(R.drawable.button_blue_soft)
    }

    private fun next(view: View)
    {
        if(FITBIT_AUTH_TOKEN)
        {
            view.findNavController().navigate(R.id.action_loginWatch_to_dataInputPersonal)
        }
    }

}