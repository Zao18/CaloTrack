package student.projects.calotrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnAlreadyHaveAccount = findViewById<Button>(R.id.btnAlreadyHaveAccount)

        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, UserInfoActivity::class.java))
        }

        btnAlreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}

