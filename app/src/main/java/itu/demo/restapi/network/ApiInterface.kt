package itu.demo.restapi.network

import com.google.gson.GsonBuilder
import itu.demo.restapi.data.RequestDataModel
import itu.demo.restapi.data.ResponseDataModel
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiInterface {

    @GET("posts")
    fun fetchAllPosts(): Call<List<ResponseDataModel>>

    @POST("posts")
    fun createPost(@Body RequestDataModel: RequestDataModel) : Call<ResponseDataModel>

    @DELETE("posts/{id}")
    fun deletePost(@Path("id") id:Int):Call<String>

    companion object create{
        private var retrofit: Retrofit?=null
        fun getApiClient(): Retrofit {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(100, TimeUnit.SECONDS)
                .build()
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl("https://jsonplaceholder.typicode.com/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
            }
            return retrofit!!
        }

    }

}