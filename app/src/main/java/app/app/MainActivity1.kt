package app.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.findNavController

class MainActivity1 : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen() //Launch screen
        setContentView(R.layout.activity_main1)
    }

    override fun onSupportNavigateUp(): Boolean {
        navController = findNavController(R.id.my_nav)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
