package app.app

import app.app.LoginWatchFragment
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.log

const val SHARED_PREFS_NAME = "MyAppPrefs"
private var launched = false

private var FITBIT_AUTH_TOKEN = false
private val CLIENT_ID = "23RTB5"
private val REDIRECT_URI = "seniorhealthmonitoringapplication2024pvp://callbackdata"
private val CLIENT_SECRET = "f448e71dc034bd7466a0b5719a03e8cf"
private val SCOPES = "activity heartrate sleep profile"
private val FITBIT_TOKEN_ENDPOINT = "https://api.fitbit.com/oauth2/token"
private var ACCESSTOKEN = ""

class MainActivity1 : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch screen
        installSplashScreen()

        val sharedPreference = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreference.edit()

        // Forcing first-time launch for testing
        if (!launched) {
            editor.clear().commit()
            launched = true
        }

        Log.e("TEST", sharedPreference.getBoolean("first_time_launch", true).toString())

        if (sharedPreference.getBoolean("first_time_launch", true))
        {
            Log.e("TEST", "SETUP LAUNCH")
            setContentView(R.layout.activity_setup)
        }
        else
        {
            Log.e("TEST", "MAIN LAUNCH")
            setContentView(R.layout.activity_main1)
        }

        handleCallbackIntent(getIntent())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleCallbackIntent(intent!!)
    }

    private fun handleCallbackIntent(intent: Intent?) {
        if (intent != null) {
            Log.d("FitbitCallback", "Action: ${intent.action}")
            Log.d("FitbitCallback", "Data URI: ${intent.data}")
            Log.d("FitbitCallback", "All Extras: ${intent.extras}")

            // Check if the intent action or data requires handling
                // Perform your intent handling logic here
                val dataUri = intent.data // The deep link URI
                if (dataUri != null) {
                    val code = dataUri.getQueryParameter("code")
                    Log.d("Code:", "String: $code")

                    if (code != null) {
                        // Process the code if needed
                        exchangeCodeForToken(code)
                    } else {
                        // Handle missing code
                        Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun exchangeCodeForToken(code: String) {
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("code", code)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", REDIRECT_URI)
            .build()

        val credentials = "$CLIENT_ID:$CLIENT_SECRET"
        val encodedCredentials = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        val request = Request.Builder()
            .url(FITBIT_TOKEN_ENDPOINT)
            .post(formBody)
            .addHeader("Authorization", "Basic $encodedCredentials")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("NetworkError", "Network Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code
                val responseBodyString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val tokensData = extractTokens(responseBodyString)
                    if (tokensData != null) {
                        ACCESSTOKEN = tokensData.accessToken
                        val expiresIn = tokensData.expiresIn

                        val dbHelper = DBHelper(this@MainActivity1) // Initialize DBHelper with context
                        val id = dbHelper.saveAccessToken(ACCESSTOKEN, expiresIn)

                        if (id != -1L) {
                            Log.d("DB", "Access token saved successfully with ID: $id")
                        } else {
                            Log.e("DB", "Failed to save access token to database")
                            // Handle failure to save token
                        }
                    } else {
                        Log.e("JSONParsingError", "Failed to parse JSON response")
                    }
                } else {
                    Log.e("NetworkError", "Network Error: $responseCode - $responseBodyString")
                }
            }
        })
    }

    private fun extractTokens(responseData: String): TokensData? {
        return try {
            val jsonObject = JSONObject(responseData)
            val accessToken = jsonObject.getString("access_token")
            val refreshToken = jsonObject.getString("refresh_token")
            val expiresIn = jsonObject.getInt("expires_in")
            TokensData(accessToken, refreshToken, expiresIn)
        } catch (e: JSONException) {
            null
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.fragmentContainerView)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
