package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AliasDao {
    @Query("SELECT * FROM aliases ORDER BY createdAt DESC")
    fun getAllAliasesFlow(): Flow<List<AliasEntity>>

    @Query("SELECT * FROM aliases WHERE id = :id")
    suspend fun getAliasById(id: Long): AliasEntity?

    @Query("""
        SELECT * FROM aliases 
        WHERE (serviceLabel LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR tokenLabel LIKE '%' || :query || '%')
        AND (:statusFilter = 'ALL' OR status = :statusFilter)
        ORDER BY createdAt DESC
    """)
    fun searchAliasesFlow(query: String, statusFilter: String): Flow<List<AliasEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: AliasEntity): Long

    @Update
    suspend fun updateAlias(alias: AliasEntity)

    @Query("UPDATE aliases SET status = :status WHERE id = :id")
    suspend fun updateAliasStatus(id: Long, status: String)

    @Query("UPDATE aliases SET copyCount = copyCount + 1 WHERE id = :id")
    suspend fun incrementCopyCount(id: Long)

    @Query("DELETE FROM aliases WHERE id = :id")
    suspend fun deleteAliasById(id: Long)

    @Query("DELETE FROM aliases WHERE id IN (:ids)")
    suspend fun deleteAliasesByIds(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM aliases")
    fun getTotalAliasCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM aliases WHERE status = 'ACTIVE'")
    fun getActiveAliasCountFlow(): Flow<Int>
}
