package student.projects.calotrack.adapters // Create a new package/folder called 'adapters'

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import student.projects.calotrack.R
import student.projects.calotrack.models.RecommendedMeal

class RecommendedMealsAdapter(
    private val meals: List<RecommendedMeal>,
    private val clickListener: (RecommendedMeal) -> Unit
) : RecyclerView.Adapter<RecommendedMealsAdapter.MealViewHolder>() {

    class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mealName: TextView = view.findViewById(R.id.tvMealName)
        val mealImage: ImageView = view.findViewById(R.id.imgMealPhoto)
        val mealCalories: TextView = view.findViewById(R.id.tvMealCalories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.meal_item, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealName.text = meal.name
        holder.mealCalories.text = "${meal.calories} kcal"

        Glide.with(holder.mealImage.context)
            .load(meal.photoUrl)
            .placeholder(R.drawable.ic_default_meal)
            .centerCrop()
            .into(holder.mealImage)

        holder.itemView.setOnClickListener {
            clickListener(meal)
        }
    }

    override fun getItemCount() = meals.size
}