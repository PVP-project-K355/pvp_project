package app.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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

data class API(
    val id: Int = 0,
    val clientId: String,
    val clientSecret: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "Database.db"

        // User table
        private const val table_user = "User_data"
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
        private const val table_Contact = "Contact_data"
        private const val contact_id = "Id"
        private const val contact_name = "Name"
        private const val contact_surname = "Surname"
        private const val contact_phone = "Phone_number"

        //Threshold table
        private const val table_threshold = "Threshold_data"
        private const val threshold_id = "Id"
        private const val threshold_minRate = "Min_heart_rate"
        private const val threshold_maxRate = "Max_heart_rate"

        //API table
        private const val table_api = "API_data"
        private const val api_id = "Id"
        private const val api_clientId = "Client_id"
        private const val api_clientSecret = "Client_secret"
        private const val api_accessToken = "Access_token"
        private const val api_refresfToken = "Refresh_token"
        private const val api_expiresIn = "Expires_in"

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

        val createAPITable = ("CREATE TABLE $table_api("
                + "$api_id INTEGER PRIMARY KEY,"
                + "$api_clientId TEXT,"
                + "$api_clientSecret TEXT,"
                + "$api_accessToken TEXT,"
                + "$api_refresfToken TEXT,"
                + "$api_expiresIn INTEGER"
                + ")")
        db.execSQL(createAPITable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $table_user")
        db.execSQL("DROP TABLE IF EXISTS $table_Heart_Rate")
        db.execSQL("DROP TABLE IF EXISTS $table_Contact")
        db.execSQL("DROP TABLE IF EXISTS $table_threshold")
        db.execSQL("DROP TABLE IF EXISTS $table_api")
        onCreate(db)
    }

    //User table
    //Add user data
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

    //Get user data
    @SuppressLint("Range")
    fun getUser(id: Int): User? {
        var user: User? = null
        val db = readableDatabase
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor: Cursor = db.query(table_user, null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndex(user_name))
            val surname = cursor.getString(cursor.getColumnIndex(user_surname))
            val height = cursor.getDouble(cursor.getColumnIndex(user_height))
            val weight = cursor.getDouble(cursor.getColumnIndex(user_weight))
            val birth = cursor.getString(cursor.getColumnIndex(user_birthdate))

            user = User(id, name, surname, height, weight, birth)
        }

        cursor.close()
        db.close()
        return if (user != null) user else null
    }

    //Update user data
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
    //Add heart rate data
    fun addHeartRate(heartRate: HeartRate): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(heart_rate, heartRate.rate)
        values.put(heart_time, heartRate.time)
        val id = db.insert(table_Heart_Rate, null, values)
        db.close()
        return id
    }

    //Get single heart rate data
    @SuppressLint("Range")
    fun getSingleRate(id: Int): HeartRate? {
        var rate: HeartRate? = null
        val db = readableDatabase
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor: Cursor = db.query(table_Heart_Rate, null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val heartRate = cursor.getInt(cursor.getColumnIndex(heart_rate))
            val time = cursor.getString(cursor.getColumnIndex(heart_time))

            rate = HeartRate(id, heartRate, time)
        }

        cursor.close()
        db.close()
        return if (rate != null) rate else null
    }

    //Get all heart rates data
    @SuppressLint("Range")
    fun getAllRates(): ArrayList<HeartRate> {
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

    //Delete single heart rate data
    fun deleteSingleHeartRate(rate: HeartRate) {
        val db = this.writableDatabase
        db.delete(
            table_Heart_Rate, "$heart_id = ?",
            arrayOf(rate.id.toString())
        )
        db.close()
    }

    // Delete all heart rates
    fun deleteAllHeartRates() {
        val db = this.writableDatabase
        db.delete(table_Heart_Rate, null, null)
        db.close()
    }

    //Contact table
    //Add new contact data
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

    //Get single contact data
    @SuppressLint("Range")
    fun getContact(id: Int): Contact? {
        var contact: Contact? = null
        val db = readableDatabase
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor: Cursor = db.query(table_Contact, null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndex(contact_name))
            val surname = cursor.getString(cursor.getColumnIndex(contact_surname))
            val phone = cursor.getString(cursor.getColumnIndex(contact_phone))

            contact = Contact(id, name, surname, phone)
        }

        cursor.close()
        db.close()
        return if (contact != null) contact else null
    }

    //Get all contacts data
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
                    cursor.getString(cursor.getColumnIndex(contact_phone))
                )
                contactsList.add(contact)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return contactsList
    }

    //Update contact data
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

    //Delete contact data
    fun deleteContact(contact: Contact) {
        val db = this.writableDatabase
        db.delete(
            table_Contact, "$contact_id = ?",
            arrayOf(contact.id.toString())
        )
        db.close()
    }

    //Threshold table
    //Add threshold data
    fun addThreshold(threshold: Threshold): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(threshold_minRate, threshold.minRate)
        values.put(threshold_maxRate, threshold.maxRate)

        val id = db.insert(table_threshold, null, values)
        db.close()
        return id
    }

    //Get threshold data
    @SuppressLint("Range")
    fun getThreshold(id: Int): Threshold? {
        var threshold: Threshold? = null
        val db = readableDatabase
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor: Cursor = db.query(table_threshold, null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val minRate = cursor.getInt(cursor.getColumnIndex(threshold_minRate))
            val maxRate = cursor.getInt(cursor.getColumnIndex(threshold_maxRate))

            threshold = Threshold(id, minRate, maxRate)
        }

        cursor.close()
        db.close()
        return if (threshold != null) threshold else null
    }

    //Update threshold data
    fun updateThreshold(threshold: Threshold): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(threshold_minRate, threshold.minRate)
        values.put(threshold_maxRate, threshold.maxRate)

        return db.update(
            table_threshold, values, "$threshold_id = ?",
            arrayOf(threshold.id.toString())
        )
    }

    //API table
    //Add api data
    fun addApi(api: API): Long{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(api_clientId, EncryptionUtils.encrypt(api.clientId))
        values.put(api_clientSecret, EncryptionUtils.encrypt(api.clientSecret))
        values.put(api_accessToken, EncryptionUtils.encrypt(api.accessToken))
        values.put(api_refresfToken, EncryptionUtils.encrypt(api.refreshToken))
        values.put(api_expiresIn, api.expiresIn)

        val id = db.insert(table_api, null, values)
        db.close()
        return id
    }

    //Get api data
    @SuppressLint("Range")
    fun getApi(id: Int): API? {
        var apiData: API? = null
        val db = readableDatabase
        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor: Cursor = db.query(table_api, null, selection, selectionArgs, null, null, null)

        if (cursor.moveToFirst()) {
            val clientId = EncryptionUtils.decrypt(cursor.getString(cursor.getColumnIndex(api_clientId)))
            val clientSecret = EncryptionUtils.decrypt(cursor.getString(cursor.getColumnIndex(api_clientSecret)))
            val accessToken = EncryptionUtils.decrypt(cursor.getString(cursor.getColumnIndex(api_accessToken)))
            val refreshToken = EncryptionUtils.decrypt(cursor.getString(cursor.getColumnIndex(api_refresfToken)))
            val expiresIn = cursor.getInt(cursor.getColumnIndex(api_expiresIn))

            apiData = API(id, clientId, clientSecret, accessToken, refreshToken, expiresIn)
        }

        cursor.close()
        db.close()
        return if (apiData != null) apiData else null
    }

    //Update api data
    fun updateApi(api: API): Int{
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(api_clientId, EncryptionUtils.encrypt(api.clientId))
        values.put(api_clientSecret, EncryptionUtils.encrypt(api.clientSecret))
        values.put(api_accessToken, EncryptionUtils.encrypt(api.accessToken))
        values.put(api_refresfToken, EncryptionUtils.encrypt(api.refreshToken))
        values.put(api_expiresIn, api.expiresIn)

        return db.update(
            table_api, values, "$api_id = ?",
            arrayOf(api.id.toString())
        )
    }

    //Delete api data
    fun deleteApi(api: API) {
        val db = this.writableDatabase
        db.delete(
            table_api, "$api_id = ?",
            arrayOf(api.id.toString())
        )
        db.close()
    }

}