package student.projects.calotrack

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class UserInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        val btnMale = findViewById<Button>(R.id.btnMale)
        val btnFemale = findViewById<Button>(R.id.btnFemale)
        val editAge = findViewById<EditText>(R.id.editAge)
        val editHeight = findViewById<EditText>(R.id.editHeight)
        val editWeight = findViewById<EditText>(R.id.editWeight)
        val btnNext = findViewById<Button>(R.id.btnNext)
        var selectedGender = ""

        fun updateGenderSelection(selected: Button, other: Button, gender: String) {
            selected.isSelected = true
            other.isSelected = false
            selected.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            other.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            selectedGender = gender
        }

        btnMale.setOnClickListener { updateGenderSelection(btnMale, btnFemale, "Male") }
        btnFemale.setOnClickListener { updateGenderSelection(btnFemale, btnMale, "Female") }

        btnNext.setOnClickListener {
            val ageText = editAge.text.toString().trim()
            val heightText = editHeight.text.toString().trim()
            val weightText = editWeight.text.toString().trim()

            if (selectedGender.isEmpty() || ageText.isEmpty() || heightText.isEmpty() || weightText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            val height = heightText.toIntOrNull()
            val weight = weightText.toIntOrNull()

            if (age == null || age <= 0 || age > 120) {
                Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (height == null || height <= 0 || weight == null || weight <= 0) {
                Toast.makeText(this, "Enter valid height and weight", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, GoalWeightActivity::class.java)
            intent.putExtra("gender", selectedGender)
            intent.putExtra("age", age)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            startActivity(intent)
            finish()
        }
    }
}





