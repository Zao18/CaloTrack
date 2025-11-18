package student.projects.calotrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OfflineFoodLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao

    companion object { //(Lackner, 2023)
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "calotrack_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
    }
}
