package student.projects.calotrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        executor = ContextCompat.getMainExecutor(this)

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val email = findViewById<EditText>(R.id.emailLogin)
        val password = findViewById<EditText>(R.id.passwordLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val goToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        val btnGoogle = findViewById<SignInButton>(R.id.btnGoogleLogin)
        val btnBiometric = findViewById<ImageButton>(R.id.btnBiometricLogin) //(Android Developers, 2025)

        val prefs = getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_LONG).show()
            }
        }

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnLogin.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()

            if (emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(emailText, passText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        prefs.edit()
                            .putString("saved_email", emailText)
                            .putString("saved_password", passText)
                            .putBoolean("use_biometric", true)
                            .apply()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "${getString(R.string.login_failed)}: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        goToRegister.setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }

        btnBiometric.setOnClickListener { //(Android Developers, 2025)
            val useBiometric = prefs.getBoolean("use_biometric", false)
            if (useBiometric) {
                val savedEmail = prefs.getString("saved_email", null)
                val savedPassword = prefs.getString("saved_password", null)
                if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                    startBiometricLogin(savedEmail, savedPassword)
                } else {
                    Toast.makeText(this, "No credentials saved for biometric login.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "You must first log in normally to enable biometric login.", Toast.LENGTH_SHORT).show()
            }
        }

        val autoUse = prefs.getBoolean("use_biometric", false) //(Android Developers, 2025)
        if (autoUse) {
            val savedEmail = prefs.getString("saved_email", null)
            val savedPassword = prefs.getString("saved_password", null)
            if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                startBiometricLogin(savedEmail, savedPassword)
            }
        }
    }

    private fun startBiometricLogin(email: String, password: String) { //(Android Developers, 2025)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_login_title))
            .setSubtitle(getString(R.string.biometric_login_subtitle))
            .setNegativeButtonText(getString(R.string.biometric_login_negative))
            .build()

        val biometricPrompt = BiometricPrompt(this, executor, //(Android Developers, 2025)
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Toast.makeText(applicationContext, errString, Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { //(Android Developers, 2025)
                    // Sign in with saved credentials
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(applicationContext, getString(R.string.biometric_authentication_succeeded), Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(applicationContext, "Firebase login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }

                override fun onAuthenticationFailed() { //(Android Developers, 2025)
                    Toast.makeText(applicationContext, getString(R.string.biometric_authentication_failed), Toast.LENGTH_SHORT).show()
                }
            })

        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(this, "Biometric authentication not available on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val token = acct.idToken ?: return
        val credential = GoogleAuthProvider.getCredential(token, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        ensureUserInDatabase(it.uid, it.displayName ?: "User", it.email ?: "")
                    }
                } else {
                    Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun ensureUserInDatabase(uid: String, name: String, email: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.child(uid).get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val userMap = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "gender" to "Not set",
                    "age" to 0,
                    "height_cm" to 0,
                    "weight_kg" to 0,
                    "goal_weight_kg" to 0,
                    "calorie_goal" to 2000
                )

                usersRef.child(uid).setValue(userMap).addOnCompleteListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}












