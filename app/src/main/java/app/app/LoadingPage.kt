package app.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LoadingPage: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_page)

        // Delay for 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Start MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            // Finish SplashActivity so it cannot be returned to
            finish()
        }, 3000) // 3000 milliseconds delay
    }
}