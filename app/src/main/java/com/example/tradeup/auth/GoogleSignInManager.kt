package com.example.tradeup.auth

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.example.tradeup.model.User
import android.util.Log

class GoogleSignInManager(
    private val activity: ComponentActivity
) : GoogleSignInHandler {
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<android.content.Intent>
    
    private var onSuccessCallback: ((User) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
    init {
        setupGoogleSignIn()
        setupActivityResultLauncher()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("782121234664-gk3hsk7p789rcd5705710dgu2v0tn0f9.apps.googleusercontent.com")
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }
    
    private fun setupActivityResultLauncher() {
        signInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("GoogleSignInManager", "Google sign in failed", e)
                onErrorCallback?.invoke("Đăng nhập Google thất bại: ${e.statusCode}")
            }
        }
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        val appUser = User(
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            password = "", // Google users don't have password
                            firebaseUid = user.uid,
                            avatarUrl = user.photoUrl?.toString(),
                            isActive = true
                        )
                        
                        saveUserToDatabase(appUser)
                        onSuccessCallback?.invoke(appUser)
                    }
                } else {
                    onErrorCallback?.invoke("Firebase authentication failed")
                }
            }
    }
    
    private fun saveUserToDatabase(user: User) {
        user.firebaseUid?.let { uid ->
            database.child("users").child(uid).setValue(user)
        }
    }
    
    override fun setCallbacks(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        onSuccessCallback = onSuccess
        onErrorCallback = onError
    }
    
    override fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
    
    override fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }
    
    override fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { user ->
            User(
                name = user.displayName ?: "",
                email = user.email ?: "",
                password = "",
                firebaseUid = user.uid,
                avatarUrl = user.photoUrl?.toString(),
                isActive = true
            )
        }
    }
}
