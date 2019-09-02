package devon.exam.network

import devon.exam.model.TypicodePhotos
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("photos")
    fun getPhotos(): Call<List<TypicodePhotos>>
}