package app.app

import android.content.Context

class CheckData(context: Context) {

    private val dbHelper = DBHelper(context)

    fun checkRate(rateId: Int, thresholdId: Int){
        if(dbHelper.getSingleRate(id=rateId) != null && dbHelper.getThreshold(id=thresholdId) != null){
            val rate = dbHelper.getSingleRate(id=rateId)!!.rate
            val time = dbHelper.getSingleRate(id=rateId)!!.time
            val minRate = dbHelper.getThreshold(id=thresholdId)!!.minRate
            val maxRate = dbHelper.getThreshold(id=thresholdId)!!.maxRate

            if (rate < minRate ){
                println("Heart rate is too low. Rate: $rate, time: $time")
                sendSms("Heart rate is too low. Rate: $rate, time: $time")
            }
            else if(rate > maxRate){
                println("Heart rate is too high. Rate: $rate, time: $time")
                sendSms("Heart rate is too high. Rate: $rate, time: $time")
            }
            else{
                println("Heart rate is good. Rate: $rate, time: $time")
            }
        }
        else{
            println("Some data is missing")
        }

    }

    fun sendSms(message: String){
        val allContacts = dbHelper.getAllContacts()

        if (allContacts.isNotEmpty()){
            allContacts.forEach{ c ->
                SmsManager().sendSMS(c.phoneNumber, message)
            }
        }
        else{
            println("Contacts not found")
        }

    }
}