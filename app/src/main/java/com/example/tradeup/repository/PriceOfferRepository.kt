package com.example.tradeup.repository

import android.content.Context
import com.example.tradeup.database.DatabaseHelper
import com.example.tradeup.model.PriceOffer
import com.example.tradeup.model.Message
import com.example.tradeup.model.MessageType
import com.example.tradeup.utils.FirebaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PriceOfferRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val chatRepository = ChatRepository()
    
    /**
     * Tạo đề nghị giá mới và gửi tin nhắn đặc biệt vào chat
     */
    suspend fun createPriceOffer(
        productId: Long,
        buyerEmail: String,
        sellerEmail: String,
        originalPrice: Double,
        offeredPrice: Double,
        message: String,
        productTitle: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            // Tạo conversation ID
            val conversationId = "${buyerEmail}_${sellerEmail}_$productId"
            
            // Tạo đề nghị giá trong database
            val priceOffer = PriceOffer(
                productId = productId,
                buyerEmail = buyerEmail,
                sellerEmail = sellerEmail,
                originalPrice = originalPrice,
                offeredPrice = offeredPrice,
                message = message,
                conversationId = conversationId,
                createdAt = System.currentTimeMillis()
            )
            
            val offerId = dbHelper.createPriceOffer(priceOffer)
                ?: throw Exception("Không thể tạo đề nghị giá")
            
            // Tạo tin nhắn đặc biệt trong chat với messageId an toàn cho Firebase
            val safeOfferId = FirebaseUtils.sanitizeForFirebaseKey(offerId)
            val specialMessage = Message(
                messageId = "offer_$safeOfferId",
                sender = buyerEmail,
                receiver = sellerEmail,
                text = "Đề nghị giá cho sản phẩm: $productTitle",
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.PRICE_OFFER.value,
                priceOfferId = offerId,
                metadata = mapOf(
                    "productTitle" to productTitle,
                    "originalPrice" to originalPrice,
                    "offeredPrice" to offeredPrice,
                    "offerMessage" to message
                )
            )
            
            // Gửi tin nhắn vào Firebase
            chatRepository.sendSpecialMessage(
                buyerEmail = buyerEmail,
                sellerEmail = sellerEmail,
                message = specialMessage,
                onSuccess = { onSuccess(offerId) },
                onError = onError
            )
            
        } catch (e: Exception) {
            onError(e)
        }
    }
    
    /**
     * Phản hồi đề nghị giá (chấp nhận hoặc từ chối)
     */
    suspend fun respondToPriceOffer(
        offerId: String,
        isAccepted: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val status = if (isAccepted) "ACCEPTED" else "REJECTED"
            
            // Cập nhật trạng thái trong database
            val success = dbHelper.updatePriceOfferStatus(offerId, status)
            if (!success) {
                throw Exception("Không thể cập nhật đề nghị giá")
            }
            
            // Lấy thông tin đề nghị giá để gửi tin nhắn phản hồi
            val priceOffer = dbHelper.getPriceOfferById(offerId)
                ?: throw Exception("Không tìm thấy đề nghị giá")
            
            val responseMessage = Message(
                messageId = "response_${FirebaseUtils.sanitizeForFirebaseKey(offerId)}",
                sender = priceOffer.sellerEmail,
                receiver = priceOffer.buyerEmail,
                text = if (isAccepted) "Đã chấp nhận đề nghị giá của bạn" else "Đã từ chối đề nghị giá của bạn",
                timestamp = System.currentTimeMillis(),
                messageType = MessageType.PRICE_RESPONSE.value,
                priceOfferId = offerId,
                metadata = mapOf(
                    "isAccepted" to isAccepted,
                    "offeredPrice" to priceOffer.offeredPrice
                )
            )
            
            // Gửi tin nhắn phản hồi
            chatRepository.sendSpecialMessage(
                buyerEmail = priceOffer.buyerEmail,
                sellerEmail = priceOffer.sellerEmail,
                message = responseMessage,
                onSuccess = onSuccess,
                onError = onError
            )
            
        } catch (e: Exception) {
            onError(e)
        }
    }
    
    /**
     * Lấy đề nghị giá theo ID
     */
    suspend fun getPriceOffer(offerId: String): PriceOffer? = withContext(Dispatchers.IO) {
        dbHelper.getPriceOfferById(offerId)
    }
    
    /**
     * Lấy tất cả đề nghị giá cho sản phẩm
     */
    suspend fun getPriceOffersForProduct(productId: Long): List<PriceOffer> = withContext(Dispatchers.IO) {
        dbHelper.getPriceOffersForProduct(productId)
    }
    
    /**
     * Lấy đề nghị giá mà người dùng đã gửi
     */
    suspend fun getPriceOffersBySender(buyerEmail: String): List<PriceOffer> = withContext(Dispatchers.IO) {
        dbHelper.getPriceOffersBySender(buyerEmail)
    }
}
