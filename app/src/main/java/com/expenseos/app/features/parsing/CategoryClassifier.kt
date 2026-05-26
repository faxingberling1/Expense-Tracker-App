package com.expenseos.app.features.parsing

import com.expenseos.app.core.model.Category

class CategoryClassifier {
    fun classify(text: String): Category {
        val normalized = text.lowercase()
        return when {
            listOf("foodpanda", "restaurant", "lunch", "dinner", "khana").any(normalized::contains) -> Category.FOOD
            listOf("grocery", "mart", "imtiyaz", "metro", "alfatah").any(normalized::contains) -> Category.GROCERIES
            listOf("careem", "uber", "ride", "fare", "kiraya").any(normalized::contains) -> Category.TRANSPORT
            listOf("fuel", "petrol", "shell", "total", "pso").any(normalized::contains) -> Category.FUEL
            listOf("netflix", "spotify", "icloud", "youtube").any(normalized::contains) -> Category.SUBSCRIPTIONS
            listOf("daraz", "shopping", "order").any(normalized::contains) -> Category.SHOPPING
            listOf("bill", "electricity", "gas", "ptcl", "ke").any(normalized::contains) -> Category.BILLS
            listOf("received", "salary", "credited", "mila").any(normalized::contains) -> Category.INCOME
            listOf("sent", "transfer", "bheja", "ibft").any(normalized::contains) -> Category.TRANSFERS
            else -> Category.OTHER
        }
    }
}
