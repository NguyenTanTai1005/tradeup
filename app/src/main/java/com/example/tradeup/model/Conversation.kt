package com.example.tradeup.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Model đại diện cho một cuộc trò chuyện
 */
data class Conversation(
    val conversationId: String,
    val otherUserEmail: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val productTitle: String = ""
) {
    fun getFormattedTime(): String {
        val date = Date(lastTimestamp)
        val today = Calendar.getInstance()
        val messageDate = Calendar.getInstance()
        messageDate.time = date
        
        val formatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formatDate = SimpleDateFormat("dd/MM", Locale.getDefault())
        
        return if (today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR)) {
            formatTime.format(date)
        } else {
            formatDate.format(date)
        }
    }
    
    fun getOtherUserName(): String {
        return otherUserEmail.split("@").firstOrNull() ?: otherUserEmail
    }
}
