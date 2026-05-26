package com.expenseos.app.features.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.expenseos.app.ExpenseOsApplication
import com.expenseos.app.core.model.TransactionStatus
import com.expenseos.app.features.parsing.SmsTransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.util.UUID

class SmsBroadcastReceiver : BroadcastReceiver() {

    private val parser = SmsTransactionParser()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            val app = context.applicationContext as ExpenseOsApplication
            val repository = app.repository
            val notificationHelper = SmsNotificationHelper(context)

            for (sms in messages) {
                val body = sms.displayMessageBody
                
                // Real implementation would also filter by sender (e.g. 8583, 8287, HBL, Meezan)
                // We'll run all SMS through the parser
                
                val candidate = parser.parse(body)
                if (candidate != null) {
                    val finalCandidate = candidate.copy(
                        id = UUID.randomUUID().toString(),
                        occurredAt = ZonedDateTime.now(),
                        status = TransactionStatus.SUGGESTED
                    )

                    // Fire Coroutine to save to DB
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.insertTransaction(finalCandidate)
                        notificationHelper.showTransactionDetectedNotification(finalCandidate)
                    }
                }
            }
        }
    }
}
