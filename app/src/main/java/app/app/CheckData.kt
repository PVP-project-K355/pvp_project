package app.app

import android.content.Context
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

/*This class is used to check if data is between thresholds and if it is outside thresholds
sends automatic emergency sms messages for added contacts using SmsManager class*/

class CheckData(private val context: Context) {

    private val dbHelper = DBHelper(context)

    private val name = dbHelper.getUser(1)?.name

    private var age: Int = 0
    private var maxPossibleRate: Int = 0

    //Checking heart rate data
//    fun checkRate(rateId: Int, thresholdId: Int){
//
//        if(dbHelper.getUser(1) != null){
//            age = calculateAge(dbHelper.getUser(1)!!.birthdate)
//        }else {
//            age = 0
//        }
//
//        if(age > 0){
//            maxPossibleRate = 220 - age
//        }
//        else{
//            maxPossibleRate = 0
//        }
//
//        println("Age: $age, max rate: $maxPossibleRate")
//
//        if(dbHelper.getSingleRate(id=rateId) != null && dbHelper.getThreshold(id=thresholdId) != null && maxPossibleRate > 100){
//            val rate = dbHelper.getSingleRate(id=rateId)!!.rate
//            val time = dbHelper.getSingleRate(id=rateId)!!.time
//            val minRate = dbHelper.getThreshold(id=thresholdId)!!.minRate
//            val maxRate = dbHelper.getThreshold(id=thresholdId)!!.maxRate
//
//            if (rate < minRate*0.85 ){
//                sendSms("Automatic message. $name heart rate is too low. Rate: $rate BPM, time: $time")
//                NotificationsManager(context).sendNotification(1,"Warning", "$name, your heart rate is too low. Rate: $rate BPM")
//            }
//            else if(rate < minRate && rate >= minRate*0.85){
//                NotificationsManager(context).sendNotification(2,"Warning", "$name, your heart rate is too low. Rate: $rate BPM")
//            }
//            else if(rate > maxRate && rate <= maxPossibleRate*0.7){
//                NotificationsManager(context).sendNotification(3,"Warning", "$name, your heart rate is too high. Rate: $rate BPM")
//            }
//            else if(rate > maxPossibleRate*0.7){
//                sendSms("Automatic message. $name heart rate is too high. Rate: $rate BPM, time: $time")
//                NotificationsManager(context).sendNotification(4,"Warning", "$name, your heart rate is too high. Rate: $rate BPM")
//            }
//            else{
//                println("$name heart rate is good. Rate: $rate, time: $time")
//            }
//        }
//        else{
//            println("Some data is missing")
//        }
//
//    }

    //Calculating person age
    fun calculateAge(birthDate: String): Int {
        // Define the format of the input date string
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Parse the birthDate string to LocalDate
        val birthLocalDate = LocalDate.parse(birthDate, formatter)

        // Get the current date
        val currentDate = LocalDate.now()

        // Calculate the age
        return Period.between(birthLocalDate, currentDate).years
    }

    //Sending automatic emergency sms messages for added contacts using SmsManager class
    private fun sendSms(message: String){
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