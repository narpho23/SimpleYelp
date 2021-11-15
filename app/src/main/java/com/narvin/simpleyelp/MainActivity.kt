package com.narvin.simpleyelp

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import android.net.ConnectivityManager
import android.view.View
import java.io.IOException







private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "qRVc8XOqI1n-i2YlSWKIY-MnFZPEX8KeG2R94909kw_zJveCtftdoXDCVCCyW95KeeZhwW26FUKf0Pc_oNARlM3gAhf-g1eHE7yKGfPDGeAK480CzeJHh9xeXmOHYXYx"

class MainActivity : AppCompatActivity() {
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_main)

        fun loadData() {
            val restaurants = mutableListOf<YelpRestaurant>()
            val adapter = RestaurantAdapter(this, restaurants)
            rvRestaurants.adapter = adapter
            rvRestaurants.layoutManager = LinearLayoutManager(this)

            val retrofit =
                Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val yelpService = retrofit.create(YelpService::class.java)
            yelpService.searchRestaurants("Bearer $API_KEY", "Avocado Toast", "New York")
                .enqueue(object : Callback<YelpSearchResult> {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onResponse(
                        call: Call<YelpSearchResult>,
                        response: Response<YelpSearchResult>
                    ) {
                        Log.i(TAG, "onResponse $response")
                        val body = response.body()
                        if (body == null) {
                            Log.w(
                                TAG,
                                "Did not receive valid response body from Yelp API... exiting"
                            )
                            return
                        }
                        restaurants.addAll(body.restaurants)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                        Log.i(TAG, "onResponse $t")
                    }

                })
        }

        if (!isNetworkAvailable() && !isOnline()) {
            tvNetError.setVisibility(View.VISIBLE)
            buttonRetry.setVisibility(View.VISIBLE)
            buttonRetry.setOnClickListener {
                if (isNetworkAvailable() && isOnline()) {
                    tvNetError.setVisibility(View.GONE)
                    buttonRetry.setVisibility(View.GONE)
                    loadData()
                }
            }
        } else {
            loadData()
        }

    }
}