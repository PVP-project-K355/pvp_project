package app.app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class Mainpage : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mainpage, container, false)

        view.findViewById<CardView>(R.id.nav_button_pulse).setOnClickListener{view.findNavController().navigate(R.id.action_mainpage_to_pulse)}
        view.findViewById<CardView>(R.id.nav_button_steps).setOnClickListener{view.findNavController().navigate(R.id.action_mainpage_to_steps)}
        view.findViewById<CardView>(R.id.nav_button_sleep).setOnClickListener{view.findNavController().navigate(R.id.action_mainpage_to_sleep)}
        view.findViewById<CardView>(R.id.nav_button_contacts).setOnClickListener{view.findNavController().navigate(R.id.action_mainpage_to_contacts)}
        view.findViewById<CardView>(R.id.nav_button_settings).setOnClickListener{view.findNavController().navigate(R.id.action_mainpage_to_settings)}
        view.findViewById<CardView>(R.id.nav_button_reports).setOnClickListener{view.findNavController().navigate(R.id.action_mainpage_to_report)}

        return view
    }
}