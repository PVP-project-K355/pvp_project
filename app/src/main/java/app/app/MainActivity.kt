package app.app;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.FormBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;
import okhttp3.*
import org.json.JSONObject
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// Data Structures
private val heartRateList = mutableListOf<Int>() // Mutable list for storing heart rate data
private lateinit var heartRateTextView: TextView // Declare your TextView

private lateinit var recyclerView: RecyclerView


data class TokensData(val accessToken: String, val refreshToken: String, val expiresIn: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var authorizeButton: Button
    private lateinit var heartrate_button: Button
    private lateinit var authorizationStatusText: TextView
    private lateinit var goToData: Button
    private lateinit var smsSender_button: Button

    private val CLIENT_ID = "23RTB5"
    private val REDIRECT_URI = "seniorhealthmonitoringapplication2024pvp://callbackdata"
    private val CLIENT_SECRET = "f448e71dc034bd7466a0b5719a03e8cf"
    private val SCOPES = "activity heartrate sleep profile"
    private val FITBIT_TOKEN_ENDPOINT = "https://api.fitbit.com/oauth2/token"
    private var ACCESSTOKEN = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) 
        authorizeButton = findViewById(R.id.authorize_button)
        heartrate_button = findViewById(R.id.get_heart_rate_button)
        authorizationStatusText = findViewById(R.id.authorization_status)
        heartRateTextView = findViewById(R.id.hearRate)
        goToData = findViewById(R.id.goToDataInfoButton)
        smsSender_button = findViewById(R.id.sms_sender_page_button)

        authorizeButton.setOnClickListener {
            startAuthorization()
        }
        heartrate_button.setOnClickListener {
            fetchHeartRateData(ACCESSTOKEN)
        }
        goToData.setOnClickListener{
            val intent = Intent(this, HealthInfoActivity::class.java)
            startActivity(intent)
        }
        smsSender_button.setOnClickListener{
            val intent = Intent(this, SmsSender::class.java)
            startActivity(intent)
        }

        if (!PermissionsManager(this).checkPermissions()){
            PermissionsManager(this).requestPermissions(this)
        }

        handleCallbackIntent(getIntent());

        //db testing cases
        val dbHelper = DBHelper(this)

        if(dbHelper.getUser(1)==null){
            //Example user data
            val newUser = User(
                name = "John",
                surname = "Doe",
                height = 175.0,
                weight = 80.0,
                birthdate = "1900-01-01" // Assuming birthdate is in "YYYY-MM-DD" format
            )
            //Inserting user data into the database
            //dbHelper.addUser(newUser)
        }
        else{
            println("User exists")
        }

        if(dbHelper.getThreshold(1)==null){
            //Example threshold data
            val newThreshold = Threshold(
                minRate = 60,
                maxRate = 100
            )
            //Inserting threshold data into the database
            //dbHelper.addThreshold(newThreshold)
        }
        else{
            println("Threshold exists")
        }

        if(dbHelper.getApi(1)==null){
            //Example API data
            val newApi = API(
                clientId = "Petras",
                clientSecret = "petraspetras",
                accessToken = "petrasdu",
                refreshToken = "petrastrys",
                expiresIn = 365
            )
            //Inserting api data into database
            //dbHelper.addApi(newApi)
        }
        else{
            println("API exists")
        }

        if(dbHelper.getContact(1)==null){
            //Example contact data
            val newContact = Contact(
                name = "John",
                surname = "Doe",
                phoneNumber = "+3706"
            )
            //Inserting contact data into the database
            //dbHelper.addContact(newContact)
        }
        else{
            println("Contact exists")
        }

        // Example user data
        val updateUser = User(
            id = 1,
            name = "Petras",
            surname = "Doe",
            height = 175.0,
            weight = 80.0,
            birthdate = "1999-01-01" // Assuming birthdate is in "YYYY-MM-DD" format
        )
        //Updating user data
        //dbHelper.updateUser(updateUser)

        //Example API data
        val updateAPI = API(
            id = 1,
            clientId = "Petras",
            clientSecret = "petraspetras",
            accessToken = "petrasdu",
            refreshToken = "petraspenki",
            expiresIn = 300
        )
        //Updating api data
        //dbHelper.updateApi(updateAPI)

        //Example heart rate data
        val newRate = HeartRate(
            rate = 65,
            time = "2024-03-20 22:02"
        )
        // Inserting heart rate into the database and getting inserted heart rate id
        //val insertedRateId = dbHelper.addHeartRate(newRate)

        //Checking if inserted heart rate is between thresholds
        //CheckData(this).checkRate(insertedRateId.toInt(), 1)

        /*val apiID = 1 // Replace with the ID of the user you want to retrieve
        val api = dbHelper.getApi(apiID)

        if (api != null) {
            println("API found:")
            println("ID: ${api.id}")
            println("Client: ${api.clientId}")
            println("Secret: ${api.clientSecret}")
            println("Access token: ${api.accessToken}")
            println("Refresh token: ${api.refreshToken}")
            println("Expires in: ${api.expiresIn}")
        } else {
            println("API not found.")
        }*/

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

    private fun parseHeartRateData(responseBodyString: String): Int {
        val gson = Gson()

        try {
            val heartRateValues = gson.fromJson(responseBodyString, JsonObject::class.java)
                ?.getAsJsonObject("activities-heart")
                ?.getAsJsonArray("value") // Get the 'value' array
                ?.get(0)?.asJsonObject // Get the first element in the 'value' array
                ?.getAsJsonArray("heartRateZones")

            val currentHeartRate = heartRateValues?.last()?.asJsonObject?.get("min")?.asInt ?: 0

            return currentHeartRate
        } catch (e: Exception) {
            Log.e("FitbitHeartRateParsing", "Error parsing he art rate data", e)
            return 0 // Return a default value on error
        }
    }
    private fun updateHeartRateDisplay() {
        val heartRateDisplayText = heartRateList.joinToString(separator = "\n") // Format as you like
        runOnUiThread {
            heartRateTextView.text = heartRateDisplayText
        }
    }

    private var lastHeartRateResponse: String = ""

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
                try {
                    if (response.isSuccessful && response.body != null) {
                        val responseBodyString = response.body!!.string()
                        lastHeartRateResponse = responseBodyString // Save the response
                        heartRateTextView.text = responseBodyString
                        val heartRate = parseHeartRateData(responseBodyString)
                        //heartRateList.add(lastHeartRateResponse) //testinis
                        //heartRateList.add(heartRate) // Add the heart rate to the list
                        //updateHeartRateDisplay() // Update the TextView
                        Log.d("FitbitHeartRateDATA", "Response: $responseBodyString")
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
                } catch (e: Exception) {
                    Log.e("FetchHeartRateData", "Error handling response: ${e.message}")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Error handling response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } finally {
                    response.body?.close() // Ensure response body is closed after reading
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
