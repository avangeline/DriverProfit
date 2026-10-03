package com.grabdriver.driverprofit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grabdriver.driverprofit.data.local.dao.DailyFuelDao
import com.grabdriver.driverprofit.data.local.dao.TripDao
import com.grabdriver.driverprofit.data.local.dao.VehicleDao
import com.grabdriver.driverprofit.data.local.entity.DailyFuelSettingEntity
import com.grabdriver.driverprofit.data.local.entity.TripRecordEntity
import com.grabdriver.driverprofit.data.local.entity.VehicleProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        VehicleProfileEntity::class,
        DailyFuelSettingEntity::class,
        TripRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun dailyFuelDao(): DailyFuelDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "driver_profit_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // เพิ่มข้อมูลเริ่มต้นสำหรับรุ่นรถ
                val wave = VehicleProfileEntity(
                    name = "Honda Wave 110i",
                    vehicleType = "MOTORCYCLE",
                    defaultKmL = 52.0,
                    idleLiterPerHour = 0.12,
                    isSelected = true
                )
                val pcx = VehicleProfileEntity(
                    name = "Honda PCX 160",
                    vehicleType = "MOTORCYCLE",
                    defaultKmL = 42.0,
                    idleLiterPerHour = 0.16,
                    isSelected = false
                )
                val ecoCar = VehicleProfileEntity(
                    name = "Eco Car (1.0L - 1.2L)",
                    vehicleType = "CAR",
                    defaultKmL = 18.0,
                    idleLiterPerHour = 0.65,
                    isSelected = false
                )
                db.vehicleDao().insertVehicle(wave)
                db.vehicleDao().insertVehicle(pcx)
                db.vehicleDao().insertVehicle(ecoCar)

                // เพิ่มราคาน้ำมันของวันนี้เป็นค่าเริ่มต้น
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val initialFuel = DailyFuelSettingEntity(
                    dateString = todayStr,
                    fuelPricePerLiter = 38.50,
                    fuelType = "Gasohol 95"
                )
                db.dailyFuelDao().insertOrUpdateFuelSetting(initialFuel)
            }
        }
    }
}
