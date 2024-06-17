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

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.credentials.*
import androidx.credentials.exceptions.*
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.*
import kotlinx.coroutines.*
import java.io.File
import com.auth0.android.jwt.JWT

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
        val activityContext = requireContext()
        val credentialManager = CredentialManager.create(activityContext)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("753676471034-6fc0j3lqc4k1h62h8vg1vaqi2m9cekhj.apps.googleusercontent.com")
            .build()

        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder("753676471034-6fc0j3lqc4k1h62h8vg1vaqi2m9cekhj.apps.googleusercontent.com")
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext,
                )
                handleSignIn(view, result)
            } catch (e: GetCredentialException) {
                //error
                if (e.type == "android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL") {
                    Log.e("LOGIN_ERROR", e.message.toString())
                    Toast.makeText(activityContext, "Please add a Google account to your device.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(Settings.ACTION_ADD_ACCOUNT))
                } else {
                    Log.e("LOGIN_ERROR", e.message.toString())
                    Toast.makeText(activityContext, "Error: " + e.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun handleSignIn(view: View, result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        val credential = result.credential
        val TAG = "CREDENTIAL_ERROR"
        when (credential) {
            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Credential info object
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        val subValue = decodeJWTAndGetSub(googleIdTokenCredential.idToken)
                        Log.e("LOGIN_SUCCESS", "ID: $subValue")
                        Toast.makeText(requireContext(), "Login success!", Toast.LENGTH_LONG).show()
                        GOOGLE_AUTH_TOKEN = true
                        view.findViewById<Button>(R.id.button_next_setup).setBackgroundResource(R.drawable.button_blue_soft)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun decodeJWTAndGetSub(token: String): String? {
        return try {
            val jwt = JWT(token)
            val sub = jwt.subject // The "sub" value is usually mapped to the subject field
            sub
        } catch (e: Exception) {
            Log.e("Login", "Invalid token: ${e.message}")
            null
        }
    }

    private fun next(view: View)
    {
        if(GOOGLE_AUTH_TOKEN)
        {
            view.findNavController().navigate(R.id.action_login_to_loginWatch)
        }
    }
}