package student.projects.calotrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FoodLogDao { //(Lackner, 2023)

    @Insert
    suspend fun insert(food: OfflineFoodLog): Long //(Lackner, 2023)

    @Query("SELECT * FROM food_log ORDER BY timestamp ASC") //(Lackner, 2023)
    suspend fun getAll(): List<OfflineFoodLog>

    @Query("SELECT * FROM food_log WHERE synced = 0 ORDER BY timestamp ASC") //(Lackner, 2023)
    suspend fun getUnsynced(): List<OfflineFoodLog>

    @Query("DELETE FROM food_log WHERE id = :id") //(Lackner, 2023)
    suspend fun deleteById(id: Int) //(Lackner, 2023)
}


