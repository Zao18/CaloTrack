package student.projects.calotrack.models

import student.projects.calotrack.R
import java.io.Serializable

data class RecommendedMeal(
    val name: String,
    val type: String,
    val photoUrl: Int,
    val calories: Int,
    val ingredients: List<String>,
    val instructions: List<String>
) : Serializable

object MealRecommendations {
    val meals = listOf(
        RecommendedMeal(
            name = "Chicken & Veg Stir Fry",
            type = "LOSE",
            photoUrl = R.drawable.stirfry,
            calories = 350,
            ingredients = listOf("100g Chicken Breast", "200g Mixed Vegetables", "1 tbsp Soy Sauce", "1 tsp Sesame Oil"),
            instructions = listOf("Slice chicken and vegetables.", "Heat oil in a pan, add chicken and cook until done.", "Add vegetables and stir fry until tender-crisp.", "Stir in soy sauce.")
        ),
        RecommendedMeal(
            name = "Lentil Soup",
            type = "LOSE",
            photoUrl = R.drawable.lintilsoup,
            calories = 300,
            ingredients = listOf("1 cup Brown Lentils", "4 cups Vegetable Broth", "1 Carrot (diced)", "1 Celery Stalk (diced)", "Spices"),
            instructions = listOf("Combine all ingredients in a pot.", "Bring to a boil, then reduce heat and simmer for 30 minutes, or until lentils are tender.")
        ),
        RecommendedMeal(
            name = "Avocado Toast with Egg",
            type = "LOSE",
            photoUrl = R.drawable.avotoast,
            calories = 320,
            ingredients = listOf("1 slice Whole Grain Toast", "1/2 Avocado", "1 Egg", "Salt and Pepper"),
            instructions = listOf("Toast the bread.", "Mash the avocado and spread on toast.", "Fry or poach the egg.", "Place egg on top, season, and serve.")
        ),
        RecommendedMeal(
            name = "High-Calorie Smoothie",
            type = "BULK",
            photoUrl = R.drawable.smoothiehighcal,
            calories = 750,
            ingredients = listOf("1 cup Whole Milk", "1 scoop Protein Powder", "1 Banana", "2 tbsp Peanut Butter", "1/2 cup Oats"),
            instructions = listOf("Blend all ingredients until smooth.", "Serve immediately.")
        ),
        RecommendedMeal(
            name = "Steak & Sweet Potato",
            type = "BULK",
            photoUrl = R.drawable.steakpotato,
            calories = 680,
            ingredients = listOf("200g Sirloin Steak", "1 large Sweet Potato", "1 tbsp Olive Oil", "Asparagus (optional)"),
            instructions = listOf("Bake sweet potato until tender.", "Season and pan-fry steak to desired doneness.", "Serve together with a side of vegetables.")
        ),
        RecommendedMeal(
            name = "Chicken & Rice Bowl",
            type = "BULK",
            photoUrl = R.drawable.chickenrice,
            calories = 600,
            ingredients = listOf("150g Cooked Chicken", "1 cup Cooked White Rice", "1/2 cup Black Beans", "Salsa/Guacamole"),
            instructions = listOf("Combine all ingredients in a bowl.", "Mix gently and enjoy.")
        )
    )
}