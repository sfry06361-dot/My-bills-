package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.attemptAutoSignIn
import com.example.auth.signOut
import com.example.data.repository.BillRepository
import com.example.ui.BillTrackerApp
import com.example.ui.BillViewModel
import com.example.ui.theme.BillTrackTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppRoot()
        }
    }
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var guestUid by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (currentUser != null) guestUid = null
        }
        Firebase.auth.addAuthStateListener(listener)

        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    val activeUid = user?.uid ?: guestUid

    if (activeUid == null) {
        AuthScreen(
            onAuthSuccess = { uid ->
                if (uid != null) {
                    guestUid = uid
                } else {
                    currentUser = Firebase.auth.currentUser
                }
            }
        )
    } else {
        val databaseId = context.getString(R.string.firestore_database_id)
        key(activeUid) {
            val viewModel: BillViewModel = viewModel(
                key = activeUid,
                factory = viewModelFactory {
                    initializer {
                        val db = FirebaseFirestore.getInstance(databaseId)
                        BillViewModel(BillRepository(db), activeUid)
                    }
                }
            )

            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            BillTrackTheme(appTheme = appTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BillTrackerApp(
                        viewModel = viewModel,
                        onSignOutClicked = {
                            if (guestUid != null) {
                                guestUid = null
                            } else {
                                val credentialManager = CredentialManager.create(context)
                                signOut(credentialManager, coroutineScope) {
                                    currentUser = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
