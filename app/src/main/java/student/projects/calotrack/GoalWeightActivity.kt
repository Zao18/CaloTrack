package student.projects.calotrack

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class GoalWeightActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_weight)

        val editGoalWeight = findViewById<EditText>(R.id.editGoalWeight)
        val btnNext = findViewById<Button>(R.id.btnSaveGoal)

        val gender = intent.getStringExtra("gender")!!
        val age = intent.getIntExtra("age", 0)
        val height = intent.getIntExtra("height", 0)
        val weight = intent.getIntExtra("weight", 0)

        btnNext.setOnClickListener {
            val goalText = editGoalWeight.text.toString().trim()
            val goalWeight = goalText.toIntOrNull()
            if (goalWeight == null || goalWeight <= 0) {
                Toast.makeText(this, "Please enter a valid goal weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intentToRegister = Intent(this, RegisterActivity::class.java)
            intentToRegister.putExtra("gender", gender)
            intentToRegister.putExtra("age", age)
            intentToRegister.putExtra("height", height)
            intentToRegister.putExtra("weight", weight)
            intentToRegister.putExtra("goalWeight", goalWeight)
            startActivity(intentToRegister)
            finish()
        }
    }
}


