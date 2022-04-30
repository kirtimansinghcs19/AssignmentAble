package com.example.assignmentable

import android.content.ContentValues
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Contacts
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.assignmentable.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentDate = ""
    private val TAG = "Read data activity"
    private var users: MutableList<User> = mutableListOf()
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Using view Binding to target the view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The entry point accessing a Firebase Database
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        // Accessing the current Date to fetch the today's data
        val date = Date()
        val formatter = SimpleDateFormat("dd/MM/yy")
        currentDate = formatter.format(date)


        // Fetching the today's from firebase cloud or backend
        binding.sync.setOnClickListener(View.OnClickListener {
            binding.progress.setVisibility(View.VISIBLE)
            db.collection("users")
                .whereEqualTo("date", currentDate)
                .get()
                .addOnCompleteListener(object : OnCompleteListener<QuerySnapshot?> {
                    override fun onComplete(task: Task<QuerySnapshot?>) {
                        binding.progress.setVisibility(View.GONE)

                        // If task is successful data is directly added to your contact list
                        if (task.isSuccessful()) {
                            users = mutableListOf()
                            Toast.makeText(
                                this@MainActivity,
                                "Successfully data is synced",
                                Toast.LENGTH_SHORT
                            ).show()
                            for (document in task.getResult()!!) {
                                Log.d(
                                    TAG,
                                    document.getId().toString() + " => " + document.getData()
                                )
                                val name: String? = document.getString("name")
                                val phone: String? = document.getString("phone")
                                val email: String? = document.getString("email")
                                val date: String? = document.getString("date")
                                Log.d(TAG, "$name $phone  $email $date")
                                if (!name?.isEmpty()!! && !phone?.isEmpty()!! && !email?.isEmpty()!! && !date?.isEmpty()!!) {
                                    addContact(name ?: "", phone ?: "", email ?: "")
                                    user = User(name ?: "", phone ?: "", email ?: "", date ?: "")
                                    user?.let {
                                        users.add(it)
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Not Successfully data is synced",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
        })
    }

    //  Contact method to add contact detail to user contact list
    private fun addContact(name: String, phone: String, email: String) {
        try {
            val values = ContentValues()
            values.put(Contacts.People.NUMBER, phone)
            values.put(Contacts.People.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
            values.put(Contacts.People.LABEL, "")
            values.put(Contacts.People.PRIMARY_EMAIL_ID, email)
            values.put(Contacts.People.NAME, name)
            val dataUri = contentResolver.insert(Contacts.People.CONTENT_URI, values)
            var updateUri = Uri.withAppendedPath(dataUri, Contacts.People.Phones.CONTENT_DIRECTORY)
            values.clear()
            values.put(Contacts.People.Phones.TYPE, Contacts.People.TYPE_MOBILE)
            values.put(Contacts.People.NUMBER, phone)
            updateUri = contentResolver.insert(updateUri!!, values)
            Log.d("CONTACT", "" + updateUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}