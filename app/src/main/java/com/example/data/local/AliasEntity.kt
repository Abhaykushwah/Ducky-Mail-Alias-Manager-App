package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aliases")
data class AliasEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String, // e.g., "x8k2m9q4@duck.com"
    val tokenId: Long = 0, // ID of token used to create it
    val tokenLabel: String = "Default Account",
    val serviceLabel: String, // e.g., "Netflix", "GitHub", "Shopping"
    val note: String = "", // Optional user notes
    val status: String = "ACTIVE", // "ACTIVE" or "DEACTIVATED"
    val createdAt: Long = System.currentTimeMillis(),
    val copyCount: Int = 0
)
