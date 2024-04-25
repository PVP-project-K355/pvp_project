package app.app

import android.telephony.SmsManager

//This class is used to send sms messages

class SmsManager {

    fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            println("SMS sent successfully!")
        } catch (ex: Exception) {
            println("Failed to send SMS: ${ex.message}")
        }
    }
}