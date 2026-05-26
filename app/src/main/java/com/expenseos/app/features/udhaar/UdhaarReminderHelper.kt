package com.expenseos.app.features.udhaar

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expenseos.app.core.model.UdhaarEntry
import com.expenseos.app.core.model.UdhaarType

object UdhaarReminderHelper {

    fun sendWhatsAppReminder(context: Context, entry: UdhaarEntry) {
        val message = buildReminderMessage(entry)
        
        // WhatsApp URL scheme
        val uri = if (!entry.phoneNumber.isNullOrBlank()) {
            // Strip any non-digit chars except +
            val formattedPhone = entry.phoneNumber.filter { it.isDigit() || it == '+' }
            // Assuming Pakistani numbers usually start with 03..., format to +923...
            val finalPhone = if (formattedPhone.startsWith("0")) {
                "+92${formattedPhone.substring(1)}"
            } else {
                formattedPhone
            }
            Uri.parse("https://wa.me/$finalPhone?text=${Uri.encode(message)}")
        } else {
            // If no phone number is saved, open WhatsApp generic intent
            Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        // Attempt to restrict to WhatsApp if installed
        intent.setPackage("com.whatsapp")
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // If WhatsApp is not installed, fallback to general view
            intent.setPackage(null)
            context.startActivity(intent)
        }
    }

    private fun buildReminderMessage(entry: UdhaarEntry): String {
        return if (entry.type == UdhaarType.GIVEN) {
            """
            As-salamu alaykum ${entry.contactName}! 👋
            
            Hope you're doing well. Just a quick reminder about the Udhaar (PKR ${entry.amount}) ${entry.note?.let { "for $it" } ?: ""}.
            
            Let me know when you can settle this. No rush, just keeping the accounts clear! 😊
            
            Sent via Expense OS 📱
            """.trimIndent()
        } else {
            """
            As-salamu alaykum ${entry.contactName}! 👋
            
            Just checking in to let you know I haven't forgotten the PKR ${entry.amount} I owe you ${entry.note?.let { "for $it" } ?: ""}.
            
            I'll be settling this soon. Thanks for your patience! 🙏
            
            Sent via Expense OS 📱
            """.trimIndent()
        }
    }
}
