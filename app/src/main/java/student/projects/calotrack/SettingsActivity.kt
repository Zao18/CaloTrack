package student.projects.calotrack

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")
    private val prefs by lazy { getSharedPreferences("app_prefs", MODE_PRIVATE) }
    private val LANG_PREF_KEY = "language_code"
    private val LOCALE_CHANGED_KEY = "locale_changed_pending"
    private lateinit var tvUserName: TextView
    private lateinit var editDesiredWeight: EditText
    private lateinit var btnSaveWeight: Button
    private lateinit var spinnerLanguage: Spinner
    private lateinit var switchNotifications: Switch
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnLogout: Button


    override fun attachBaseContext(newBase: Context) {
        val lang = newBase.getSharedPreferences("app_prefs", MODE_PRIVATE).getString(LANG_PREF_KEY, "en") ?: "en"
        super.attachBaseContext(updateLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return

        tvUserName = findViewById(R.id.tvUserName)
        editDesiredWeight = findViewById(R.id.editDesiredWeight)
        btnSaveWeight = findViewById(R.id.btnSaveWeight)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        switchNotifications = findViewById(R.id.switchNotifications)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnLogout = findViewById(R.id.btnLogout)

        loadSettingsData(userId)

        setupListeners(userId)
    }
    private fun loadSettingsData(userId: String) {
        usersRef.child(userId).get().addOnSuccessListener { snapshot ->
            val name = snapshot.child("name").value?.toString() ?: getString(R.string.default_username)
            val goalWeight = snapshot.child("goal_weight_kg").value?.toString() ?: ""
            val notificationsEnabledDB = snapshot.child("notifications_enabled").value as? Boolean ?: true

            tvUserName.text = name
            editDesiredWeight.setText(goalWeight)


            switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", notificationsEnabledDB)

            val languages = arrayOf("English", "Afrikaans", "Zulu")
            val languageCodes = arrayOf("en", "af", "zu")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLanguage.adapter = adapter

            val currentLangCode = prefs.getString(LANG_PREF_KEY, "en")
            val currentPosition = languageCodes.indexOf(currentLangCode)
            if (currentPosition != -1) {
                spinnerLanguage.setSelection(currentPosition, false)
            }
        }
    }

    private fun setupListeners(userId: String) {

        // Save Weight Button
        btnSaveWeight.setOnClickListener {
            val newWeight = editDesiredWeight.text.toString().trim()
            if (newWeight.isEmpty()) return@setOnClickListener

            usersRef.child(userId).child("goal_weight_kg").setValue(newWeight.toFloat())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, getString(R.string.goal_weight_updated), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK) // Trigger MainActivity refresh (if no locale change)
                    }
                }
        }

        val languageCodes = arrayOf("en", "af", "zu")
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLangCode = languageCodes[position]
                val currentLangCode = prefs.getString(LANG_PREF_KEY, "en")

                if (selectedLangCode != currentLangCode) {
                    prefs.edit().putString(LANG_PREF_KEY, selectedLangCode).apply()

                    prefs.edit().putBoolean(LOCALE_CHANGED_KEY, true).apply()

                    setResult(RESULT_OK)
                    finish()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        // Notification Switch Listener
        switchNotifications.text = getString(R.string.enable_notifications)
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->

            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()

            usersRef.child(userId)
                .child("notifications_enabled")
                .setValue(isChecked)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val toastMessage = if (isChecked) getString(R.string.notifications_enabled) else getString(R.string.notifications_disabled)
                        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to save notification preference on server.", Toast.LENGTH_SHORT).show()
                        // Revert the switch state if the save failed
                        switchNotifications.isChecked = !isChecked
                    }
                }
        }

        // Logout button
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Delete Account button
        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_account_title))
                .setMessage(getString(R.string.delete_account_message))
                .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                    usersRef.child(userId).removeValue().addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            auth.currentUser?.delete()?.addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    Toast.makeText(this, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, LandingActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, getString(R.string.auth_delete_failed, authTask.exception?.message), Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.db_delete_failed, dbTask.exception?.message), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton(getString(R.string.cancel_button), null)
                .show()
        }
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
}

