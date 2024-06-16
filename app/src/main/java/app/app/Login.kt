package app.app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

private var GOOGLE_AUTH_TOKEN = false

class Login : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonNext = view.findViewById<Button>(R.id.button_next_setup)
        if(GOOGLE_AUTH_TOKEN)
            buttonNext.setBackgroundResource(R.drawable.button_blue_soft)
        else
            buttonNext.setBackgroundResource(R.drawable.button_white)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        view.findViewById<Button>(R.id.button_google).setOnClickListener{loginWithGoogle(view)}
        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener{next(view)}
        return view
    }

    private fun loginWithGoogle(view: View)
    {
        GOOGLE_AUTH_TOKEN = true
        view.findViewById<Button>(R.id.button_next_setup).setBackgroundResource(R.drawable.button_blue_soft)
    }

    private fun next(view: View)
    {
        if(GOOGLE_AUTH_TOKEN)
        {
            view.findNavController().navigate(R.id.action_login_to_loginWatch)
        }
    }
}