package com.example.tradeup.repository

import com.example.tradeup.model.Conversation
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ConversationRepository {
    
    private val database = FirebaseDatabase.getInstance()
    private val messagesRef = database.getReference("messages")
    
    /**
     * Lấy danh sách các cuộc trò chuyện của user
     */
    fun getUserConversations(userEmail: String): Flow<List<Conversation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableListOf<Conversation>()
                
                for (conversationSnapshot in snapshot.children) {
                    val conversationId = conversationSnapshot.key ?: continue
                    
                    // Kiểm tra xem user có tham gia conversation này không
                    val userEmailFormatted = userEmail.replace(".", "_")
                    if (conversationId.contains(userEmailFormatted)) {
                        
                        // Lấy tin nhắn cuối cùng
                        var lastMessage = ""
                        var lastTimestamp = 0L
                        var otherUserEmail = ""
                        
                        for (messageSnapshot in conversationSnapshot.children) {
                            val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            if (timestamp > lastTimestamp) {
                                lastTimestamp = timestamp
                                lastMessage = messageSnapshot.child("text").getValue(String::class.java) ?: ""
                                
                                val sender = messageSnapshot.child("sender").getValue(String::class.java) ?: ""
                                val receiver = messageSnapshot.child("receiver").getValue(String::class.java) ?: ""
                                
                                // Xác định email của người kia
                                otherUserEmail = if (sender == userEmail) receiver else sender
                            }
                        }
                        
                        if (lastMessage.isNotEmpty()) {
                            val conversation = Conversation(
                                conversationId = conversationId,
                                otherUserEmail = otherUserEmail,
                                lastMessage = lastMessage,
                                lastTimestamp = lastTimestamp,
                                productTitle = "" // Sẽ cập nhật sau nếu cần
                            )
                            conversations.add(conversation)
                        }
                    }
                }
                
                // Sắp xếp theo thời gian mới nhất
                conversations.sortByDescending { it.lastTimestamp }
                trySend(conversations)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        messagesRef.addValueEventListener(listener)
        
        awaitClose {
            messagesRef.removeEventListener(listener)
        }
    }
    
    /**
     * Tạo conversation ID từ hai email
     */
    fun createConversationId(email1: String, email2: String): String {
        val emails = listOf(email1.replace(".", "_"), email2.replace(".", "_")).sorted()
        return "${emails[0]}_${emails[1]}"
    }
}
