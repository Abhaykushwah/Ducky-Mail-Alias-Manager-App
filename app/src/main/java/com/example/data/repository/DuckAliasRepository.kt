package com.example.data.repository

import com.example.data.local.AliasDao
import com.example.data.local.AliasEntity
import com.example.data.local.BearerTokenEntity
import com.example.data.local.TokenDao
import com.example.data.remote.DuckApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed class GenerationResult {
    data class Success(val alias: AliasEntity, val isRealApi: Boolean) : GenerationResult()
    data class Error(val message: String) : GenerationResult()
}

class DuckAliasRepository(
    private val tokenDao: TokenDao,
    private val aliasDao: AliasDao
) {
    private val apiService: DuckApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://quack.duckduckgo.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(DuckApiService::class.java)
    }

    val allTokensFlow: Flow<List<BearerTokenEntity>> = tokenDao.getAllTokensFlow()
    val activeTokenFlow: Flow<BearerTokenEntity?> = tokenDao.getActiveTokenFlow()
    val allAliasesFlow: Flow<List<AliasEntity>> = aliasDao.getAllAliasesFlow()
    val totalAliasCountFlow: Flow<Int> = aliasDao.getTotalAliasCountFlow()
    val activeAliasCountFlow: Flow<Int> = aliasDao.getActiveAliasCountFlow()

    fun searchAliasesFlow(query: String, statusFilter: String): Flow<List<AliasEntity>> {
        return aliasDao.searchAliasesFlow(query, statusFilter)
    }

    suspend fun generateNewAlias(
        serviceLabel: String,
        note: String,
        targetTokenId: Long? = null
    ): GenerationResult {
        val selectedToken = if (targetTokenId != null) {
            tokenDao.getTokenById(targetTokenId) ?: tokenDao.getActiveTokenDirect()
        } else {
            tokenDao.getActiveTokenDirect()
        }
        val tokenValue = selectedToken?.tokenValue?.trim() ?: ""
        val tokenLabel = selectedToken?.label ?: "Default Account"
        val tokenId = selectedToken?.id ?: 0L

        var generatedAddress: String? = null
        var isRealApi = false
        var errorMessage: String? = null

        if (tokenValue.isNotEmpty()) {
            try {
                val bearerHeader = if (tokenValue.startsWith("Bearer ", ignoreCase = true)) {
                    tokenValue
                } else {
                    "Bearer $tokenValue"
                }
                val response = apiService.generateDuckAddress(authorization = bearerHeader)
                if (response.isSuccessful && response.body() != null) {
                    val rawAddress = response.body()?.address
                    if (!rawAddress.isNullOrBlank()) {
                        generatedAddress = if (rawAddress.endsWith("@duck.com")) {
                            rawAddress
                        } else {
                            "$rawAddress@duck.com"
                        }
                        isRealApi = true
                    }
                } else {
                    errorMessage = "DuckDuckGo API returned code ${response.code()}. Using local generator."
                }
            } catch (e: Exception) {
                errorMessage = "Network exception: ${e.localizedMessage}. Using offline generator."
            }
        }

        // Fallback / Local generation if no valid address received from remote API
        if (generatedAddress == null) {
            val randomSegment = UUID.randomUUID().toString().replace("-", "").take(8).lowercase()
            generatedAddress = "duck_$randomSegment@duck.com"
        }

        val newAlias = AliasEntity(
            address = generatedAddress,
            tokenId = tokenId,
            tokenLabel = tokenLabel,
            serviceLabel = serviceLabel.ifBlank { "Online Service" },
            note = note,
            status = "ACTIVE",
            createdAt = System.currentTimeMillis()
        )

        val id = aliasDao.insertAlias(newAlias)
        val savedAlias = newAlias.copy(id = id)

        return if (isRealApi) {
            GenerationResult.Success(savedAlias, isRealApi = true)
        } else {
            // Success via fallback generator, optionally note if there was a token issue
            GenerationResult.Success(savedAlias, isRealApi = false)
        }
    }

    suspend fun toggleAliasStatus(alias: AliasEntity) {
        val newStatus = if (alias.status == "ACTIVE") "DEACTIVATED" else "ACTIVE"
        aliasDao.updateAliasStatus(alias.id, newStatus)
    }

    suspend fun updateAlias(alias: AliasEntity) {
        aliasDao.updateAlias(alias)
    }

    suspend fun incrementCopyCount(aliasId: Long) {
        aliasDao.incrementCopyCount(aliasId)
    }

    suspend fun deleteAlias(aliasId: Long) {
        aliasDao.deleteAliasById(aliasId)
    }

    suspend fun deleteAliases(aliasIds: List<Long>) {
        if (aliasIds.isNotEmpty()) {
            aliasDao.deleteAliasesByIds(aliasIds)
        }
    }

    // Token Management
    suspend fun addToken(label: String, tokenValue: String, makeActive: Boolean = true) {
        if (makeActive) {
            tokenDao.deactivateAllTokens()
        }
        val newToken = BearerTokenEntity(
            label = label.ifBlank { "Duck Protection Account" },
            tokenValue = tokenValue,
            isActive = makeActive
        )
        val id = tokenDao.insertToken(newToken)
        if (makeActive) {
            tokenDao.setSingleActiveToken(id)
        }
    }

    suspend fun updateToken(tokenId: Long, label: String, tokenValue: String) {
        val existing = tokenDao.getTokenById(tokenId)
        if (existing != null) {
            val updated = existing.copy(
                label = label.ifBlank { "Duck Protection Account" },
                tokenValue = tokenValue
            )
            tokenDao.updateToken(updated)
        }
    }

    suspend fun selectActiveToken(tokenId: Long) {
        tokenDao.setSingleActiveToken(tokenId)
    }

    suspend fun deleteToken(tokenId: Long) {
        tokenDao.deleteTokenById(tokenId)
    }

    // Seed sample data if database is empty on first launch
    suspend fun seedSampleDataIfEmpty() {
        val tokens = tokenDao.getAllTokensFlow().firstOrNull()
        if (tokens.isNullOrEmpty()) {
            val defaultToken = BearerTokenEntity(
                label = "Personal DDG Account",
                tokenValue = "duck_bearer_demo_sample_key_98231",
                isActive = true
            )
            val tokenId = tokenDao.insertToken(defaultToken)
            tokenDao.setSingleActiveToken(tokenId)

            // Seed initial aliases
            val sampleAliases = listOf(
                AliasEntity(
                    address = "px9k2m4a@duck.com",
                    tokenId = tokenId,
                    tokenLabel = "Personal DDG Account",
                    serviceLabel = "Netflix",
                    note = "For streaming TV and movies setup",
                    status = "ACTIVE",
                    createdAt = System.currentTimeMillis() - 86400000 * 3,
                    copyCount = 4
                ),
                AliasEntity(
                    address = "ddg_f8a2c1@duck.com",
                    tokenId = tokenId,
                    tokenLabel = "Personal DDG Account",
                    serviceLabel = "GitHub",
                    note = "Developer account sign-up",
                    status = "ACTIVE",
                    createdAt = System.currentTimeMillis() - 86400000 * 7,
                    copyCount = 12
                ),
                AliasEntity(
                    address = "duck_90x1z3@duck.com",
                    tokenId = tokenId,
                    tokenLabel = "Personal DDG Account",
                    serviceLabel = "Proton Mail Newsletter",
                    note = "Privacy updates and tech digest",
                    status = "DEACTIVATED",
                    createdAt = System.currentTimeMillis() - 86400000 * 15,
                    copyCount = 1
                )
            )
            sampleAliases.forEach { aliasDao.insertAlias(it) }
        }
    }
}


