package student.projects.calotrack

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val name = findViewById<EditText>(R.id.nameRegister)
        val email = findViewById<EditText>(R.id.emailRegister)
        val password = findViewById<EditText>(R.id.passwordRegister)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnRegister.setOnClickListener {
            val nameText = name.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()

            if (nameText.isEmpty() || emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = intent.getStringExtra("gender")!!
            val age = intent.getIntExtra("age", 0)
            val height = intent.getIntExtra("height", 0)
            val weight = intent.getIntExtra("weight", 0)
            val goalWeight = intent.getIntExtra("goalWeight", 0)

            val calorieGoal = calculateCalorieGoal(gender, age, height, weight, goalWeight)

            auth.createUserWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = mapOf(
                            "uid" to userId,
                            "name" to nameText,
                            "email" to emailText,
                            "gender" to gender,
                            "age" to age,
                            "height_cm" to height,
                            "weight_kg" to weight,
                            "goal_weight_kg" to goalWeight,
                            "calorie_goal" to calorieGoal
                        )

                        usersRef.child(userId).setValue(userMap).addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Account created! 🎯", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to save data: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to create account: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun calculateCalorieGoal(
        gender: String,
        age: Int,
        height: Int,
        weight: Int,
        goalWeight: Int
    ): Int {
        val bmr = if (gender.equals("Male", true)) {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
        var dailyCalories = bmr * 1.375
        dailyCalories += when {
            goalWeight < weight -> -500
            goalWeight > weight -> 500
            else -> 0
        }
        return dailyCalories.toInt()
    }
}










