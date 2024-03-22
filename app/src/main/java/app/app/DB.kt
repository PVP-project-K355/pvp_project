package app.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class User(
    val id: Int = 0,
    val name: String,
    val surname: String,
    val height: Double,
    val weight: Double,
    val birthdate: String
)

data class HeartRate(
    val id: Int = 0,
    val rate: Int,
    val time: String
)

data class Contact(
    val id: Int = 0,
    val name: String,
    val surname: String,
    val phoneNumber: String
)

data class Threshold(
    val id: Int = 0,
    val minRate: Int,
    val maxRate: Int
)

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "Database.db"

        // User table
        private const val table_user = "User"
        private const val user_id = "Id"
        private const val user_name = "Name"
        private const val user_surname = "Surname"
        private const val user_height = "Height"
        private const val user_weight = "Weight"
        private const val user_birthdate = "Birthdate"

        //Heart rate table
        private const val table_Heart_Rate = "Heart_rate_data"
        private const val heart_id = "Id"
        private const val heart_rate = "Heart_rate"
        private const val heart_time = "Heart_rate_measurement_time"

        //Contact person table
        private const val table_Contact = "Contact"
        private const val contact_id = "Id"
        private const val contact_name = "Name"
        private const val contact_surname = "Surname"
        private const val contact_phone = "Phone_number"

        //Threshold table
        private const val table_threshold = "Threshold_data"
        private const val threshold_id = "Id"
        private const val threshold_minRate = "Min_heart_rate"
        private const val threshold_maxRate = "Max_heart_rate"

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = ("CREATE TABLE $table_user("
                + "$user_id INTEGER PRIMARY KEY,"
                + "$user_name TEXT,"
                + "$user_surname TEXT,"
                + "$user_height REAL,"
                + "$user_weight REAL,"
                + "$user_birthdate TEXT"
                + ")")
        db.execSQL(createUserTable)

        val createHeartTable = ("CREATE TABLE $table_Heart_Rate("
                + "$heart_id INTEGER PRIMARY KEY,"
                + "$heart_rate INTEGER,"
                + "$heart_time TEXT"
                + ")")
        db.execSQL(createHeartTable)

        val createContactTable = ("CREATE TABLE $table_Contact("
                + "$contact_id INTEGER PRIMARY KEY,"
                + "$contact_name TEXT,"
                + "$contact_surname TEXT,"
                + "$contact_phone TEXT"
                + ")")

        db.execSQL(createContactTable)

        val createThresholdTable = ("CREATE TABLE $table_threshold("
                + "$threshold_id INTEGER PRIMARY KEY,"
                + "$threshold_minRate INTEGER,"
                + "$threshold_maxRate INTEGER"

                + ")")
        db.execSQL(createThresholdTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $table_user")
        db.execSQL("DROP TABLE IF EXISTS $table_Heart_Rate")
        db.execSQL("DROP TABLE IF EXISTS $table_Contact")
        db.execSQL("DROP TABLE IF EXISTS $table_threshold")
        onCreate(db)
    }

    //User table
    // Adding new user
    fun addUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(user_name, user.name)
        values.put(user_surname, user.surname)
        values.put(user_height, user.height)
        values.put(user_weight, user.weight)
        values.put(user_birthdate, user.birthdate)
        val id = db.insert(table_user, null, values)
        db.close()
        return id
    }

    // Getting single user
    @SuppressLint("Range")
    fun getUser(id: Int): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            table_user, arrayOf(
                user_id,
                user_name,
                user_surname,
                user_height,
                user_weight,
                user_birthdate
            ), "$user_id=?", arrayOf(id.toString()), null, null, null, null
        )
        cursor?.moveToFirst()

        val user = cursor?.let {
            User(
                it.getInt(it.getColumnIndex(user_id)),
                it.getString(it.getColumnIndex(user_name)),
                it.getString(it.getColumnIndex(user_surname)),
                it.getDouble(it.getColumnIndex(user_height)),
                it.getDouble(it.getColumnIndex(user_weight)),
                it.getString(it.getColumnIndex(user_birthdate))
            )
        }
        cursor?.close()
        return user
    }

    // Updating single user
    fun updateUser(user: User): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(user_name, user.name)
        values.put(user_surname, user.surname)
        values.put(user_height, user.height)
        values.put(user_weight, user.weight)
        values.put(user_birthdate, user.birthdate)

        return db.update(
            table_user, values, "$user_id = ?",
            arrayOf(user.id.toString())
        )
    }

    //Heart rate table
    // Adding heart rate data
    fun addHeartRate(heartRate: HeartRate): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(heart_rate, heartRate.rate)
        values.put(heart_time, heartRate.time)
        val id = db.insert(table_Heart_Rate, null, values)
        db.close()
        return id
    }

    // Getting single heart rate
    @SuppressLint("Range")
    fun getSingleRate(id: Int): HeartRate? {
        val db = this.readableDatabase
        val cursor = db.query(
            table_Heart_Rate, arrayOf(
                heart_id,
                heart_rate,
                heart_time
            ), "$heart_id=?", arrayOf(id.toString()), null, null, null, null
        )
        cursor?.moveToFirst()

        val rate = cursor?.let {
            HeartRate(
                it.getInt(it.getColumnIndex(heart_id)),
                it.getInt(it.getColumnIndex(heart_rate)),
                it.getString(it.getColumnIndex(heart_time))
            )
        }
        cursor?.close()
        return rate
    }

    @SuppressLint("Range")
    fun getAllRate(): ArrayList<HeartRate> {
        val rateList = ArrayList<HeartRate>()
        val selectQuery = "SELECT * FROM $table_Heart_Rate"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val rate = HeartRate(
                    cursor.getInt(cursor.getColumnIndex(heart_id)),
                    cursor.getInt(cursor.getColumnIndex(heart_rate)),
                    cursor.getString(cursor.getColumnIndex(heart_time))
                )
                rateList.add(rate)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return rateList
    }

    //Contact table
    // Adding new contact
    fun addContact(contact: Contact): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(contact_name, contact.name)
        values.put(contact_surname, contact.surname)
        values.put(contact_phone, contact.phoneNumber)
        val id = db.insert(table_Contact, null, values)
        db.close()
        return id
    }

    // Getting single contact
    @SuppressLint("Range")
    fun getContact(id: Int): Contact? {
        val db = this.readableDatabase
        val cursor = db.query(
            table_Contact, arrayOf(
                contact_id,
                contact_name,
                contact_surname,
                contact_phone
            ), "$contact_id=?", arrayOf(id.toString()), null, null, null, null
        )
        cursor?.moveToFirst()

        val contact = cursor?.let {
            Contact(
                it.getInt(it.getColumnIndex(contact_id)),
                it.getString(it.getColumnIndex(contact_name)),
                it.getString(it.getColumnIndex(contact_surname)),
                it.getString(it.getColumnIndex(contact_phone))
            )
        }
        cursor?.close()
        return contact
    }

    //Get all contacts
    @SuppressLint("Range")
    fun getAllContacts(): ArrayList<Contact> {
        val contactsList = ArrayList<Contact>()
        val selectQuery = "SELECT * FROM $table_Contact"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToFirst()) {
            do {
                val contact = Contact(
                    cursor.getInt(cursor.getColumnIndex(contact_id)),
                    cursor.getString(cursor.getColumnIndex(contact_name)),
                    cursor.getString(cursor.getColumnIndex(contact_surname)),
                    cursor.getString(cursor.getColumnIndex(contact_surname))
                )
                contactsList.add(contact)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contactsList
    }

    // Updating contact
    fun updateContact(contact: Contact): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(contact_name, contact.name)
        values.put(contact_surname, contact.surname)
        values.put(contact_phone, contact.phoneNumber)

        return db.update(
            table_Contact, values, "$contact_id = ?",
            arrayOf(contact.id.toString())
        )
    }

    //Delete contact
    fun deleteContact(contact: Contact) {
        val db = this.writableDatabase
        db.delete(
            table_Contact, "$contact_id = ?",
            arrayOf(contact.id.toString())
        )
        db.close()
    }

    //Threshold table
    // Adding heart rate data
    fun addThreshold(threshold: Threshold): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(threshold_minRate, threshold.minRate)
        values.put(threshold_maxRate, threshold.maxRate)

        val id = db.insert(table_threshold, null, values)
        db.close()
        return id
    }

}