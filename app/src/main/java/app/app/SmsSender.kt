package app.app

import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

//This class is used to sent sms messages from GUI

class SmsSender : AppCompatActivity() {

    // on below line we are creating variable
    // for edit text phone and message and button
    lateinit var phoneEdt: EditText
    lateinit var messageEdt: EditText
    lateinit var sendMsgBtn: Button
    lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_temp)

        // initializing variables of phone edt,
        // message edt and send message btn.
        phoneEdt = findViewById(R.id.idEdtPhone)
        messageEdt = findViewById(R.id.idEdtMessage)
        sendMsgBtn = findViewById(R.id.idBtnSendMessage)
        backButton = findViewById(R.id.backButton)



        // adding on click listener for send message button.
        sendMsgBtn.setOnClickListener {

            // on below line we are creating two
            // variables for phone and message
            val phoneNumber = phoneEdt.text.toString()
            val message = messageEdt.text.toString()

            // on the below line we are creating a try and catch block
            try {

                // on below line we are initializing sms manager.
                //as after android 10 the getDefault function no longer works
                //so we have to check that if our android version is greater
                //than or equal toandroid version 6.0 i.e SDK 23
                val smsManager:SmsManager = this.getSystemService(SmsManager::class.java)

                // on below line we are sending text message.
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)

                // on below line we are displaying a toast message for message send,
                Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {

                // on catch block we are displaying toast message for error.
                Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }
        backButton.setOnClickListener {
            finish()
        }
    }
}
