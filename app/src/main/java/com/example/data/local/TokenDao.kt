package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {
    @Query("SELECT * FROM bearer_tokens ORDER BY id DESC")
    fun getAllTokensFlow(): Flow<List<BearerTokenEntity>>

    @Query("SELECT * FROM bearer_tokens WHERE isActive = 1 LIMIT 1")
    fun getActiveTokenFlow(): Flow<BearerTokenEntity?>

    @Query("SELECT * FROM bearer_tokens WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTokenDirect(): BearerTokenEntity?

    @Query("SELECT * FROM bearer_tokens WHERE id = :id")
    suspend fun getTokenById(id: Long): BearerTokenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: BearerTokenEntity): Long

    @Update
    suspend fun updateToken(token: BearerTokenEntity)

    @Query("DELETE FROM bearer_tokens WHERE id = :id")
    suspend fun deleteTokenById(id: Long)

    @Query("UPDATE bearer_tokens SET isActive = 0")
    suspend fun deactivateAllTokens()

    @Transaction
    suspend fun setActiveToken(id: Long) {
        deactivateAllTokens()
        @Suppress("UnnecessaryVariable")
        val activeQuery = "UPDATE bearer_tokens SET isActive = 1 WHERE id = $id"
        // Update directly via query or fetched entity
    }

    @Query("UPDATE bearer_tokens SET isActive = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setSingleActiveToken(id: Long)
}
