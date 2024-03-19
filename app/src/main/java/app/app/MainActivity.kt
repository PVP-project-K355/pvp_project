package app.app;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.FormBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
// Simple data class to represent a heart rate entry
data class HeartRateData(val dateTime: String, val value: Int)
private val heartRateList = ArrayList<Int>()
//private lateinit var heartRateAdapter: HeartRateAdapter // Assuming you'll use an adapter
private lateinit var recyclerView: RecyclerView

// Simple data class to hold token data
data class TokensData(val accessToken: String, val refreshToken: String, val expiresIn: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var authorizeButton: Button
    private lateinit var heartrate_button: Button
    private lateinit var authorizationStatusText: TextView

    private val CLIENT_ID = "23RTB5"
    private val REDIRECT_URI = "seniorhealthmonitoringapplication2024pvp://callbackdata"
    private val CLIENT_SECRET = "f448e71dc034bd7466a0b5719a03e8cf"
    private val SCOPES = "activity heartrate sleep profile"
    private val FITBIT_TOKEN_ENDPOINT = "https://api.fitbit.com/oauth2/token"
    private var ACCESSTOKEN = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Assuming you have a layout
        //recyclerView = findViewById(R.id.recycler_view)
        //heartRateAdapter = HeartRateAdapter(heartRateList)
        //recyclerView.adapter = heartRateAdapter

        authorizeButton = findViewById(R.id.authorize_button)
        heartrate_button = findViewById(R.id.get_heart_rate_button)
        authorizationStatusText = findViewById(R.id.authorization_status)

        authorizeButton.setOnClickListener {
            startAuthorization()
        }
        heartrate_button.setOnClickListener {
            fetchHeartRateData(ACCESSTOKEN)
        }

        handleCallbackIntent(getIntent());
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleCallbackIntent(intent!!)
    }

    private fun startAuthorization() {


        val authUrl = "https://www.fitbit.com/oauth2/authorize?" +
                "response_type=code" +
                "&client_id=$CLIENT_ID" +
                "&redirect_uri=$REDIRECT_URI" +
                "&scope=$SCOPES" +
                "&expires_in=604800" // Optional: Sets the access token expiry time in seconds
        Log.d("FitbitAuth", "Authorization URL: $authUrl")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)


    }

    private fun handleCallbackIntent(intent: Intent) {
        Log.d("FitbitCallback", "Action: ${intent.action}")
        Log.d("FitbitCallback", "Data URI: ${intent.data}")
        Log.d("FitbitCallback", "All Extras: ${intent.extras}")
        val dataUri = intent.data // The deep link URI
        if (dataUri != null) {
            //val code = dataUri.getQueryParameter("code") // May still be accessible via query parameters
            val code = intent.data?.getQueryParameter("code")
            Log.d("Code:", "String: ${code}")
            val state = dataUri.getQueryParameter("state")

            if (code != null) {

                authorizationStatusText.text = "Exchanging code for tokens..."
                exchangeCodeForToken(code)

            } else {
                // Handle missing code or state
                authorizationStatusText.text = "Authorization Failed (Missing Code or State)"
                Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun exchangeCodeForToken(code: String) {
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("code", code)
            .add("grant_type", "authorization_code")
            .add("redirect_uri", REDIRECT_URI) // Add the redirect_uri parameter
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
                runOnUiThread {
                    authorizationStatusText.text = "Authorization Failed (Network Error)"
                    Toast.makeText(this@MainActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code
                val responseBodyString = response.body?.string() ?: "" // Handle potential null body
                Log.d("FitbitDebug", "Response Code: $responseCode")
                Log.d("FitbitDebug", "Response Body: $responseBodyString")
                if (response.isSuccessful) {
                    val tokensData = extractTokens(responseBodyString)

                    if (tokensData != null) {
                        //storeTokens(tokensData) TODO: store tokens data in database
                        // Start updates when needed (e.g., after login)
                        //startScheduledHeartRateUpdates(tokensData.accessToken)
                        runOnUiThread {
                            val tokenInfo = "Access Token: ${tokensData.accessToken}\n" +
                                    "Refresh Token: ${tokensData.refreshToken}\n" +
                                    "Expires In: ${tokensData.expiresIn}"
                            authorizationStatusText.text = tokenInfo
                            ACCESSTOKEN = tokensData.accessToken;
                            Log.d("Access token ", "Access token: ${tokensData.accessToken}")
                            Log.d("Refresh token", "Refresh token: ${tokensData.refreshToken}")
                        }
                    } else {
                        runOnUiThread {
                            authorizationStatusText.text =
                                "Authorization Failed (Invalid Token Response)"
                        }
                    }
                } else {
                    // Handle error response from Fitbit using response.code()
                    runOnUiThread {
                        authorizationStatusText.text = "Authorization failed from Fitbit"
                        Toast.makeText(
                            this@MainActivity,
                            "Authorization failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }


    private fun extractTokens(responseData: String): TokensData? {
        try {
            val jsonObject = JSONObject(responseData)
            val accessToken = jsonObject.getString("access_token")
            val refreshToken = jsonObject.getString("refresh_token")
            val expiresIn = jsonObject.getInt("expires_in")
            return TokensData(accessToken, refreshToken, expiresIn)
        } catch (e: JSONException) {
            // Handle JSON parsing errors
            return null
        }
    }

    private fun storeTokens(tokensData: TokensData) {
        // TODO: Implement secure storage of tokensData (access token, refresh token, expires in) using encryption to database
    }

    fun parseHeartRateData(responseBodyString: String): Int {
        val gson = Gson()

        // 1. ADJUST the path based on the Fitbit API response structure
        val heartRateValues = gson.fromJson(responseBodyString, JsonObject::class.java)
            .getAsJsonObject("activities-heart-intraday")
            .getAsJsonArray("dataset")

        // 2. EXTRACT the relevant heart rate value (replace placeholder)
        val currentHeartRate = heartRateValues.last() // Assuming the last entry is the most recent
            .asJsonObject
            .get("value")
            .asInt

        return currentHeartRate
    }

    // Example function to make an API call using the access token
    private fun fetchHeartRateData(accessToken: String) {
        val client = OkHttpClient()

        // Specify the endpoint to retrieve heart rate data
        val endpoint = "https://api.fitbit.com/1/user/-/activities/heart/date/today/1d/1sec.json"

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.d("FitbitHeartRateNetwork", "Response: network error")
                    // Handle network errors
                    Toast.makeText(this@MainActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBodyString = response.body?.string() ?: "" // Handle potential null body
                Log.d("FitbitHeartRate", "Response Body: $responseBodyString")
                if (response.isSuccessful) {

                    val heartRate = parseHeartRateData(responseBodyString) // Implement your parsing
                    heartRateList.add(heartRate)

//                    // Update UI on the main thread
//                    Handler(Looper.getMainLooper()).post {
//                        heartRateAdapter.notifyDataSetChanged() // Update your RecyclerView
//                    }
                    Log.d("FitbitHeartRateDATA", "Response: succesfull")
                } else {
                    // Handle error response from Fitbit using response.code()
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error retrieving heart rate data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    // Call repeatedly to fetch data
    private fun startScheduledHeartRateUpdates(accessToken: String) {
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.scheduleAtFixedRate(
            { fetchHeartRateData(accessToken) },
            0, // Initial delay
            30, // Delay between updates in seconds
            TimeUnit.SECONDS
        )
    }
}
