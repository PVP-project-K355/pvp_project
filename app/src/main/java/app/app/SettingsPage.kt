package app.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SettingsPage: AppCompatActivity() {

    private lateinit var goToMain: Button
    private lateinit var goToThreshold: Button
    private lateinit var goToData: Button
    private lateinit var changeAcc: Button
    private lateinit var changeSub: Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)

        goToMain = findViewById(R.id.maingpage_button)
        goToThreshold = findViewById(R.id.goToThreshold)
        goToData = findViewById(R.id.goToData)
        changeAcc = findViewById(R.id.changeAcc)
        changeSub = findViewById(R.id.changeSub)

        goToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        goToThreshold.setOnClickListener {
            val intent = Intent(this, ThresholdInputPage::class.java)
            startActivity(intent)
        }

        goToData.setOnClickListener {
            val intent = Intent(this, DataInputPage::class.java)
            startActivity(intent)
        }

        changeAcc.setOnClickListener {

        }

        changeSub.setOnClickListener {

        }

    }
}