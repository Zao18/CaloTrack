package student.projects.calotrack

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import student.projects.calotrack.models.FoodItem
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CreateFoodActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                if (selectedImageUri != null) {
                    Toast.makeText(this, getString(R.string.image_selected_toast), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.no_image_selected_toast), Toast.LENGTH_SHORT).show()
                }
            }
        } // (Android Developers, 2024a)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_food)

        val btnAddPicture = findViewById<Button>(R.id.btnAddPicture) // (Android Developers, 2024a)
        val foodName = findViewById<EditText>(R.id.editFoodName)
        val calories = findViewById<EditText>(R.id.editCalories)
        val category = findViewById<EditText>(R.id.editCategory)
        val btnSave = findViewById<Button>(R.id.btnSaveFood)

        btnAddPicture.setOnClickListener { // (Android Developers, 2024a)
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnSave.setOnClickListener {
            val nameText = foodName.text.toString().trim()
            val calText = calories.text.toString().trim()
            val catText = category.text.toString().trim()

            if (nameText.isEmpty() || calText.isEmpty() || catText.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_all_fields_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val caloriesInt = calText.toIntOrNull() ?: run {
                Toast.makeText(this, getString(R.string.invalid_calories_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val photoBase64 = selectedImageUri?.let { convertUriToBase64(it) }

            val foodItem = FoodItem(nameText, caloriesInt, catText, photoBase64)

            Toast.makeText(this, getString(R.string.food_item_added_toast), Toast.LENGTH_SHORT).show()

            val resultIntent = Intent()
            resultIntent.putExtra("new_food", foodItem)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun convertUriToBase64(uri: Uri): String? { // (basickarl, 2013)
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.image_convert_failed_toast, e.message), Toast.LENGTH_SHORT).show()
            null
        }
    }
}












