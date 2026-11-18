package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity storing chat messages locally for instant offline/online display and sync.
 */
@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["chatId", "timestampMillis"])]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val memberUidsJoined: String,
    val messageContent: String,
    val messageType: String = "TEXT",
    val mediaUri: String = "",
    val amount: Double = 0.0,
    val giftName: String = "",
    val replyToText: String = "",
    val reaction: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = true,
    val isPendingOffline: Boolean = false
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestampMillis ASC")
    fun observeMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestampMillis DESC LIMIT 100")
    fun observeRecentMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Entity(tableName = "pending_messages")
data class PendingMessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val memberUidsJoined: String,
    val text: String,
    val messageType: String,
    val mediaUri: String = "",
    val amount: Double = 0.0,
    val giftName: String = "",
    val replyToText: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Dao
interface PendingMessageDao {
    @Query("SELECT * FROM pending_messages WHERE chatId = :chatId ORDER BY createdAtMillis ASC")
    fun observeForChat(chatId: String): Flow<List<PendingMessageEntity>>

    @Query("SELECT * FROM pending_messages ORDER BY createdAtMillis ASC")
    suspend fun getAllPending(): List<PendingMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: PendingMessageEntity)

    @Query("DELETE FROM pending_messages WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Database(
    entities = [PendingMessageEntity::class, ChatMessageEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PglobDatabase : RoomDatabase() {
    abstract fun pendingMessageDao(): PendingMessageDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: PglobDatabase? = null

        fun getInstance(context: Context): PglobDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PglobDatabase::class.java,
                    "pglob_local.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
