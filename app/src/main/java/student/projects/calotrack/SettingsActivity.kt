package student.projects.calotrack

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val prefs by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val editDesiredWeight = findViewById<EditText>(R.id.editDesiredWeight)
        val btnSaveWeight = findViewById<Button>(R.id.btnSaveWeight)
        val spinnerLanguage = findViewById<Spinner>(R.id.spinnerLanguage)
        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        usersRef.child(userId).get().addOnSuccessListener { snapshot ->
            val name = snapshot.child("name").value?.toString() ?: "User"
            val goalWeight = snapshot.child("goal_weight_kg").value?.toString() ?: ""
            tvUserName.text = name
            editDesiredWeight.setText(goalWeight)
        }

        btnSaveWeight.setOnClickListener {
            val newWeight = editDesiredWeight.text.toString().trim()
            if (newWeight.isEmpty()) return@setOnClickListener

            usersRef.child(userId).child("goal_weight_kg").setValue(newWeight.toFloat())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Goal weight updated!", Toast.LENGTH_SHORT).show()
                        // Notify MainActivity to refresh
                        setResult(RESULT_OK)
                    }
                }
        } //(www.youtube.com, 2020)

        val languages = arrayOf("English", "Afrikaans", "Xhosa", "Zulu")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true) //(Android Developers, 2024)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            Toast.makeText(this, "Notifications ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    usersRef.child(userId).removeValue().addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            auth.currentUser?.delete()?.addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LandingActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Failed to delete account: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } //(tanzTalks.tech, 2021)
    }
}

