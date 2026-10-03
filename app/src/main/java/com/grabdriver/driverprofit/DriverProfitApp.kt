package com.grabdriver.driverprofit

import android.app.Application
import com.grabdriver.driverprofit.data.local.AppDatabase
import com.grabdriver.driverprofit.data.repository.TripRepository
import com.grabdriver.driverprofit.data.repository.TripRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DriverProfitApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository: TripRepository by lazy { TripRepositoryImpl(database) }
}
