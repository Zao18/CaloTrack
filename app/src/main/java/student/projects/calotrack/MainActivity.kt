package student.projects.calotrack

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import student.projects.calotrack.api.ApiClient
import student.projects.calotrack.models.BuiltInFoods
import student.projects.calotrack.models.FoodItem
import student.projects.calotrack.models.User
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

    private val addFoodLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) loadUserData()
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
        progressBar = findViewById(R.id.progressBar)

        val btnAddFood = findViewById<Button>(R.id.btnAddFood)
        val btnSettings = findViewById<Button>(R.id.btnSettings)

        adapter = FoodAdapter(foodLogList)
        foodLogListView.adapter = adapter

        checkForDailyReset()

        btnAddFood.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            addFoodLauncher.launch(intent)
        }

        val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) loadUserData()
        }

        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            settingsLauncher.launch(intent)
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
    } // (Android Developers, 2020)

    private fun resetDailyFoodLog() {
        val user = auth.currentUser ?: return
        ApiClient.instance.clearFoodLog(user.uid).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                loadUserData()
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to reset food log", Toast.LENGTH_SHORT).show()
                loadUserData()
            }
        })
    } // (Philipp Lackner, 2021)

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        ApiClient.instance.getUser(userId).enqueue(object : Callback<User> { //(CodingZest, 2023)
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body() ?: return

                totalCalories = 0f
                foodLogList.clear()

                user.food_log.toSortedMap().values.mapNotNull { map -> //(Jamshidbek Boynazarov, 2025)
                    try {
                        FoodItem(
                            name = map["name"] as String,
                            calories = (map["calories"] as Number).toInt(),
                            category = map["category"] as String,
                            photoUrl = map["photoUrl"] as? String,
                            photoResId = BuiltInFoods.items.find { it.name == map["name"] }?.photoResId
                        )
                    } catch (e: Exception) {
                        null
                    }
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

        totalCaloriesText.text = "%.0f".format(totalCalories) //(prash, 2024)
        targetCaloriesText.text = "Goal: %.0f".format(target)
        remainingCaloriesText.text = "Remaining: %.0f".format(remaining)
        consumedCaloriesText.text = "Consumed: %.0f".format(totalCalories)

        ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progressValue) //(Stack Overflow, 2012)
            .setDuration(500)
            .start()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE) // (Android Developers, 2025)
        val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        val goalReachedSent = prefs.getBoolean("goal_reached_sent", false)

        if (totalCalories >= target) {
            if (notificationsEnabled && !goalReachedSent) {
                NotificationHelper.sendGoalReachedNotification(this, totalCalories)
                prefs.edit().putBoolean("goal_reached_sent", true).apply()
            }
        } else {
            prefs.edit().putBoolean("goal_reached_sent", false).apply()
        }
    } // (Ahmed Guedmioui, 2024)

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
}


















