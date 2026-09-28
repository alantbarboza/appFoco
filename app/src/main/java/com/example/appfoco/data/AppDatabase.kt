package com.example.appfoco.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appfoco.data.dao.AppDao
import com.example.appfoco.data.entity.AppSettingsEntity
import com.example.appfoco.data.entity.DeathRecordEntity
import com.example.appfoco.data.entity.ForbiddenRuleEntity
import com.example.appfoco.data.entity.HabitEntity
import com.example.appfoco.data.entity.HabitLogEntity
import com.example.appfoco.data.entity.MilestoneEntity
import com.example.appfoco.data.entity.OfensivaEntity
import com.example.appfoco.data.entity.PetEntity
import com.example.appfoco.data.entity.RuleLogEntity
import com.example.appfoco.data.entity.TaskEntity

@Database(
    entities = [
        OfensivaEntity::class,
        PetEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        TaskEntity::class,
        ForbiddenRuleEntity::class,
        RuleLogEntity::class,
        DeathRecordEntity::class,
        MilestoneEntity::class,
        AppSettingsEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_foco_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT OR REPLACE INTO app_settings (id, selectedWidgetOfensivaId, hasInitialSeedBeenDone, habitReminderTime, taskReminderTime, isDevModeActive, isReminderEnabled, totalLostOfensivasCount) VALUES (1, 1, 0, '08:00', '20:00', 0, 1, 0)")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
