package student.projects.calotrack

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class AddFoodActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var foodListView: ListView
    private lateinit var searchBar: EditText
    private val foodList = mutableListOf<FoodItem>()
    private val allFoods = mutableListOf<FoodItem>()
    private var currentUserId: String? = null
    private lateinit var adapter: FoodAdapter

    private val createFoodLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val newFood = result.data?.getSerializableExtra("new_food") as? FoodItem
                if (newFood != null && currentUserId != null) {
                    saveCreatedFood(newFood) // Save to user's list (not log)
                    allFoods.add(newFood)
                    filterFoods(searchBar.text.toString())
                    Toast.makeText(this, "${newFood.name} saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid
        if (currentUserId == null) { finish(); return }

        foodListView = findViewById(R.id.foodListView)
        searchBar = findViewById(R.id.searchBar)
        val btnCreateFood = findViewById<Button>(R.id.btnCreateFood)

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

    private fun loadAllFoods() {
        allFoods.clear()
        allFoods.addAll(BuiltInFoods.items)

        currentUserId?.let { uid ->
            ApiClient.instance.getUser(uid).enqueue(object : Callback<User> { //(Jamshidbek Boynazarov, 2025)
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body() ?: return
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
                    Toast.makeText(this@AddFoodActivity, "Failed to load custom foods", Toast.LENGTH_SHORT).show()
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

    private fun addFoodToUser(uid: String, food: FoodItem) { //(Android Developers, 2019)
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

                ApiClient.instance.updateUser(uid, updatedUser) //(Android Developers, 2019)
                    .enqueue(object : Callback<Map<String, String>> {
                        override fun onResponse(
                            call: Call<Map<String, String>>,
                            response: Response<Map<String, String>>
                        ) {
                            Toast.makeText(
                                this@AddFoodActivity,
                                "${food.name} added to log!",
                                Toast.LENGTH_SHORT
                            ).show()
                            
                            setResult(RESULT_OK)
                        }

                        override fun onFailure(call: Call<Map<String, String>>, t: Throwable) { //(Android Developers, 2019)
                            Toast.makeText(
                                this@AddFoodActivity,
                                "Failed to add food: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            override fun onFailure(call: Call<User>, t: Throwable) { // (Android Developers, 2019)
                Toast.makeText(
                    this@AddFoodActivity,
                    "Error fetching user: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun saveCreatedFood(food: FoodItem) { // (Android Developers, 2019)
        currentUserId?.let { uid ->
            ApiClient.instance.getUser(uid).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body() ?: return
                    val updatedCustomFoods = user.custom_foods?.toMutableList() ?: mutableListOf()
                    if (updatedCustomFoods.none { it["name"] == food.name }) { // (Android Developers, 2019)
                        val foodMap = mutableMapOf<String, Any>(
                            "name" to food.name,
                            "calories" to food.calories,
                            "category" to food.category
                        )
                        food.photoUrl?.let { foodMap["photoUrl"] = it }
                        updatedCustomFoods.add(foodMap)

                        val updatedUser = user.copy(custom_foods = updatedCustomFoods)

                        ApiClient.instance.updateUser(uid, updatedUser) // (Android Developers, 2019)
                            .enqueue(object : Callback<Map<String, String>> {
                                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                                    Toast.makeText(this@AddFoodActivity, "${food.name} saved!", Toast.LENGTH_SHORT).show()
                                }

                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    Toast.makeText(this@AddFoodActivity, "Failed to save custom food", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {}
            })
        }
    }
}























