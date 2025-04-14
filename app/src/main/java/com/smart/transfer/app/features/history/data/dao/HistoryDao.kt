package com.smart.transfer.app.com.smart.transfer.app.features.history.data.dao

import androidx.room.*
import com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity.History

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Query("SELECT * FROM history WHERE tag = :tag AND `from` = :fromUser")
    suspend fun getHistoryByTagAndFrom(tag: String, fromUser: String): List<History>
    @Query("SELECT * FROM history WHERE tag = :tag OR `from` = :from")
    suspend fun getHistoryByTagOrFrom(tag: String, from: String): List<History>

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistory(id: Int)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): kotlinx.coroutines.flow.Flow<List<History>>

    @Query("SELECT * FROM history WHERE tag = :tag AND `from` = :from ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPaginatedHistory(tag: String, from: String, limit: Int, offset: Int): List<History>

}
