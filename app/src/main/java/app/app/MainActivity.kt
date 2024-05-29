package app.app
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.FormBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
//import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.app.heartrate.HealthInfoActivity
import app.app.steps.DailyStepsActivity
import org.json.JSONException
//import androidx.recyclerview.widget.RecyclerView
//import com.google.gson.Gson
//import com.google.gson.JsonObject
//import java.util.concurrent.Executors
//import java.util.concurrent.TimeUnit

data class TokensData(val accessToken: String, val refreshToken: String, val expiresIn: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var authorizeButton: Button
    private lateinit var authorizationStatusText: TextView
    private lateinit var goToData: Button
    private lateinit var smsSender_button: Button
    private lateinit var settingsButton: Button
    private lateinit var stepsButton: Button
    private lateinit var dbUpdateButton: Button
    private lateinit var dbHelper: DBHelper

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
        authorizationStatusText = findViewById(R.id.authorization_status)
        goToData = findViewById(R.id.goToDataInfoButton)
        smsSender_button = findViewById(R.id.sms_sender_page_button)
        settingsButton = findViewById(R.id.settingsButton)
        stepsButton = findViewById(R.id.stepsButton)
        dbUpdateButton = findViewById(R.id.updateDBButton)

        authorizeButton.setOnClickListener {
            startAuthorization()
        }

        stepsButton.setOnClickListener {
            val intent = Intent(this, DailyStepsActivity::class.java).putExtra("accesstoken", ACCESSTOKEN)
            startActivity(intent)
        }

        goToData.setOnClickListener{
            val intent = Intent(this, HealthInfoActivity::class.java).putExtra("accesstoken", ACCESSTOKEN)

            startActivity(intent)
        }
        smsSender_button.setOnClickListener{
            val intent = Intent(this, SmsSender::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsPage::class.java)
            startActivity(intent)
        }

        dbUpdateButton.setOnClickListener {
            testDb()
        }

        if (!PermissionsManager(this).checkPermissions()){
            PermissionsManager(this).requestPermissions(this)
        }

        handleCallbackIntent(getIntent())

        //db test cases
        dbHelper = DBHelper(this)


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleCallbackIntent(intent!!)
    }

    private fun testDb(){

        //code to update and refresh db after changes
        //val db = dbHelper.writableDatabase
        //val ver = db.getVersion()
        //val verUpgrade = ver + 1
        //dbHelper.onUpgrade(db, ver, verUpgrade)

        if(dbHelper.getUser(1)==null){
            val intent = Intent(this, DataInputPage::class.java)
            startActivity(intent)
        }
        else{
            println("User exists")
        }

        if(dbHelper.getThreshold(1)==null){
            val intent = Intent(this, ThresholdInputPage::class.java)
            startActivity(intent)
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
//                            val tokenInfo = "Access Token: ${tokensData.accessToken}\n" +
//                                    "Refresh Token: ${tokensData.refreshToken}\n" +
//                                    "Expires In: ${tokensData.expiresIn}"
                            authorizationStatusText.text = "Authorization successful!"
                            ACCESSTOKEN = tokensData.accessToken
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
    // TODO: Implement secure storage of tokensData (access token, refresh token, expires in) using encryption to database
//    private fun storeTokens(tokensData: TokensData) {
//
//    }
}
