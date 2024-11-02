package com.example.firebasetest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class HomeActivity : ComponentActivity() {

    private val updateLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FabAddData()
            FirebaseUserListScreen(updateLauncher)
        }
    }
}

@Composable
fun UserEntryItem(userEntry: UserEntry, onClick:() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick()
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = userEntry.heading, style = MaterialTheme.typography.bodyLarge)
            Text(text = userEntry.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun FirebaseUserListScreen(updateLauncher: ActivityResultLauncher<Intent>) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference
    val user = auth.currentUser

    var userEntries by remember { mutableStateOf<List<UserEntry>>(emptyList()) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            try {
                val dataSnapshot = database.child("users").child(user.uid).get().await()
                val entries = mutableListOf<UserEntry>()

                dataSnapshot.children.forEach { child ->
                    val entry = child.getValue(UserEntry::class.java)
                    if (entry != null) {
                        entries.add(entry)
                    }
                }
                userEntries = entries
            } catch (e: Exception) {
                status = "Error: ${e.message}"
            }
        } else {
            status = "User is not logged in"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (status.isNotEmpty()) {
            Text(text = status, style = MaterialTheme.typography.bodyMedium)
        } else if (userEntries.isEmpty()) {
            Text("No data found", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(userEntries) { entry ->
                    UserEntryItem(entry) {
                        val intent = Intent(context, EnterDataActivity::class.java)
                        intent.putExtra("uid",entry.entryId)
                        updateLauncher.launch(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun FabAddData() {
    val context = LocalContext.current
    val addEntryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK) {
                (context as Activity).recreate()
            }
        }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        val intent = Intent(context, EnterDataActivity::class.java)
        FloatingActionButton(onClick = {
            addEntryLauncher.launch(intent)
        }, modifier = Modifier.padding(16.dp)) {
            Image(painter = painterResource(id = R.drawable.ic_plus), contentDescription = null)
        }
    }
}

/*database.child("users").child(user!!.uid).child(entry.entryId).removeValue()
.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        Toast.makeText(context, "deleted", Toast.LENGTH_SHORT).show()
        (context as Activity).recreate()
//                                    activity.setResult(Activity.RESULT_OK)
//                                    activity.finish()
    } else {
        //activity.finish()
    }
}*/

data class UserEntry(
    val entryId: String = "",
    val heading: String = "",
    val content: String = ""
)

