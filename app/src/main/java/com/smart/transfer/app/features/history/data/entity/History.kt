package com.smart.transfer.app.com.smart.transfer.app.features.history.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val fileType: String,
    val tag: String,
    val from: String,
    val timestamp: Long
)
