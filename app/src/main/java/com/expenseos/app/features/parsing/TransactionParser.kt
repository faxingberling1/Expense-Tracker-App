package com.expenseos.app.features.parsing

import com.expenseos.app.core.model.TransactionCandidate

interface TransactionParser {
    fun parse(rawText: String): TransactionCandidate?
}

