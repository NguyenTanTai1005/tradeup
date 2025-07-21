package com.example.tradeup.auth

import com.example.tradeup.model.User

interface GoogleSignInHandler {
    fun setCallbacks(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    )
    
    fun signIn()
    
    fun signOut()
    
    fun isSignedIn(): Boolean
    
    fun getCurrentUser(): User?
}
