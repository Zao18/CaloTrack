package student.projects.calotrack

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import student.projects.calotrack.adapters.RecommendedMealsAdapter
import student.projects.calotrack.api.ApiClient
import student.projects.calotrack.data.AppDatabase
import student.projects.calotrack.data.OfflineFoodLog
import student.projects.calotrack.models.BuiltInFoods
import student.projects.calotrack.models.FoodItem
import student.projects.calotrack.models.RecommendedMeal
import student.projects.calotrack.models.MealRecommendations
import student.projects.calotrack.models.User

class AddFoodActivity : AppCompatActivity() {

    private lateinit var foodListView: ListView
    private lateinit var searchBar: EditText
    private lateinit var rvRecommendedMeals: RecyclerView
    private lateinit var tvRecommendedMealsTitle: TextView
    private val foodList = mutableListOf<FoodItem>()
    private val allFoods = mutableListOf<FoodItem>()
    private var currentUserId: String? = null
    private lateinit var adapter: FoodAdapter
    private var goalType: String = "MAINTAIN"

    private val createFoodLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newFood = result.data?.getSerializableExtra("new_food") as? FoodItem
                if (newFood != null && currentUserId != null) {
                    saveCreatedFood(newFood)
                    allFoods.add(newFood)
                    filterFoods(searchBar.text.toString())
                    Toast.makeText(this, getString(R.string.food_saved_toast, newFood.name), Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            finish()
            return
        }

        foodListView = findViewById(R.id.foodListView)
        searchBar = findViewById(R.id.searchBar)
        val btnCreateFood = findViewById<Button>(R.id.btnCreateFood)

        rvRecommendedMeals = findViewById(R.id.rvRecommendedMeals)
        tvRecommendedMealsTitle = findViewById(R.id.tvRecommendedMealsTitle)

        rvRecommendedMeals.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRecommendedMeals.adapter = RecommendedMealsAdapter(emptyList()) { meal ->
            showMealDetailsDialog(meal)
        }

        adapter = FoodAdapter(foodList)
        foodListView.adapter = adapter

        loadAllFoods()

        foodListView.setOnItemClickListener { _, _, position, _ ->
            currentUserId?.let { addFoodToUser(it, foodList[position]) }
        }

        btnCreateFood.setOnClickListener {
            val intent = Intent(this, CreateFoodActivity::class.java)
            createFoodLauncher.launch(intent)
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFoods(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun showMealDetailsDialog(meal: RecommendedMeal) {
        val message = buildString {
            append("Calories: ${meal.calories}\n\n")
            append("Ingredients:\n")
            meal.ingredients.forEach { append("• $it\n") }
            append("\nInstructions:\n")
            meal.instructions.forEachIndexed { index, instruction ->
                append("${index + 1}. $instruction\n")
            }
        }

        AlertDialog.Builder(this)
            .setTitle(meal.name)
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun loadAllFoods() {
        allFoods.clear()
        allFoods.addAll(BuiltInFoods.items)

        currentUserId?.let { uid ->
            ApiClient.instance.getUser(uid).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body() ?: return

                    val currentWeight = user.weight_kg
                    val goalWeight = user.goal_weight_kg

                    if (currentWeight != null && goalWeight != null) {
                        goalType = when {
                            goalWeight > currentWeight -> "BULK"
                            goalWeight < currentWeight -> "LOSE"
                            else -> "MAINTAIN"
                        }
                    }

                    val filteredMeals = MealRecommendations.meals.filter { it.type == goalType }

                    tvRecommendedMealsTitle.text = when (goalType) {
                        "BULK" -> getString(R.string.recommended_meals_bulking)
                        "LOSE" -> getString(R.string.recommended_meals_weight_loss)
                        else -> getString(R.string.recommended_meals_general)
                    }

                    val mealsAdapter = RecommendedMealsAdapter(filteredMeals) { meal ->
                        showMealDetailsDialog(meal)
                    }
                    rvRecommendedMeals.adapter = mealsAdapter

                    user.custom_foods?.forEach { map ->
                        val name = map["name"] as? String ?: return@forEach
                        val calories = (map["calories"] as? Number)?.toInt() ?: 0
                        val category = map["category"] as? String ?: "Custom"
                        val photoUrl = map["photoUrl"] as? String
                        if (BuiltInFoods.items.none { it.name == name }) {
                            allFoods.add(FoodItem(name, calories, category, photoUrl))
                        }
                    }
                    filterFoods(searchBar.text.toString())
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@AddFoodActivity, getString(R.string.toast_failed_custom_load), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun filterFoods(query: String) {
        val filtered = if (query.isEmpty()) allFoods else allFoods.filter {
            it.name.contains(query, ignoreCase = true)
        }
        foodList.clear()
        foodList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun addFoodToUser(uid: String, food: FoodItem) {
        if (!isOnline()) {
            val db = AppDatabase.getDatabase(this)
            val offline = OfflineFoodLog(
                userId = uid,
                name = food.name,
                calories = food.calories,
                category = food.category,
                photoUrl = food.photoUrl
            )
            CoroutineScope(Dispatchers.IO).launch {
                db.foodLogDao().insert(offline)
            }
            Toast.makeText(this, getString(R.string.food_saved_offline_toast, food.name), Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            return
        }
        ApiClient.instance.getUser(uid).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body() ?: return
                val updatedFoodLog = user.food_log.toMutableMap()

                val foodMap = mutableMapOf<String, Any>(
                    "name" to food.name,
                    "calories" to food.calories,
                    "category" to food.category
                )

                food.photoUrl?.let { foodMap["photoUrl"] = it }

                updatedFoodLog[System.currentTimeMillis().toString()] = foodMap

                val updatedUser = user.copy(food_log = updatedFoodLog)

                ApiClient.instance.updateUser(uid, updatedUser)
                    .enqueue(object : Callback<Map<String, String>> {
                        override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                            Toast.makeText(this@AddFoodActivity, getString(R.string.food_added_to_log_toast, food.name), Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                        }

                        override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                            Toast.makeText(this@AddFoodActivity, getString(R.string.food_add_failed_toast, t.message), Toast.LENGTH_SHORT).show()
                        }
                    })
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@AddFoodActivity, getString(R.string.food_add_failed_toast, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveCreatedFood(food: FoodItem) {
        currentUserId?.let { uid ->
            ApiClient.instance.getUser(uid).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body() ?: return
                    val updatedCustomFoods = user.custom_foods?.toMutableList() ?: mutableListOf()
                    if (updatedCustomFoods.none { it["name"] == food.name }) {
                        val foodMap = mutableMapOf<String, Any>(
                            "name" to food.name,
                            "calories" to food.calories,
                            "category" to food.category
                        )
                        food.photoUrl?.let { foodMap["photoUrl"] = it }
                        updatedCustomFoods.add(foodMap)

                        val updatedUser = user.copy(custom_foods = updatedCustomFoods)

                        ApiClient.instance.updateUser(uid, updatedUser)
                            .enqueue(object : Callback<Map<String, String>> {
                                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                                    Toast.makeText(this@AddFoodActivity, getString(R.string.food_saved_toast, food.name), Toast.LENGTH_SHORT).show()
                                }

                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    Toast.makeText(this@AddFoodActivity, getString(R.string.custom_food_save_failed), Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@AddFoodActivity, getString(R.string.custom_food_save_failed), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val netInfo = cm.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return netInfo.isConnected
        }
    }
}