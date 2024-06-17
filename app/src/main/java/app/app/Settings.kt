package app.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [Fragment] subclass.
 * Use the [Settings.newInstance] factory method to
 * create an instance of this fragment.
 */
class Settings : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var mToast: Toast? = null
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val name = dbHelper.getLogin(1)?.name
        val surname = dbHelper.getLogin(1)?.surname
        val uri = dbHelper.getLogin(1)?.uri?.toUri()
        val fullname = "$name $surname"

        Glide.with(this)
            .load(uri)
            .into(view.findViewById(R.id.profile_picture))

        view.findViewById<TextView>(R.id.profile_name).text = fullname

        view.findViewById<Button>(R.id.maingpage_button).setOnClickListener { NavHostFragment.findNavController(this@Settings).popBackStack(R.id.mainpage, false) }

        view.findViewById<Button>(R.id.goToThreshold).setOnClickListener {
            NavHostFragment.findNavController(this@Settings).navigate(R.id.settingsThresholdPage)
        }

        view.findViewById<Button>(R.id.goToData).setOnClickListener {
            NavHostFragment.findNavController(this@Settings).navigate(R.id.Edit_User_Data_Page)
        }

        view.findViewById<Button>(R.id.changeAcc).setOnClickListener {
//          NavHostFragment.findNavController(this@Settings).navigate(R.id.Edit_User_Data_Page)
            loginWithGoogle(view)
        }

        view.findViewById<Button>(R.id.changeSub).setOnClickListener {
            NavHostFragment.findNavController(this@Settings).navigate(R.id.SubscriptionPage)
        }
        return view
    }

    private fun loginWithGoogle(view: View)
    {
        val activityContext = requireContext()
        val credentialManager =  CredentialManager.create(activityContext)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("753676471034-6fc0j3lqc4k1h62h8vg1vaqi2m9cekhj.apps.googleusercontent.com")
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
                    mToast?.cancel()
                    mToast = Toast.makeText(activityContext, "Please add a Google account to your device.", Toast.LENGTH_LONG)
                    mToast?.show()
                    startActivity(Intent(android.provider.Settings.ACTION_ADD_ACCOUNT))
                } else {
                    Log.e("LOGIN_ERROR", e.message.toString())
                    mToast?.cancel()
                    mToast = Toast.makeText(activityContext, "Error: " + e.message.toString(), Toast.LENGTH_LONG)
                    mToast?.show()
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
                        // add to database
                        val idtoken = googleIdTokenCredential.idToken
                        val usertoken = decodeJWTAndGetSub(googleIdTokenCredential.idToken).toString()
                        val name = googleIdTokenCredential.givenName.toString()
                        val surname = googleIdTokenCredential.familyName.toString()
                        val email = googleIdTokenCredential.id
                        val uri = googleIdTokenCredential.profilePictureUri.toString()
                        val status = 1
                        Log.e("LOGIN_SUCCESS", "User token: $usertoken")
                        Log.e("LOGIN_SUCCESS", "ID Token: $idtoken")
                        dbHelper.addLogin(LoginData(0, usertoken, name, surname, email, uri, status))

                        val fullname = "$name $surname"
                        Glide.with(this)
                            .load(uri)
                            .into(view.findViewById(R.id.profile_picture))
                        view.findViewById<TextView>(R.id.profile_name).text = fullname

                        mToast?.cancel()
                        mToast = Toast.makeText(requireContext(), "Account changed!", Toast.LENGTH_LONG)
                        mToast?.show()
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                        mToast?.cancel()
                        mToast = Toast.makeText(requireContext(), "Error: invalid token response", Toast.LENGTH_LONG)
                        mToast?.show()
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                    mToast?.cancel()
                    mToast = Toast.makeText(requireContext(), "Error: unexpected credential", Toast.LENGTH_LONG)
                    mToast?.show()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Settings().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}