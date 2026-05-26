package com.expenseos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.expenseos.app.ui.ExpenseOsApp
import com.expenseos.app.ui.theme.ExpenseOsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseOsTheme {
                ExpenseOsApp()
            }
        }
    }
}
