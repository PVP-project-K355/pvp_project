package app.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

private var FITBIT_AUTH_TOKEN = false
private val CLIENT_ID = "23RTB5"
private val REDIRECT_URI = "seniorhealthmonitoringapplication2024pvp://callbackdata"
private val SCOPES = "activity heartrate sleep profile"

//data class TokensData(val accessToken: String, val refreshToken: String, val expiresIn: Int)

class LoginWatchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_watch, container, false)

        view.findViewById<Button>(R.id.button_fitbit).setOnClickListener {
            startAuthorization()
        }

        view.findViewById<Button>(R.id.button_next_setup).setOnClickListener {
            if (FITBIT_AUTH_TOKEN) {
                NavHostFragment.findNavController(this@LoginWatchFragment).navigate(R.id.action_loginWatch_to_dataInputPersonal)
            } else {
                Toast.makeText(requireContext(), "Please login with Fitbit first", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.button_back_setup).setOnClickListener {
            NavHostFragment.findNavController(this@LoginWatchFragment).popBackStack()
        }

        return view
    }

    private fun startAuthorization() {
        val authUrl = "https://www.fitbit.com/oauth2/authorize?" +
                "response_type=code" +
                "&client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&scope=$SCOPES" +
                "&expires_in=604800"
        Log.d("FitbitAuth", "Authorization URL: $authUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
        FITBIT_AUTH_TOKEN = true

    }
}

