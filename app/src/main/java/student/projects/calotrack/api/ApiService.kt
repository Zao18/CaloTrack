package student.projects.calotrack.api

import retrofit2.Call
import retrofit2.http.*
import student.projects.calotrack.models.User
import student.projects.calotrack.models.FoodItem

interface ApiService {
    @GET("users/{uid}")
    fun getUser(@Path("uid") uid: String): Call<User>

    @POST("users")
    fun createUser(@Body user: User): Call<Map<String, String>>

    @PUT("users/{uid}")
    fun updateUser(@Path("uid") uid: String, @Body user: User): Call<Map<String, String>>

    @PUT("users/{uid}/clearFoodLog")
    fun clearFoodLog(@Path("uid") uid: String): Call<Map<String, String>>
}


