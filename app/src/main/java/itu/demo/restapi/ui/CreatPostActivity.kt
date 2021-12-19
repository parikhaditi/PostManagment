package itu.demo.restapi.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.opengl.Visibility
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import itu.demo.R
import itu.demo.restapi.data.RequestDataModel
import itu.demo.restapi.data.ResponseDataModel
import itu.demo.restapi.network.ApiInterface
import kotlinx.android.synthetic.main.activity_list_posts.*
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.custom_common_toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CreatPostActivity : AppCompatActivity(){

    private var apiInterface: ApiInterface?=null
    var requestDataModel : RequestDataModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        apiInterface = ApiInterface.getApiClient().create(ApiInterface::class.java)

        iv_toolbar_plus.visibility = View.GONE

        iv_toolbar_back.setOnClickListener { finish() }

        btn_create_post.setOnClickListener() {
            if(!TextUtils.isEmpty(et_title.text) && !TextUtils.isEmpty(et_body.text)) {
                if (isOnline(applicationContext)) {
                    createPost()
                }
                else
                {
                    Toast.makeText(baseContext,"Internet is not available. Please try again..!",Toast.LENGTH_SHORT).show()
                }
            }
            else
            {
                Toast.makeText(baseContext,"Please provide all details for the Post",Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun isOnline(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nInfo = cm.activeNetworkInfo
            return nInfo != null && nInfo.isConnected
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    fun createPost() {

        requestDataModel = RequestDataModel(1,et_title.text.toString(),et_body.text.toString())

        apiInterface?.createPost(requestDataModel!!)?.enqueue(object : Callback<ResponseDataModel> {

            override fun onFailure(call: Call<ResponseDataModel>, t: Throwable) {
                Log.e("ERROR",""+t.message);

                Toast.makeText(baseContext,"Something went wrong",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                    call: Call<ResponseDataModel>,
                    response: Response<ResponseDataModel>
            ) {
                val res = response.body()

                if (res!=null){

                    Toast.makeText(baseContext,"Post Created!!",Toast.LENGTH_LONG).show()

                    val intent = Intent()
                    intent.putExtra("title", et_title.text.toString())
                    intent.putExtra("body", et_body.text.toString())
                    intent.putExtra("id", res.id)
                    setResult(Activity.RESULT_OK, intent)
                    finish()

                }else{
                    Toast.makeText(baseContext,"Something went wrong",Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

}


