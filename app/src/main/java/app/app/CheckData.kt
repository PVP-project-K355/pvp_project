package app.app

import android.content.Context

/*This class is used to check if data is between thresholds and if it is outside thresholds
sends automatic emergency sms messages for added contacts using SmsManager class*/

class CheckData(private val context: Context) {

    private val dbHelper = DBHelper(context)

    private val name = dbHelper.getUser(1)?.name

    //Checking heart rate data
    fun checkRate(rateId: Int, thresholdId: Int){
        if(dbHelper.getSingleRate(id=rateId) != null && dbHelper.getThreshold(id=thresholdId) != null){
            val rate = dbHelper.getSingleRate(id=rateId)!!.rate
            val time = dbHelper.getSingleRate(id=rateId)!!.time
            val minRate = dbHelper.getThreshold(id=thresholdId)!!.minRate
            val maxRate = dbHelper.getThreshold(id=thresholdId)!!.maxRate

            if (rate < minRate ){
                sendSms("Automatic message. $name heart rate is too low. Rate: $rate BPM, time: $time")
                NotificationsManager(context).sendNotification(1,"Warning", "$name, your heart rate is too low. Rate: $rate BPM")
            }
            else if(rate > maxRate){
                sendSms("Automatic message. $name heart rate is too high. Rate: $rate BPM, time: $time")
                NotificationsManager(context).sendNotification(2,"Warning", "$name, your heart rate is too high. Rate: $rate BPM")
            }
            else{
                println("$name heart rate is good. Rate: $rate, time: $time")
            }
        }
        else{
            println("Some data is missing")
        }

    }

    //Sending automatic emergency sms messages for added contacts using SmsManager class
    private fun sendSms(message: String){
        val allContacts = dbHelper.getAllContacts()

        if (allContacts.isNotEmpty()){
            allContacts.forEach{ c ->
                //SmsManager().sendSMS(c.phoneNumber, message)
            }
        }
        else{
            println("Contacts not found")
        }

    }
}