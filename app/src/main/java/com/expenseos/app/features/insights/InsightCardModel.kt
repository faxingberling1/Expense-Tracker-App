package com.expenseos.app.features.insights

enum class InsightType {
    INFO,
    WARNING,
    CELEBRATION,
    TIP
}

data class InsightCardModel(
    val id: String,
    val title: String,
    val body: String,
    val type: InsightType,
    val actionLabel: String? = null,
    val actionRoute: String? = null
)
