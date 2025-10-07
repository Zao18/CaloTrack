package student.projects.calotrack.models

data class FoodItem(
    val name: String,
    val calories: Int,
    val category: String,
    val photoUrl: String? = null,
    val photoResId: Int? = null
) : java.io.Serializable

