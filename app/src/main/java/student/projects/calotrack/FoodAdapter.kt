package student.projects.calotrack

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import student.projects.calotrack.models.FoodItem

class FoodAdapter(private val items: List<FoodItem>) : BaseAdapter() {

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View { // (Android Developers, 2025a)
        val view = convertView ?: LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_food, parent, false)

        val food = items[position]
        val imageView = view.findViewById<ImageView>(R.id.foodImageView)
        val textView = view.findViewById<TextView>(R.id.foodTextView)
        val caloriesTextView = view.findViewById<TextView>(R.id.foodCaloriesTextView)

        textView.text = food.name
        caloriesTextView.text = parent?.context?.getString(R.string.food_calories_format, food.calories, food.category)

        // (Игор, 2013)
        when {
            food.photoResId != null -> {
                imageView.setImageResource(food.photoResId)
            }
            !food.photoUrl.isNullOrEmpty() -> {
                try {
                    val bytes = Base64.decode(food.photoUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    imageView.setImageResource(android.R.drawable.ic_menu_report_image)
                }
            }
            else -> imageView.setImageResource(android.R.drawable.ic_menu_report_image)
        }

        return view
    }
}




