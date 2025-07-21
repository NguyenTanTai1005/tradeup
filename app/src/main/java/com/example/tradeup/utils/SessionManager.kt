package com.example.tradeup.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.tradeup.model.User

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("tradeup_session", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_BIO = "user_bio"
    }
    
    fun saveUserSession(user: User) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_PHONE, user.phone)
        editor.putString(KEY_USER_BIO, user.bio)
        editor.apply()
    }

    fun saveUser(user: User) {
        saveUserSession(user)
    }
    
    fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null
        
        val id = sharedPreferences.getLong(KEY_USER_ID, 0)
        val email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
        val name = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        val phone = sharedPreferences.getString(KEY_USER_PHONE, "") ?: ""
        val bio = sharedPreferences.getString(KEY_USER_BIO, "") ?: ""
        
        return if (id > 0 && email.isNotEmpty()) {
            User(id = id, email = email, password = "", name = name, phone = phone, bio = bio)
        } else null
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
