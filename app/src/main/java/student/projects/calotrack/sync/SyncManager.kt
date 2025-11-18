package student.projects.calotrack.sync

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.calotrack.api.ApiClient
import student.projects.calotrack.data.AppDatabase
import student.projects.calotrack.data.OfflineFoodLog
import student.projects.calotrack.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SyncManager {
    private const val TAG = "SyncManager"

    fun syncOfflineFoods(context: Context) { //(Lackner, 2023)
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val unsynced = db.foodLogDao().getUnsynced()
                if (unsynced.isEmpty()) return@launch

                val byUser = unsynced.groupBy { it.userId }
                byUser.forEach { (userId, entries) ->
                    ApiClient.instance.getUser(userId).enqueue(object : Callback<User> {
                        override fun onResponse(call: Call<User>, response: Response<User>) {
                            val user = response.body() ?: run {
                                Log.w(TAG, "User $userId fetch returned null")
                                return
                            }
                            val updatedFoodLog = user.food_log.toMutableMap() //(Lackner, 2023)
                            entries.forEach { offline ->
                                val map = mutableMapOf<String, Any>(
                                    "name" to offline.name,
                                    "calories" to offline.calories,
                                    "category" to offline.category
                                )
                                offline.photoUrl?.let { map["photoUrl"] = it }
                                updatedFoodLog[offline.timestamp.toString()] = map
                            }

                            val updatedUser = user.copy(food_log = updatedFoodLog)
                            ApiClient.instance.updateUser(userId, updatedUser) //(Lackner, 2023)
                                .enqueue(object : Callback<Map<String, String>> {
                                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            entries.forEach { db.foodLogDao().deleteById(it.id) }
                                            Log.i(TAG, "Synced ${entries.size} entries for $userId")
                                        }
                                    }

                                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) { //(Lackner, 2023)
                                        Log.e(TAG, "Failed to push updates for user $userId: ${t.message}")
                                    }
                                })
                        }

                        override fun onFailure(call: Call<User>, t: Throwable) { //(Lackner, 2023)
                            Log.e(TAG, "Failed to fetch user $userId: ${t.message}")
                        }
                    })
                }
            } catch (e: Exception) { //(Lackner, 2023)
                Log.e(TAG, "Sync error: ${e.message}")
            }
        }
    }
}
