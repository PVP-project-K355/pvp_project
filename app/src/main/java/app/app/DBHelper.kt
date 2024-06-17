package app.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//import com.github.mikephil.charting.data.Entry

data class User(
    val id: Int = 0,
    val name: String,
    val surname: String,
    val height: Double,
    val weight: Double,
    val birthdate: String
)

data class LoginData(
    val id: Int = 0,
    val token: String,
    val name: String,
    val surname: String,
    val email: String,
    // picture URI
    val uri: String,
    // is logged in? integer bool
    val status: Int
)

data class HeartRate(
    val id: Int = 0,
    val heartRate: Int,
    val date: Long
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
    val maxRate: Int,
    val stepsGoal: Int
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
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "Database.db"

        // User table
        private const val table_user = "User_data"
        private const val user_id = "Id"
        private const val user_name = "Name"
        private const val user_surname = "Surname"
        private const val user_height = "Height"
        private const val user_weight = "Weight"
        private const val user_birthdate = "Birthdate"

        // Login data table
        private const val table_login = "Login_data"
        private const val login_id = "id"
        private const val login_token = "token"
        private const val login_name = "name"
        private const val login_surname = "surname"
        private const val login_email = "email"
        private const val login_uri = "uri"
        private const val login_status = "status"

        //Heart rate table
        private const val TABLE_HEART_RATE = "HeartRate"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_HEART_RATE = "heartRate"

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
        private const val threshold_stepGoal = "Daily_steps_goal"

        //API table
        private const val table_api = "API_data"
        private const val api_accessToken = "Access_token"
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

        val createLoginTable = ("CREATE TABLE $table_login("
                + "$login_id INTEGER PRIMARY KEY,"
                + "$login_token TEXT,"
                + "$login_name TEXT,"
                + "$login_surname TEXT,"
                + "$login_email TEXT,"
                + "$login_uri TEXT,"
                + "$login_status INTEGER"
                + ")")
        db.execSQL(createLoginTable)

        val createTableHeartRate = ("CREATE TABLE $TABLE_HEART_RATE ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_DATE INTEGER,"
                + "$COLUMN_HEART_RATE INTEGER)")
        db.execSQL(createTableHeartRate)

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
                + "$threshold_maxRate INTEGER,"
                + "$threshold_stepGoal INTEGER"
                + ")")
        db.execSQL(createThresholdTable)

        val createAPITable = ("CREATE TABLE $table_api("
                + "$api_accessToken TEXT,"
                + "$api_expiresIn INTEGER"
                + ")")
        db.execSQL(createAPITable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $table_user")
        db.execSQL("DROP TABLE IF EXISTS $table_login")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HEART_RATE")
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

    //Login table
    //Add login data
    fun addLogin(login: LoginData): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(login_token, login.token)
        values.put(login_name, login.name)
        values.put(login_surname, login.surname)
        values.put(login_email, login.email)
        values.put(login_uri, login.uri)
        values.put(login_status, login.status)
        db.execSQL("delete from $table_login");
        val id = db.insert(table_login, null, values)
        db.close()
        return id
    }

//    //Get user data
//    @SuppressLint("Range")
//    fun getUser(id: Int): User? {
//        var user: User? = null
//        val db = readableDatabase
//        val selection = "id = ?"
//        val selectionArgs = arrayOf(id.toString())
//        val cursor: Cursor = db.query(table_user, null, selection, selectionArgs, null, null, null)
//
//        if (cursor.moveToFirst()) {
//            val name = cursor.getString(cursor.getColumnIndex(user_name))
//            val surname = cursor.getString(cursor.getColumnIndex(user_surname))
//            val height = cursor.getDouble(cursor.getColumnIndex(user_height))
//            val weight = cursor.getDouble(cursor.getColumnIndex(user_weight))
//            val birth = cursor.getString(cursor.getColumnIndex(user_birthdate))
//
//            user = User(id, name, surname, height, weight, birth)
//        }
//
//        cursor.close()
//        db.close()
//        return if (user != null) user else null
//    }
//
//    //Update user data
//    fun updateUser(user: User): Int {
//        val db = this.writableDatabase
//        val values = ContentValues()
//        values.put(user_name, user.name)
//        values.put(user_surname, user.surname)
//        values.put(user_height, user.height)
//        values.put(user_weight, user.weight)
//        values.put(user_birthdate, user.birthdate)
//
//        return db.update(
//            table_user, values, "$user_id = ?",
//            arrayOf(user.id.toString())
//        )
//    }

    //Heart rate table
    //Add heart rate data
    fun insertHeartRate(date: Long, heartRate: Int): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_DATE, date)
            put(COLUMN_HEART_RATE, heartRate)
        }
        val id = db.insert(TABLE_HEART_RATE, null, contentValues)
        db.close()
        return id
    }

    fun getHeartRateEntries(min_date: Long, max_date: Long): List<HeartRate> {
        val entries = mutableListOf<HeartRate>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HEART_RATE WHERE $COLUMN_DATE > ? AND $COLUMN_DATE <= ?", arrayOf("$min_date","$max_date"))
        if (cursor.moveToFirst()) {
            do {
                val heartRate = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HEART_RATE))
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                entries.add(HeartRate(heartRate = heartRate, date = date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return entries
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
        values.put(threshold_stepGoal, threshold.stepsGoal)

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
            val steps = cursor.getInt(cursor.getColumnIndex(threshold_stepGoal))

            threshold = Threshold(id, minRate, maxRate, steps)
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
        values.put(threshold_stepGoal, threshold.stepsGoal)

        return db.update(
            table_threshold, values, "$threshold_id = ?",
            arrayOf(threshold.id.toString())
        )
    }

    //API table
    //Add api data
    @SuppressLint("Range")
    fun getValidAccessToken(): String? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            table_api,
            arrayOf(api_accessToken, api_expiresIn),
            null,
            null,
            null,
            null,
            null
        )
        var accessToken: String? = null
        cursor.moveToFirst()
        accessToken = cursor.getString(cursor.getColumnIndex(api_accessToken))
        cursor.close()
        db.close()
        return accessToken
    }

    fun saveAccessToken(accessToken: String, expiresIn: Int): Long {
        val db = this.writableDatabase

        // Calculate expiration time in milliseconds
        val expirationTimeMillis = System.currentTimeMillis() + expiresIn * 1000L

        // Format expiration time as a string
        val expirationTimeString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(expirationTimeMillis))

        // Prepare data for insertion
        val values = ContentValues().apply {
            put(api_accessToken, accessToken)
            put(api_expiresIn, expirationTimeString)
        }

        // Clear old token
        db.delete(table_api, null, null)

        // Insert new token with expiration time
        val id = db.insert(table_api, null, values)

        db.close()

        return id
    }
}