package student.projects.calotrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_log")
data class OfflineFoodLog( //(Lackner, 2023)
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val name: String,
    val calories: Int,
    val category: String,
    val photoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

