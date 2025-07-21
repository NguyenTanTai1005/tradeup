package com.example.tradeup.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.tradeup.model.Product
import com.example.tradeup.model.User
import com.example.tradeup.model.BlockedUser
import com.example.tradeup.model.PriceOffer
import com.example.tradeup.utils.FirebaseUtils
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tradeup.db"
        private const val DATABASE_VERSION = 10 // Tăng version để thêm bảng price_offers

        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_PHONE = "phone"
        private const val COLUMN_USER_BIO = "bio"
        private const val COLUMN_USER_FIREBASE_UID = "firebase_uid"
        private const val COLUMN_USER_AVATAR_URL = "avatar_url"
        private const val COLUMN_USER_IS_ACTIVE = "is_active"

        // Products table
        private const val TABLE_PRODUCTS = "products"
        private const val COLUMN_PRODUCT_ID = "id"
        private const val COLUMN_PRODUCT_TITLE = "title"
        private const val COLUMN_PRODUCT_DESCRIPTION = "description"
        private const val COLUMN_PRODUCT_PRICE = "price"
        private const val COLUMN_PRODUCT_USER_ID = "user_id"
        private const val COLUMN_PRODUCT_CREATED_AT = "created_at"
        private const val COLUMN_PRODUCT_STATUS = "status"
        private const val COLUMN_PRODUCT_CATEGORY = "category"
        private const val COLUMN_PRODUCT_CONDITION = "condition"
        private const val COLUMN_PRODUCT_CREATED_AT_TIMESTAMP = "created_at_timestamp"
        private const val COLUMN_PRODUCT_IMAGE_PATHS = "image_paths"
        private const val COLUMN_PRODUCT_RATING = "rating" // Rating từ 0.0 đến 5.0
        private const val COLUMN_PRODUCT_RATING_COUNT = "rating_count" // Số lượng đánh giá
        private const val COLUMN_PRODUCT_LOCATION = "location" // Vị trí sản phẩm
        private const val COLUMN_PRODUCT_LATITUDE = "latitude" // Tọa độ vĩ độ
        private const val COLUMN_PRODUCT_LONGITUDE = "longitude" // Tọa độ kinh độ
        private const val COLUMN_PRODUCT_SYNCED = "synced" // Trường mới để đánh dấu đã sync
        
        // Blocked Users table
        private const val TABLE_BLOCKED_USERS = "blocked_users"
        private const val COLUMN_BLOCKED_ID = "id"
        private const val COLUMN_BLOCKER_EMAIL = "blocker_email"
        private const val COLUMN_BLOCKED_EMAIL = "blocked_email"
        private const val COLUMN_BLOCKED_AT = "blocked_at"
        
        // Price Offers table
        private const val TABLE_PRICE_OFFERS = "price_offers"
        private const val COLUMN_OFFER_ID = "offer_id"
        private const val COLUMN_OFFER_PRODUCT_ID = "product_id"
        private const val COLUMN_OFFER_BUYER_EMAIL = "buyer_email"
        private const val COLUMN_OFFER_SELLER_EMAIL = "seller_email"
        private const val COLUMN_OFFER_ORIGINAL_PRICE = "original_price"
        private const val COLUMN_OFFER_OFFERED_PRICE = "offered_price"
        private const val COLUMN_OFFER_MESSAGE = "message"
        private const val COLUMN_OFFER_STATUS = "status"
        private const val COLUMN_OFFER_CREATED_AT = "created_at"
        private const val COLUMN_OFFER_RESPONDED_AT = "responded_at"
        private const val COLUMN_OFFER_CONVERSATION_ID = "conversation_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create users table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_PHONE TEXT,
                $COLUMN_USER_BIO TEXT,
                $COLUMN_USER_FIREBASE_UID TEXT,
                $COLUMN_USER_AVATAR_URL TEXT,
                $COLUMN_USER_IS_ACTIVE INTEGER DEFAULT 1
            )
        """.trimIndent()

        // Create products table
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_PRODUCT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PRODUCT_TITLE TEXT NOT NULL,
                $COLUMN_PRODUCT_DESCRIPTION TEXT,
                $COLUMN_PRODUCT_PRICE REAL NOT NULL,
                $COLUMN_PRODUCT_USER_ID INTEGER NOT NULL,
                $COLUMN_PRODUCT_CREATED_AT TEXT NOT NULL,
                $COLUMN_PRODUCT_STATUS TEXT DEFAULT 'Available',
                $COLUMN_PRODUCT_CATEGORY TEXT DEFAULT 'Khác',
                $COLUMN_PRODUCT_CONDITION TEXT DEFAULT 'Mới',
                $COLUMN_PRODUCT_CREATED_AT_TIMESTAMP INTEGER DEFAULT 0,
                $COLUMN_PRODUCT_IMAGE_PATHS TEXT,
                $COLUMN_PRODUCT_RATING REAL DEFAULT 0.0,
                $COLUMN_PRODUCT_RATING_COUNT INTEGER DEFAULT 0,
                $COLUMN_PRODUCT_LOCATION TEXT,
                $COLUMN_PRODUCT_LATITUDE REAL,
                $COLUMN_PRODUCT_LONGITUDE REAL,
                $COLUMN_PRODUCT_SYNCED INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_PRODUCT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        // Create blocked_users table
        val createBlockedUsersTable = """
            CREATE TABLE $TABLE_BLOCKED_USERS (
                $COLUMN_BLOCKED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_BLOCKER_EMAIL TEXT NOT NULL,
                $COLUMN_BLOCKED_EMAIL TEXT NOT NULL,
                $COLUMN_BLOCKED_AT INTEGER NOT NULL,
                UNIQUE($COLUMN_BLOCKER_EMAIL, $COLUMN_BLOCKED_EMAIL)
            )
        """.trimIndent()

        // Create price_offers table
        val createPriceOffersTable = """
            CREATE TABLE $TABLE_PRICE_OFFERS (
                $COLUMN_OFFER_ID TEXT PRIMARY KEY,
                $COLUMN_OFFER_PRODUCT_ID INTEGER NOT NULL,
                $COLUMN_OFFER_BUYER_EMAIL TEXT NOT NULL,
                $COLUMN_OFFER_SELLER_EMAIL TEXT NOT NULL,
                $COLUMN_OFFER_ORIGINAL_PRICE REAL NOT NULL,
                $COLUMN_OFFER_OFFERED_PRICE REAL NOT NULL,
                $COLUMN_OFFER_MESSAGE TEXT,
                $COLUMN_OFFER_STATUS TEXT DEFAULT 'PENDING',
                $COLUMN_OFFER_CREATED_AT INTEGER NOT NULL,
                $COLUMN_OFFER_RESPONDED_AT INTEGER,
                $COLUMN_OFFER_CONVERSATION_ID TEXT,
                FOREIGN KEY($COLUMN_OFFER_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PRODUCT_ID)
            )
        """.trimIndent()

        db?.execSQL(createUsersTable)
        db?.execSQL(createProductsTable)
        db?.execSQL(createBlockedUsersTable)
        db?.execSQL(createPriceOffersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add new columns to existing tables
            db?.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_PHONE TEXT")
            db?.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_BIO TEXT")
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_STATUS TEXT DEFAULT 'Available'")
        }
        if (oldVersion < 3) {
            // Add new columns for filtering and sorting
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_CATEGORY TEXT DEFAULT 'Khác'")
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_CONDITION TEXT DEFAULT 'Mới'")
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_CREATED_AT_TIMESTAMP INTEGER DEFAULT 0")
            
            // Update existing records with current timestamp
            db?.execSQL("UPDATE $TABLE_PRODUCTS SET $COLUMN_PRODUCT_CREATED_AT_TIMESTAMP = ${System.currentTimeMillis()} WHERE $COLUMN_PRODUCT_CREATED_AT_TIMESTAMP = 0")
        }
        if (oldVersion < 4) {
            // Add image paths column
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_IMAGE_PATHS TEXT")
        }
        if (oldVersion < 5) {
            // Add synced column for Firebase sync
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_SYNCED INTEGER DEFAULT 0")
        }
        if (oldVersion < 6) {
            // Add new User fields
            db?.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_FIREBASE_UID TEXT")
            db?.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_AVATAR_URL TEXT")
            db?.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_IS_ACTIVE INTEGER DEFAULT 1")
        }
        if (oldVersion < 7) {
            // Add rating columns for products
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_RATING REAL DEFAULT 0.0")
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_RATING_COUNT INTEGER DEFAULT 0")
        }
        if (oldVersion < 8) {
            // Add location columns for products
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_LOCATION TEXT")
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_LATITUDE REAL")
            db?.execSQL("ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_PRODUCT_LONGITUDE REAL")
        }
        if (oldVersion < 9) {
            // Create blocked_users table
            val createBlockedUsersTable = """
                CREATE TABLE $TABLE_BLOCKED_USERS (
                    $COLUMN_BLOCKED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_BLOCKER_EMAIL TEXT NOT NULL,
                    $COLUMN_BLOCKED_EMAIL TEXT NOT NULL,
                    $COLUMN_BLOCKED_AT INTEGER NOT NULL,
                    UNIQUE($COLUMN_BLOCKER_EMAIL, $COLUMN_BLOCKED_EMAIL)
                )
            """.trimIndent()
            db?.execSQL(createBlockedUsersTable)
        }
        if (oldVersion < 10) {
            // Create price_offers table
            val createPriceOffersTable = """
                CREATE TABLE $TABLE_PRICE_OFFERS (
                    $COLUMN_OFFER_ID TEXT PRIMARY KEY,
                    $COLUMN_OFFER_PRODUCT_ID INTEGER NOT NULL,
                    $COLUMN_OFFER_BUYER_EMAIL TEXT NOT NULL,
                    $COLUMN_OFFER_SELLER_EMAIL TEXT NOT NULL,
                    $COLUMN_OFFER_ORIGINAL_PRICE REAL NOT NULL,
                    $COLUMN_OFFER_OFFERED_PRICE REAL NOT NULL,
                    $COLUMN_OFFER_MESSAGE TEXT,
                    $COLUMN_OFFER_STATUS TEXT DEFAULT 'PENDING',
                    $COLUMN_OFFER_CREATED_AT INTEGER NOT NULL,
                    $COLUMN_OFFER_RESPONDED_AT INTEGER,
                    $COLUMN_OFFER_CONVERSATION_ID TEXT,
                    FOREIGN KEY($COLUMN_OFFER_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_PRODUCT_ID)
                )
            """.trimIndent()
            db?.execSQL(createPriceOffersTable)
        }
    }

    // User methods
    fun registerUser(email: String, password: String, name: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_NAME, name)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun loginUser(email: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)) ?: "",
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE)) ?: "",
                bio = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_BIO)) ?: "",
                firebaseUid = cursor.getString(cursor.getColumnIndex(COLUMN_USER_FIREBASE_UID)),
                avatarUrl = cursor.getString(cursor.getColumnIndex(COLUMN_USER_AVATAR_URL)),
                isActive = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_ACTIVE)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Product methods
    fun addProduct(
        title: String, 
        description: String, 
        price: Double, 
        userId: Long, 
        imagePaths: String? = null, 
        status: String = "Available", 
        category: String = "Khác", 
        condition: String = "Mới",
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Long {
        val db = writableDatabase
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val timestamp = System.currentTimeMillis()
        
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_TITLE, title)
            put(COLUMN_PRODUCT_DESCRIPTION, description)
            put(COLUMN_PRODUCT_PRICE, price)
            put(COLUMN_PRODUCT_USER_ID, userId)
            put(COLUMN_PRODUCT_CREATED_AT, currentTime)
            put(COLUMN_PRODUCT_STATUS, status)
            put(COLUMN_PRODUCT_CATEGORY, category)
            put(COLUMN_PRODUCT_CONDITION, condition)
            put(COLUMN_PRODUCT_CREATED_AT_TIMESTAMP, timestamp)
            put(COLUMN_PRODUCT_IMAGE_PATHS, imagePaths)
            put(COLUMN_PRODUCT_LOCATION, location)
            put(COLUMN_PRODUCT_LATITUDE, latitude)
            put(COLUMN_PRODUCT_LONGITUDE, longitude)
            put(COLUMN_PRODUCT_SYNCED, 0) // Mặc định chưa sync
        }
        return db.insert(TABLE_PRODUCTS, null, values)
    }

    // Helper function to create Product from cursor
    private fun createProductFromCursor(cursor: android.database.Cursor): Product {
        val imagePathsColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_IMAGE_PATHS)
        val imagePathsValue = if (cursor.isNull(imagePathsColumnIndex)) null else cursor.getString(imagePathsColumnIndex)
        
        val syncedColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_SYNCED)
        val synced = if (syncedColumnIndex >= 0) cursor.getInt(syncedColumnIndex) == 1 else false
        
        val ratingColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_RATING)
        val rating = if (ratingColumnIndex >= 0) cursor.getFloat(ratingColumnIndex) else 0f
        
        val ratingCountColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_RATING_COUNT)
        val ratingCount = if (ratingCountColumnIndex >= 0) cursor.getInt(ratingCountColumnIndex) else 0
        
        // Location columns
        val locationColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_LOCATION)
        val location = if (locationColumnIndex >= 0) cursor.getString(locationColumnIndex) else null
        
        val latitudeColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_LATITUDE)
        val latitude = if (latitudeColumnIndex >= 0 && !cursor.isNull(latitudeColumnIndex)) cursor.getDouble(latitudeColumnIndex) else null
        
        val longitudeColumnIndex = cursor.getColumnIndex(COLUMN_PRODUCT_LONGITUDE)
        val longitude = if (longitudeColumnIndex >= 0 && !cursor.isNull(longitudeColumnIndex)) cursor.getDouble(longitudeColumnIndex) else null
        
        return Product(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_DESCRIPTION)),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_PRICE)),
            userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_USER_ID)),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CREATED_AT)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_STATUS)) ?: "Available",
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CATEGORY)) ?: "Khác",
            condition = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CONDITION)) ?: "Mới",
            createdAtTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_CREATED_AT_TIMESTAMP)),
            imagePaths = imagePathsValue,
            rating = rating,
            ratingCount = ratingCount,
            location = location,
            latitude = latitude,
            longitude = longitude,
            synced = synced
        )
    }

    fun getAllProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            val product = createProductFromCursor(cursor)
            products.add(product)
        }
        cursor.close()
        return products
    }

    fun getProductById(productId: Int): Product? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PRODUCT_ID = ?",
            arrayOf(productId.toString()),
            null,
            null,
            null
        )

        var product: Product? = null
        if (cursor.moveToFirst()) {
            product = createProductFromCursor(cursor)
        }
        cursor.close()
        return product
    }

    fun getUserProducts(userId: Long): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PRODUCT_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            val product = createProductFromCursor(cursor)
            products.add(product)
        }
        cursor.close()
        return products
    }

    fun searchProducts(keyword: String, minPrice: Double? = null, maxPrice: Double? = null): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        
        var selection = "$COLUMN_PRODUCT_TITLE LIKE ?"
        val selectionArgs = mutableListOf("%$keyword%")

        if (minPrice != null) {
            selection += " AND $COLUMN_PRODUCT_PRICE >= ?"
            selectionArgs.add(minPrice.toString())
        }

        if (maxPrice != null) {
            selection += " AND $COLUMN_PRODUCT_PRICE <= ?"
            selectionArgs.add(maxPrice.toString())
        }

        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            selection,
            selectionArgs.toTypedArray(),
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            val product = createProductFromCursor(cursor)
            products.add(product)
        }
        cursor.close()
        return products
    }

    // New methods for extended functionality
    fun updateProduct(productId: Long, title: String, description: String, price: Double, status: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_TITLE, title)
            put(COLUMN_PRODUCT_DESCRIPTION, description)
            put(COLUMN_PRODUCT_PRICE, price)
            put(COLUMN_PRODUCT_STATUS, status)
            put(COLUMN_PRODUCT_SYNCED, 0) // Đánh dấu cần sync lại
        }
        return db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
    }

    fun deleteProduct(productId: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_PRODUCTS, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
    }

    fun markProductAsSold(productId: Long): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_STATUS, "Sold")
            put(COLUMN_PRODUCT_SYNCED, 0) // Đánh dấu cần sync lại
        }
        return db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
    }

    fun updateProductStatus(productId: Long, status: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_STATUS, status)
            put(COLUMN_PRODUCT_SYNCED, 0) // Đánh dấu cần sync lại
        }
        return db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
    }

    fun updateProductRating(productId: Long, newRating: Float): Int {
        val db = writableDatabase
        
        // Lấy rating hiện tại để tính toán rating trung bình mới
        val currentProduct = getProductById(productId.toInt())
        if (currentProduct != null) {
            val currentRating = currentProduct.rating
            val currentCount = currentProduct.ratingCount
            
            // Tính toán rating trung bình mới
            val totalRating = (currentRating * currentCount) + newRating
            val newCount = currentCount + 1
            val newAverageRating = totalRating / newCount
            
            val values = ContentValues().apply {
                put(COLUMN_PRODUCT_RATING, newAverageRating)
                put(COLUMN_PRODUCT_RATING_COUNT, newCount)
                put(COLUMN_PRODUCT_SYNCED, 0) // Đánh dấu cần sync lại
            }
            return db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
        }
        return 0
    }

    fun setProductRating(productId: Long, rating: Float, ratingCount: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_RATING, rating)
            put(COLUMN_PRODUCT_RATING_COUNT, ratingCount)
            put(COLUMN_PRODUCT_SYNCED, 0) // Đánh dấu cần sync lại
        }
        return db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
    }

    fun getUserProductsByStatus(userId: Long, status: String): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PRODUCT_USER_ID = ? AND $COLUMN_PRODUCT_STATUS = ?",
            arrayOf(userId.toString(), status),
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            val product = createProductFromCursor(cursor)
            products.add(product)
        }
        cursor.close()
        return products
    }

    fun getAllAvailableProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PRODUCT_STATUS = ?",
            arrayOf("Available"),
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            val product = createProductFromCursor(cursor)
            products.add(product)
        }
        cursor.close()
        return products
    }

    fun updateUserProfile(userId: Long, name: String, phone: String, bio: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_PHONE, phone)
            put(COLUMN_USER_BIO, bio)
        }
        return db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
    }

    fun getUserById(userId: Long): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)) ?: "",
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE)) ?: "",
                bio = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_BIO)) ?: "",
                firebaseUid = cursor.getString(cursor.getColumnIndex(COLUMN_USER_FIREBASE_UID)),
                avatarUrl = cursor.getString(cursor.getColumnIndex(COLUMN_USER_AVATAR_URL)),
                isActive = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_ACTIVE)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    fun searchProductsAdvanced(keyword: String, minPrice: Double? = null, maxPrice: Double? = null, status: String? = null): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        
        var selection = "$COLUMN_PRODUCT_TITLE LIKE ?"
        val selectionArgs = mutableListOf("%$keyword%")

        if (minPrice != null) {
            selection += " AND $COLUMN_PRODUCT_PRICE >= ?"
            selectionArgs.add(minPrice.toString())
        }

        if (maxPrice != null) {
            selection += " AND $COLUMN_PRODUCT_PRICE <= ?"
            selectionArgs.add(maxPrice.toString())
        }

        if (status != null) {
            selection += " AND $COLUMN_PRODUCT_STATUS = ?"
            selectionArgs.add(status)
        }

        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            selection,
            selectionArgs.toTypedArray(),
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            val product = createProductFromCursor(cursor)
            products.add(product)
        }
        cursor.close()
        return products
    }
    
    // New method for filtering and sorting products
    fun getFilteredAndSortedProducts(
        keyword: String = "",
        category: String? = null,
        condition: String? = null,
        status: String? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        sortBy: String = "newest",
        userId: Long? = null // null means all users, otherwise filter by user
    ): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        
        // Build WHERE clause
        val whereConditions = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()
        
        // Keyword search
        if (keyword.isNotBlank()) {
            whereConditions.add("$COLUMN_PRODUCT_TITLE LIKE ?")
            selectionArgs.add("%$keyword%")
        }
        
        // Category filter
        if (!category.isNullOrBlank() && category != "Tất cả") {
            whereConditions.add("$COLUMN_PRODUCT_CATEGORY = ?")
            selectionArgs.add(category)
        }
        
        // Condition filter
        if (!condition.isNullOrBlank() && condition != "Tất cả") {
            whereConditions.add("$COLUMN_PRODUCT_CONDITION = ?")
            selectionArgs.add(condition)
        }
        
        // Status filter
        if (!status.isNullOrBlank() && status != "Tất cả") {
            whereConditions.add("$COLUMN_PRODUCT_STATUS = ?")
            selectionArgs.add(status)
        }
        
        // Price range filter
        if (minPrice != null) {
            whereConditions.add("$COLUMN_PRODUCT_PRICE >= ?")
            selectionArgs.add(minPrice.toString())
        }
        
        if (maxPrice != null) {
            whereConditions.add("$COLUMN_PRODUCT_PRICE <= ?")
            selectionArgs.add(maxPrice.toString())
        }
        
        // User filter
        if (userId != null) {
            whereConditions.add("$COLUMN_PRODUCT_USER_ID = ?")
            selectionArgs.add(userId.toString())
        }
        
        // Build ORDER BY clause
        val orderBy = when (sortBy) {
            "newest" -> "$COLUMN_PRODUCT_CREATED_AT_TIMESTAMP DESC"
            "price_asc" -> "$COLUMN_PRODUCT_PRICE ASC"
            "price_desc" -> "$COLUMN_PRODUCT_PRICE DESC"
            else -> "$COLUMN_PRODUCT_CREATED_AT_TIMESTAMP DESC"
        }
        
        // Build complete WHERE clause
        val whereClause = if (whereConditions.isNotEmpty()) {
            whereConditions.joinToString(" AND ")
        } else null
        
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            whereClause,
            if (selectionArgs.isNotEmpty()) selectionArgs.toTypedArray() else null,
            null,
            null,
            orderBy
        )
        
        while (cursor.moveToNext()) {
            products.add(createProductFromCursor(cursor))
        }
        
        cursor.close()
        return products
    }
    
    // =============== FIREBASE SYNC METHODS ===============
    
    /**
     * Lấy danh sách các sản phẩm chưa được đồng bộ với Firebase
     */
    fun getUnsyncedProducts(): List<Product> {
        val products = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PRODUCT_SYNCED = ?",
            arrayOf("0"),
            null,
            null,
            null
        )
        
        while (cursor.moveToNext()) {
            products.add(createProductFromCursor(cursor))
        }
        
        cursor.close()
        return products
    }
    
    /**
     * Lấy danh sách sản phẩm của một người dùng theo ID
     */
    fun getProductsByUserId(userId: Long): List<Product> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COLUMN_PRODUCT_USER_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_PRODUCT_CREATED_AT_TIMESTAMP DESC"
        )

        val products = mutableListOf<Product>()
        while (cursor.moveToNext()) {
            products.add(createProductFromCursor(cursor))
        }
        
        cursor.close()
        return products
    }

    /**
     * Đánh dấu một sản phẩm đã được đồng bộ với Firebase
     */
    fun markProductAsSynced(productId: Long): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_SYNCED, 1)
        }
        
        val rowsAffected = db.update(
            TABLE_PRODUCTS,
            values,
            "$COLUMN_PRODUCT_ID = ?",
            arrayOf(productId.toString())
        )
        
        return rowsAffected > 0
    }
    
    /**
     * Đánh dấu một sản phẩm chưa được đồng bộ (khi cần sync lại)
     */
    fun markProductAsUnsynced(productId: Long): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_SYNCED, 0)
        }
        
        val rowsAffected = db.update(
            TABLE_PRODUCTS,
            values,
            "$COLUMN_PRODUCT_ID = ?",
            arrayOf(productId.toString())
        )
        
        return rowsAffected > 0
    }
    
    /**
     * Đếm số lượng sản phẩm chưa được đồng bộ
     */
    fun getUnsyncedProductsCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_PRODUCTS WHERE $COLUMN_PRODUCT_SYNCED = 0",
            null
        )
        
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        
        cursor.close()
        return count
    }
    
    /**
     * Thêm sản phẩm từ Firebase vào SQLite
     */
    fun addProductFromFirebase(product: Product): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_ID, product.id)
            put(COLUMN_PRODUCT_TITLE, product.title)
            put(COLUMN_PRODUCT_DESCRIPTION, product.description)
            put(COLUMN_PRODUCT_PRICE, product.price)
            put(COLUMN_PRODUCT_USER_ID, product.userId)
            put(COLUMN_PRODUCT_CREATED_AT, product.createdAt)
            put(COLUMN_PRODUCT_STATUS, product.status)
            put(COLUMN_PRODUCT_CATEGORY, product.category)
            put(COLUMN_PRODUCT_CONDITION, product.condition)
            put(COLUMN_PRODUCT_CREATED_AT_TIMESTAMP, product.createdAtTimestamp)
            put(COLUMN_PRODUCT_IMAGE_PATHS, product.imagePaths)
            put(COLUMN_PRODUCT_RATING, product.rating)
            put(COLUMN_PRODUCT_RATING_COUNT, product.ratingCount)
            put(COLUMN_PRODUCT_LOCATION, product.location)
            put(COLUMN_PRODUCT_LATITUDE, product.latitude)
            put(COLUMN_PRODUCT_LONGITUDE, product.longitude)
            put(COLUMN_PRODUCT_SYNCED, if (product.synced) 1 else 0)
        }
        
        return db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
    
    /**
     * Cập nhật sản phẩm từ Firebase
     */
    fun updateProductFromFirebase(product: Product): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_TITLE, product.title)
            put(COLUMN_PRODUCT_DESCRIPTION, product.description)
            put(COLUMN_PRODUCT_PRICE, product.price)
            put(COLUMN_PRODUCT_STATUS, product.status)
            put(COLUMN_PRODUCT_CATEGORY, product.category)
            put(COLUMN_PRODUCT_CONDITION, product.condition)
            put(COLUMN_PRODUCT_IMAGE_PATHS, product.imagePaths)
            put(COLUMN_PRODUCT_RATING, product.rating)
            put(COLUMN_PRODUCT_RATING_COUNT, product.ratingCount)
            put(COLUMN_PRODUCT_LOCATION, product.location)
            put(COLUMN_PRODUCT_LATITUDE, product.latitude)
            put(COLUMN_PRODUCT_LONGITUDE, product.longitude)
            put(COLUMN_PRODUCT_SYNCED, if (product.synced) 1 else 0)
        }
        
        return db.update(
            TABLE_PRODUCTS,
            values,
            "$COLUMN_PRODUCT_ID = ?",
            arrayOf(product.id.toString())
        )
    }
    
    // =============== MISSING METHODS FOR OTP ===============
    
    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)) ?: "",
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE)) ?: "",
                bio = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_BIO)) ?: "",
                firebaseUid = cursor.getString(cursor.getColumnIndex(COLUMN_USER_FIREBASE_UID)),
                avatarUrl = cursor.getString(cursor.getColumnIndex(COLUMN_USER_AVATAR_URL)),
                isActive = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_IS_ACTIVE)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }
    
    fun updatePassword(email: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_PASSWORD, newPassword)
        }
        val rowsAffected = db.update(
            TABLE_USERS, 
            values, 
            "$COLUMN_USER_EMAIL = ?", 
            arrayOf(email)
        )
        return rowsAffected > 0
    }
    
    fun registerUserWithFirebase(email: String, password: String, name: String, firebaseUid: String, avatarUrl: String? = null): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_FIREBASE_UID, firebaseUid)
            put(COLUMN_USER_AVATAR_URL, avatarUrl)
            put(COLUMN_USER_IS_ACTIVE, 1)
        }
        return db.insert(TABLE_USERS, null, values)
    }
    
    // Blocked Users methods
    /**
     * Chặn một người dùng
     */
    fun blockUser(blockerEmail: String, blockedEmail: String): Boolean {
        if (blockerEmail == blockedEmail) return false // Không thể tự chặn mình
        
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_BLOCKER_EMAIL, blockerEmail)
            put(COLUMN_BLOCKED_EMAIL, blockedEmail)
            put(COLUMN_BLOCKED_AT, System.currentTimeMillis())
        }
        return try {
            db.insertOrThrow(TABLE_BLOCKED_USERS, null, values) > 0
        } catch (e: Exception) {
            false // Có thể đã bị chặn trước đó (UNIQUE constraint)
        }
    }
    
    /**
     * Bỏ chặn một người dùng
     */
    fun unblockUser(blockerEmail: String, blockedEmail: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_BLOCKED_USERS,
            "$COLUMN_BLOCKER_EMAIL = ? AND $COLUMN_BLOCKED_EMAIL = ?",
            arrayOf(blockerEmail, blockedEmail)
        )
        return rowsDeleted > 0
    }
    
    /**
     * Kiểm tra xem một người dùng có bị chặn không
     */
    fun isUserBlocked(blockerEmail: String, blockedEmail: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BLOCKED_USERS,
            null,
            "$COLUMN_BLOCKER_EMAIL = ? AND $COLUMN_BLOCKED_EMAIL = ?",
            arrayOf(blockerEmail, blockedEmail),
            null,
            null,
            null
        )
        val isBlocked = cursor.count > 0
        cursor.close()
        return isBlocked
    }
    
    /**
     * Kiểm tra xem có quan hệ chặn 2 chiều không (A chặn B hoặc B chặn A)
     */
    fun isBlockedByEither(email1: String, email2: String): Boolean {
        return isUserBlocked(email1, email2) || isUserBlocked(email2, email1)
    }
    
    /**
     * Lấy danh sách người dùng bị chặn bởi một người
     */
    fun getBlockedUsers(blockerEmail: String): List<BlockedUser> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BLOCKED_USERS,
            null,
            "$COLUMN_BLOCKER_EMAIL = ?",
            arrayOf(blockerEmail),
            null,
            null,
            "$COLUMN_BLOCKED_AT DESC"
        )
        
        val blockedUsers = mutableListOf<BlockedUser>()
        while (cursor.moveToNext()) {
            val blockedUser = BlockedUser(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED_ID)),
                blockerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOCKER_EMAIL)),
                blockedEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED_EMAIL)),
                blockedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BLOCKED_AT))
            )
            blockedUsers.add(blockedUser)
        }
        cursor.close()
        return blockedUsers
    }
    
    // ================== PRICE OFFERS METHODS ==================
    
    /**
     * Tạo một đề nghị giá mới
     */
    fun createPriceOffer(priceOffer: PriceOffer): String? {
        // Tạo offerId an toàn cho Firebase
        val safeEmail = FirebaseUtils.sanitizeForFirebaseKey(priceOffer.buyerEmail)
        val offerId = "${priceOffer.productId}_${safeEmail}_${System.currentTimeMillis()}"
        
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_OFFER_ID, offerId)
            put(COLUMN_OFFER_PRODUCT_ID, priceOffer.productId)
            put(COLUMN_OFFER_BUYER_EMAIL, priceOffer.buyerEmail)
            put(COLUMN_OFFER_SELLER_EMAIL, priceOffer.sellerEmail)
            put(COLUMN_OFFER_ORIGINAL_PRICE, priceOffer.originalPrice)
            put(COLUMN_OFFER_OFFERED_PRICE, priceOffer.offeredPrice)
            put(COLUMN_OFFER_MESSAGE, priceOffer.message)
            put(COLUMN_OFFER_STATUS, priceOffer.status)
            put(COLUMN_OFFER_CREATED_AT, priceOffer.createdAt)
            put(COLUMN_OFFER_CONVERSATION_ID, priceOffer.conversationId)
        }
        
        return if (db.insert(TABLE_PRICE_OFFERS, null, values) > 0) offerId else null
    }
    
    /**
     * Cập nhật trạng thái đề nghị giá (chấp nhận/từ chối)
     */
    fun updatePriceOfferStatus(offerId: String, status: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_OFFER_STATUS, status)
            put(COLUMN_OFFER_RESPONDED_AT, System.currentTimeMillis())
        }
        
        val rowsAffected = db.update(
            TABLE_PRICE_OFFERS,
            values,
            "$COLUMN_OFFER_ID = ?",
            arrayOf(offerId)
        )
        return rowsAffected > 0
    }
    
    /**
     * Lấy đề nghị giá theo ID
     */
    fun getPriceOfferById(offerId: String): PriceOffer? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRICE_OFFERS,
            null,
            "$COLUMN_OFFER_ID = ?",
            arrayOf(offerId),
            null,
            null,
            null
        )
        
        return if (cursor.moveToFirst()) {
            val priceOffer = PriceOffer(
                offerId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ID)),
                productId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_PRODUCT_ID)),
                buyerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_BUYER_EMAIL)),
                sellerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_SELLER_EMAIL)),
                originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ORIGINAL_PRICE)),
                offeredPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_OFFERED_PRICE)),
                message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_MESSAGE)) ?: "",
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_STATUS)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CREATED_AT)),
                respondedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_RESPONDED_AT)).takeIf { it > 0 },
                conversationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CONVERSATION_ID)) ?: ""
            )
            cursor.close()
            priceOffer
        } else {
            cursor.close()
            null
        }
    }
    
    /**
     * Lấy tất cả đề nghị giá cho một sản phẩm
     */
    fun getPriceOffersForProduct(productId: Long): List<PriceOffer> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRICE_OFFERS,
            null,
            "$COLUMN_OFFER_PRODUCT_ID = ?",
            arrayOf(productId.toString()),
            null,
            null,
            "$COLUMN_OFFER_CREATED_AT DESC"
        )
        
        val priceOffers = mutableListOf<PriceOffer>()
        while (cursor.moveToNext()) {
            val priceOffer = PriceOffer(
                offerId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ID)),
                productId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_PRODUCT_ID)),
                buyerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_BUYER_EMAIL)),
                sellerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_SELLER_EMAIL)),
                originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ORIGINAL_PRICE)),
                offeredPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_OFFERED_PRICE)),
                message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_MESSAGE)) ?: "",
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_STATUS)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CREATED_AT)),
                respondedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_RESPONDED_AT)).takeIf { it > 0 },
                conversationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CONVERSATION_ID)) ?: ""
            )
            priceOffers.add(priceOffer)
        }
        cursor.close()
        return priceOffers
    }
    
    /**
     * Lấy đề nghị giá mà người dùng đã gửi
     */
    fun getPriceOffersBySender(buyerEmail: String): List<PriceOffer> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRICE_OFFERS,
            null,
            "$COLUMN_OFFER_BUYER_EMAIL = ?",
            arrayOf(buyerEmail),
            null,
            null,
            "$COLUMN_OFFER_CREATED_AT DESC"
        )
        
        val priceOffers = mutableListOf<PriceOffer>()
        while (cursor.moveToNext()) {
            val priceOffer = PriceOffer(
                offerId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ID)),
                productId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_PRODUCT_ID)),
                buyerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_BUYER_EMAIL)),
                sellerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_SELLER_EMAIL)),
                originalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_ORIGINAL_PRICE)),
                offeredPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OFFER_OFFERED_PRICE)),
                message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_MESSAGE)) ?: "",
                status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_STATUS)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CREATED_AT)),
                respondedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_OFFER_RESPONDED_AT)).takeIf { it > 0 },
                conversationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OFFER_CONVERSATION_ID)) ?: ""
            )
            priceOffers.add(priceOffer)
        }
        cursor.close()
        return priceOffers
    }
    
    /**
     * Kiểm tra xem người dùng có đề nghị giá đang chờ cho sản phẩm này không
     */
    fun hasPendingOfferForProduct(productId: Long, buyerEmail: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRICE_OFFERS,
            arrayOf(COLUMN_OFFER_ID),
            "$COLUMN_OFFER_PRODUCT_ID = ? AND $COLUMN_OFFER_BUYER_EMAIL = ? AND $COLUMN_OFFER_STATUS = ?",
            arrayOf(productId.toString(), buyerEmail, "PENDING"),
            null,
            null,
            null
        )
        
        val hasPending = cursor.count > 0
        cursor.close()
        return hasPending
    }
    
    /**
     * Xóa đề nghị giá (nếu cần)
     */
    fun deletePriceOffer(offerId: String): Boolean {
        val db = writableDatabase
        val rowsDeleted = db.delete(
            TABLE_PRICE_OFFERS,
            "$COLUMN_OFFER_ID = ?",
            arrayOf(offerId)
        )
        return rowsDeleted > 0
    }
    
    /**
     * Update product images in local database
     * @param productId The product ID
     * @param imagePaths JSON string containing image URLs
     */
    fun updateProductImages(productId: Int, imagePaths: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRODUCT_IMAGE_PATHS, imagePaths)
        }
        return db.update(TABLE_PRODUCTS, values, "$COLUMN_PRODUCT_ID = ?", arrayOf(productId.toString()))
    }
    
    /**
     * Update user avatar URL in local database
     * @param userId The user ID  
     * @param avatarUrl The new avatar URL
     */
    fun updateUserAvatar(userId: Int, avatarUrl: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_AVATAR_URL, avatarUrl)
        }
        return db.update(TABLE_USERS, values, "$COLUMN_USER_ID = ?", arrayOf(userId.toString()))
    }
}
