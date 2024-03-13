package app.app;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

// Simple data class to hold token data
data class TokensData(val accessToken: String, val refreshToken: String, val expiresIn: Int)

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var authorizeButton: Button
    private lateinit var authorizationStatusText: TextView

    private val CLIENT_ID = "23RT82"
    private val REDIRECT_URI = "seniorhealthmonitoringapplication2024pvp://callbackdata"
    private val CLIENT_SECRET = "7ae655202ef53382ee2f5a95e3622623"
    private val SCOPES = "activity heartrate sleep profile"
    private val FITBIT_TOKEN_ENDPOINT = "https://api.fitbit.com/oauth2/token"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Assuming you have a layout

        authorizeButton = findViewById(R.id.authorize_button)
        authorizationStatusText = findViewById(R.id.authorization_status)

        authorizeButton.setOnClickListener {
            startAuthorization()
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

//    private fun generateCodeVerifier(): String {
//        val secureRandom = SecureRandom()
//        val randomBytes = ByteArray(32) // Generates a 32-byte (256-bit) random value
//        secureRandom.nextBytes(randomBytes)
//        return Base64.encodeToString(randomBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
//            .substring(0, 128) // Enforce max 128 char length
//    }
//
//    private fun generateCodeChallenge(codeVerifier: String): String {
//        val digest = MessageDigest.getInstance("SHA-256")
//        val hashBytes = digest.digest(codeVerifier.toByteArray(Charsets.UTF_8))
//        return Base64.encodeToString(hashBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
//    }
//
//    private fun storeCodeVerifier(state: String, codeVerifier: String) {
//        getSharedPreferences("secret_prefs", Context.MODE_PRIVATE).edit()
//            .putString("code_verifier_$state", codeVerifier)
//            .apply()
//    }

    private fun retrieveCodeVerifier(state: String): String? {
        val sharedPreferences = getSharedPreferences("secret_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("code_verifier_$state", null)
    }


    private fun exchangeCodeForToken(code: String) {
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .add("code", code)
            .add("grant_type", "authorization_code")
            // Note: No code_verifier needed anymore
            .build()

        val request = Request.Builder()
            .url(FITBIT_TOKEN_ENDPOINT)
            .post(formBody)
            .header("Authorization", "Basic " + Base64.encodeToString("$CLIENT_ID:$CLIENT_SECRET".toByteArray(), Base64.NO_WRAP))
            .header("Content-Type", "application/x-www-form-urlencoded")
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
                    val responseData = response.body?.string() ?: ""
                    val tokensData = extractTokens(responseData)

                    if (tokensData != null) {
                        storeTokens(tokensData)
                        runOnUiThread {
                            val tokenInfo = "Access Token: ${tokensData.accessToken}\n" +
                                    "Refresh Token: ${tokensData.refreshToken}\n" +
                                    "Expires In: ${tokensData.expiresIn}"
                            authorizationStatusText.text = tokenInfo
                        }
                    } else {
                        runOnUiThread {
                            authorizationStatusText.text = "Authorization Failed (Invalid Token Response)"
                        }
                    }
                } else {
                    // Handle error response from Fitbit using response.code()
                    runOnUiThread {
                        authorizationStatusText.text = "Authorization failed from Fitbit"
                        Toast.makeText(this@MainActivity, "Authorization failed", Toast.LENGTH_SHORT).show()
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
        // TODO: Implement secure storage of tokensData (access token, refresh token, expires in) using Android Keystore
    }




    // Example function to make an API call using the access token
//    private fun fetchUserProfile() {
//        val accessToken = getAccessToken()
//        if (accessToken != null) {
//            val client = OkHttpClient()
//            val request = Request.Builder()
//                .url("https://api.fitbit.com/1/user/-/profile.json")
//                .header("Authorization", "Bearer $accessToken")
//                .build()
//
//            client.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    // Handle Network Errors (e.g., display error message to the user)
//                    runOnUiThread {
//                        Toast.makeText(this@MainActivity, "Network Error", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val responseData = response.body?.string()
//                    if (responseData != null) {
//                        val profileData = JSONObject(responseData)
//                        // Display profile data in the UI
//                        runOnUiThread { updateUI(profileData) }
//                    } else {
//                        // Handle unexpected response
//                        runOnUiThread {
//                            Toast.makeText(this@MainActivity, "Unexpected Response", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            })
//        } else {
//            // Handle case where no access token is stored
//            Toast.makeText(this, "No access token found", Toast.LENGTH_SHORT).show()
//        }
//    }
}