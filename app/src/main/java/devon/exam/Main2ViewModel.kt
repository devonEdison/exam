package devon.exam

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import devon.exam.network.ApiStation
import devon.exam.network.SingleLiveEvent
import devon.exam.model.TypicodePhotos
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Main2ViewModel : ViewModel() {

    private val TAG = javaClass.simpleName
    val notifyDataChanged = SingleLiveEvent<Void>()
    val error = SingleLiveEvent<Void>()
    val photos = mutableListOf<TypicodePhotos>()

    fun getPhotos() {
        val call = ApiStation.apiService.getPhotos()
        call.enqueue(object : Callback<List<TypicodePhotos>> {

            override fun onResponse(
                call: Call<List<TypicodePhotos>>,
                response: Response<List<TypicodePhotos>>
            ) {
                photos.clear()
                photos.addAll(response.body()!!.toList())
                notifyDataChanged.call()
            }

            override fun onFailure(call: Call<List<TypicodePhotos>>, t: Throwable) {
                error.call()
            }
        })
    }
}