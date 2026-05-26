package com.expenseos.app.core.db

import com.expenseos.app.core.db.entities.RewardEventEntity
import com.expenseos.app.core.db.entities.TransactionEntity
import com.expenseos.app.core.db.entities.UdhaarEntity
import com.expenseos.app.core.model.Category
import com.expenseos.app.features.rewards.RewardEventType
import com.expenseos.app.core.model.TransactionCandidate
import com.expenseos.app.core.model.TransactionDirection
import com.expenseos.app.core.model.TransactionSource
import com.expenseos.app.core.model.TransactionStatus
import com.expenseos.app.core.model.UdhaarEntry
import com.expenseos.app.core.model.UdhaarType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class AppRepository(private val database: AppDatabase) {

    // --- Transactions ---

    fun observeAllTransactions(): Flow<List<TransactionCandidate>> {
        return database.transactionDao().observeAll().map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun insertTransaction(transaction: TransactionCandidate) {
        database.transactionDao().insert(transaction.toEntity())
    }

    suspend fun updateTransactionStatus(id: String, status: TransactionStatus) {
        database.transactionDao().updateStatus(id, status.name)
    }
    
    suspend fun deleteTransaction(id: String) {
        database.transactionDao().delete(id)
    }

    // --- Udhaar ---

    fun observeActiveUdhaar(): Flow<List<UdhaarEntry>> {
        return database.udhaarDao().observeActive().map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun insertUdhaar(entry: UdhaarEntry) {
        database.udhaarDao().insert(entry.toEntity())
    }

    suspend fun markUdhaarSettled(id: String) {
        database.udhaarDao().markSettled(id, System.currentTimeMillis())
    }

    // --- Rewards ---

    fun observeTotalPoints(): Flow<Int> {
        return database.rewardDao().observeTotalPoints().map { it ?: 0 }
    }

    suspend fun addRewardEvent(eventType: RewardEventType, points: Int, description: String) {
        val event = RewardEventEntity(
            id = UUID.randomUUID().toString(),
            eventType = eventType.name,
            points = points,
            description = description,
            timestamp = System.currentTimeMillis()
        )
        database.rewardDao().insert(event)
    }

    // --- Mappers ---

    private fun TransactionEntity.toModel() = TransactionCandidate(
        id = id,
        amount = amount,
        currencyCode = currencyCode,
        direction = TransactionDirection.valueOf(direction),
        merchant = merchant,
        category = Category.valueOf(category),
        source = TransactionSource.valueOf(source),
        occurredAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(occurredAt), ZoneId.systemDefault()),
        confidence = confidence,
        rawText = rawText,
        status = TransactionStatus.valueOf(status)
    )

    private fun TransactionCandidate.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        currencyCode = currencyCode,
        direction = direction.name,
        merchant = merchant,
        category = category.name,
        source = source.name,
        occurredAt = occurredAt.toInstant().toEpochMilli(),
        confidence = confidence,
        rawText = rawText,
        status = status.name
    )

    private fun UdhaarEntity.toModel() = UdhaarEntry(
        id = id,
        contactName = contactName,
        phoneNumber = phoneNumber,
        amount = amount,
        type = UdhaarType.valueOf(type),
        note = note,
        dueDate = dueDate?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        isSettled = isSettled,
        createdAt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        settledAt = settledAt?.let { ZonedDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) }
    )

    private fun UdhaarEntry.toEntity() = UdhaarEntity(
        id = id,
        contactName = contactName,
        phoneNumber = phoneNumber,
        amount = amount,
        type = type.name,
        note = note,
        dueDate = dueDate?.toInstant()?.toEpochMilli(),
        isSettled = isSettled,
        createdAt = createdAt.toInstant().toEpochMilli(),
        settledAt = settledAt?.toInstant()?.toEpochMilli()
    )
}
