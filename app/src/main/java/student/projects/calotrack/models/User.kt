package student.projects.calotrack.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height_cm: Int = 0,
    val weight_kg: Int = 0,
    val goal_weight_kg: Int = 0,
    val food_log: Map<String, Map<String, Any>> = emptyMap(),
    val custom_foods: List<Map<String, Any>>? = null
)

