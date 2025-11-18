package student.projects.calotrack

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import student.projects.calotrack.api.ApiClient
import student.projects.calotrack.data.AppDatabase
import student.projects.calotrack.data.OfflineFoodLog
import student.projects.calotrack.models.BuiltInFoods
import student.projects.calotrack.models.FoodItem
import student.projects.calotrack.models.User
import student.projects.calotrack.utils.NetworkMonitor
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var foodLogListView: ListView
    private lateinit var adapter: FoodAdapter
    private val foodLogList = mutableListOf<FoodItem>()
    private var totalCalories = 0f

    private lateinit var totalCaloriesText: TextView
    private lateinit var targetCaloriesText: TextView
    private lateinit var remainingCaloriesText: TextView
    private lateinit var consumedCaloriesText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var networkMonitor: NetworkMonitor

    private val addFoodLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) loadUserData()
    }

    // Locale handling
    private val prefs by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }
    private val LANG_PREF_KEY = "language_code"
    private val LOCALE_CHANGED_KEY = "locale_changed_pending"

    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE)
            .getString(LANG_PREF_KEY, "en") ?: "en"
        super.attachBaseContext(updateLocale(newBase, lang))
    }

    private fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config: Configuration = resources.configuration

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
            context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        foodLogListView = findViewById(R.id.foodLogList)
        totalCaloriesText = findViewById(R.id.totalCalories)
        targetCaloriesText = findViewById(R.id.targetCalories)
        remainingCaloriesText = findViewById(R.id.remainingCalories)
        consumedCaloriesText = findViewById(R.id.consumedCalories)
        progressBar = findViewById(R.id.circularProgressBar) // FIXED: Correct ProgressBar ID

        val btnAddFood = findViewById<Button>(R.id.btnAddFood)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnHistory = findViewById<Button>(R.id.btnHistory) // NEW: Initialize History Button

        adapter = FoodAdapter(foodLogList)
        foodLogListView.adapter = adapter

        checkForDailyReset()

        if (isOnline()) {
            syncOfflineFoods()
        }

        networkMonitor = NetworkMonitor(this) {
            syncOfflineFoods()
        }
        networkMonitor.startMonitoring()

        getFCMTokenAndSave()

        btnAddFood.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            addFoodLauncher.launch(intent)
        }

        val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (prefs.getBoolean(LOCALE_CHANGED_KEY, false)) {
                    prefs.edit().putBoolean(LOCALE_CHANGED_KEY, false).apply()
                    recreate()
                } else {
                    loadUserData()
                }
            }
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
        }

        // NEW: History Button Click Listener
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.stopMonitoring()
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun syncOfflineFoods() {
        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            val unsynced = db.foodLogDao().getAll()
            if (unsynced.isEmpty()) return@launch

            val userGroups = unsynced.groupBy { it.userId }

            userGroups.forEach { (userId, list) ->
                ApiClient.instance.getUser(userId).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        val user = response.body() ?: return
                        val serverMap = user.food_log.toMutableMap()

                        list.forEach { offline ->
                            serverMap[offline.timestamp.toString()] = mapOf(
                                "name" to offline.name,
                                "calories" to offline.calories,
                                "category" to offline.category,
                                "photoUrl" to (offline.photoUrl ?: "")
                            )
                        }

                        val updatedUser = user.copy(food_log = serverMap)

                        ApiClient.instance.updateUser(userId, updatedUser)
                            .enqueue(object : Callback<Map<String, String>> {
                                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        list.forEach { db.foodLogDao().deleteById(it.id) }
                                    }
                                    runOnUiThread { loadUserData() }
                                }

                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    // keep offline data for retry
                                }
                            })
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        // API unreachable → try later
                    }
                })
            }
        }
    }

    private fun checkForDailyReset() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastDate = prefs.getString("last_date", null)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (lastDate == null || lastDate != today) {
            prefs.edit().putString("last_date", today).putBoolean("goal_reached_sent", false).apply()
            resetDailyFoodLog()
        } else {
            loadUserData()
        }
    }

    private fun resetDailyFoodLog() {
        val user = auth.currentUser ?: return
        ApiClient.instance.clearFoodLog(user.uid).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                loadUserData()
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@MainActivity, getString(R.string.food_log_reset_failed), Toast.LENGTH_SHORT).show()
                loadUserData()
            }
        })
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        ApiClient.instance.getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body() ?: return
                totalCalories = 0f
                foodLogList.clear()

                user.food_log.toSortedMap().values.mapNotNull { map ->
                    try {
                        FoodItem(
                            name = map["name"] as String,
                            calories = (map["calories"] as Number).toInt(),
                            category = map["category"] as String,
                            photoUrl = map["photoUrl"] as? String,
                            photoResId = BuiltInFoods.items.find { it.name == map["name"] }?.photoResId
                        )
                    } catch (e: Exception) { null }
                }.forEach { food ->
                    foodLogList.add(food)
                    totalCalories += food.calories
                }

                adapter.notifyDataSetChanged()
                updateCaloriesText(user)
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                foodLogList.clear()
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun updateCaloriesText(user: User) {
        val target = calculateDailyCalories(user)
        val remaining = (target - totalCalories).coerceAtLeast(0f)
        val progressValue = ((totalCalories / target) * 100f).coerceIn(0f, 100f).toInt()

        totalCaloriesText.text = "%.0f".format(totalCalories)
        targetCaloriesText.text = getString(R.string.label_goal, target)
        remainingCaloriesText.text = getString(R.string.label_remaining, remaining)
        consumedCaloriesText.text = getString(R.string.label_consumed, totalCalories)

        android.animation.ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progressValue)
            .setDuration(500)
            .start()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val goalReachedSent = prefs.getBoolean("goal_reached_sent", false)

        if (totalCalories >= target && notificationsEnabled && !goalReachedSent) {
            NotificationHelper.sendGoalReachedNotification(this, totalCalories)
            prefs.edit().putBoolean("goal_reached_sent", true).apply()
        } else if (totalCalories < target) {
            prefs.edit().putBoolean("goal_reached_sent", false).apply()
        }
    }

    private fun calculateDailyCalories(user: User): Float {
        val age = user.age
        val bmr = if (user.gender.lowercase() == "male") {
            10 * user.weight_kg + 6.25 * user.height_cm - 5 * age + 5
        } else {
            10 * user.weight_kg + 6.25 * user.height_cm - 5 * age - 161
        }

        val maintenance = bmr * 1.375
        return when {
            user.goal_weight_kg < user.weight_kg -> (maintenance - 500).toFloat()
            user.goal_weight_kg > user.weight_kg -> (maintenance + 500).toFloat()
            else -> maintenance.toFloat()
        }
    }

    private fun getFCMTokenAndSave() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val body = mapOf(
                    "uid" to userId,
                    "token" to token
                )
                ApiClient.instance.saveFcmToken(body)
                    .enqueue(object : retrofit2.Callback<Map<String, String>> {
                        override fun onResponse(
                            call: retrofit2.Call<Map<String, String>>,
                            response: retrofit2.Response<Map<String, String>>
                        ) {
                            if (response.isSuccessful) {
                                Log.d("FCM", "FCM token saved successfully on server")
                            } else {
                                Log.w("FCM", "Failed to save FCM token. Server returned: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<Map<String, String>>, t: Throwable) {
                            Log.e("FCM", "Failed to save FCM token", t)
                        }
                    })
            }
        }
    }
}






















