package com.example.firebasetest

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class EnterDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference
        val uid = intent.getStringExtra("uid") ?: ""
        setContent {
            MainScreen(auth = auth, database = database, uid)
        }
    }
}

@Composable
fun MainScreen(auth: FirebaseAuth, database: DatabaseReference, uid: String) {

    val user = auth.currentUser
    val context = LocalContext.current
    val activity = (context as Activity)

    var headingValue by remember {
        mutableStateOf("")
    }

    var contentValue by remember {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        OutlinedTextField(value = headingValue, onValueChange = {
            headingValue = it
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp),
            label = {
                Text(text = "Enter Heading")
            }
        )

        OutlinedTextField(value = contentValue, onValueChange = {
            contentValue = it
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                .weight(1f),
            label = {
                Text(text = "Enter Content")
            }
        )

        Button(onClick = {
            if(uid.isBlank()) {
                val entryRef = database.child("users").child(user!!.uid).push()
                val entry = UserEntry(entryRef.key!!,headingValue, contentValue)
                entryRef.setValue(entry)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        } else {
                            activity.finish()
                        }
                    }
            } else {
                val entryRef = database.child("users").child(user!!.uid).child(uid)
                val entry = UserEntry(uid,headingValue, contentValue)
                entryRef.setValue(entry)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        } else {
                            activity.finish()
                        }
                    }
            }

            headingValue = ""
            contentValue = ""

        }, modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(48.dp)
        ) {
            Text(text = "Add Note")
        }
    }
}