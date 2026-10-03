package com.grabdriver.driverprofit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.grabdriver.driverprofit.ui.dashboard.DashboardScreen
import com.grabdriver.driverprofit.ui.dashboard.DashboardViewModel
import com.grabdriver.driverprofit.ui.dashboard.DashboardViewModelFactory
import com.grabdriver.driverprofit.ui.theme.DriverProfitTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory((application as DriverProfitApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            DriverProfitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
