package com.expenseos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.expenseos.app.ui.ExpenseOsApp
import com.expenseos.app.ui.ExpenseOsViewModel
import com.expenseos.app.ui.ExpenseOsViewModelFactory
import com.expenseos.app.ui.theme.ExpenseOsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ExpenseOsApplication
        val viewModelFactory = ExpenseOsViewModelFactory(app.repository)

        setContent {
            ExpenseOsTheme {
                val viewModel: ExpenseOsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
                ExpenseOsApp(viewModel = viewModel)
            }
        }
    }
}
