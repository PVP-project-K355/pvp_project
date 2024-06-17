package app.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

const val SHARED_PREFS_NAME = "MyAppPrefs"
private var launched = false

class MainActivity1 : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Launch screen
        installSplashScreen()

        val sharedPreference = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreference.edit()

        //Forcing first time launch for testing
        if(!launched) {
            editor.remove("first_time_launch").commit()
            launched = true
        }

        if(sharedPreference.getBoolean("first_time_launch", true))
        {
            setContentView(R.layout.activity_setup)
        }
        else
        {
            setContentView(R.layout.activity_main1)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.my_nav)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
