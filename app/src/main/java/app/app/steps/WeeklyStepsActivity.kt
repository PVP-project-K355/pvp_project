package app.app.steps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import app.app.MainActivity
import app.app.R

class WeeklyStepsActivity : AppCompatActivity() {

    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_steps)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val dailyButton: Button = findViewById(R.id.dailyButton)
        dailyButton.setOnClickListener {
            startActivity(Intent(this, DailyStepsActivity::class.java))
        }

        // Implement weekly data fetching and graph updating logic here
    }
}
