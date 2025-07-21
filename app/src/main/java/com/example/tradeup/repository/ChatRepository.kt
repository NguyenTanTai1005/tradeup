package com.example.tradeup.repository

import com.example.tradeup.model.Message
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepository {
    
    private val database = FirebaseDatabase.getInstance()
    private val messagesRef = database.getReference("messages")
    
    // Lưu ý: setPersistenceEnabled() đã được gọi trong TradeUpApplication.onCreate()
    
    /**
     * Tạo conversation ID từ hai email
     * Sắp xếp theo thứ tự từ điển để đảm bảo unique ID
     */
    fun createConversationId(email1: String, email2: String): String {
        val emails = listOf(email1.replace(".", "_"), email2.replace(".", "_")).sorted()
        return "${emails[0]}_${emails[1]}"
    }
    
    /**
     * Gửi tin nhắn mới
     */
    fun sendMessage(
        buyerEmail: String,
        sellerEmail: String,
        senderEmail: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val conversationId = createConversationId(buyerEmail, sellerEmail)
        val messageRef = messagesRef.child(conversationId).push()
        
        val message = Message(
            messageId = messageRef.key ?: "",
            sender = senderEmail,
            receiver = if (senderEmail == buyerEmail) sellerEmail else buyerEmail,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        messageRef.setValue(message)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    
    /**
     * Lắng nghe tin nhắn real-time
     */
    fun getMessages(buyerEmail: String, sellerEmail: String): Flow<List<Message>> = callbackFlow {
        val conversationId = createConversationId(buyerEmail, sellerEmail)
        val conversationRef = messagesRef.child(conversationId)
        
        val listener = object : ChildEventListener {
            private val messages = mutableListOf<Message>()
            
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Message::class.java)?.let { message ->
                    messages.add(message)
                    messages.sortBy { it.timestamp }
                    trySend(messages.toList())
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Message::class.java)?.let { updatedMessage ->
                    val index = messages.indexOfFirst { it.messageId == updatedMessage.messageId }
                    if (index != -1) {
                        messages[index] = updatedMessage
                        messages.sortBy { it.timestamp }
                        trySend(messages.toList())
                    }
                }
            }
            
            override fun onChildRemoved(snapshot: DataSnapshot) {
                snapshot.getValue(Message::class.java)?.let { removedMessage ->
                    messages.removeAll { it.messageId == removedMessage.messageId }
                    trySend(messages.toList())
                }
            }
            
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        conversationRef.addChildEventListener(listener)
        
        awaitClose {
            conversationRef.removeEventListener(listener)
        }
    }
    
    /**
     * Gửi tin nhắn đặc biệt (đề nghị giá, phản hồi)
     */
    fun sendSpecialMessage(
        buyerEmail: String,
        sellerEmail: String,
        message: Message,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val conversationId = createConversationId(buyerEmail, sellerEmail)
        val messageRef = messagesRef.child(conversationId).child(message.messageId)
        
        messageRef.setValue(message)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
