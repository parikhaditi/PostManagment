package itu.demo.restapi.ui

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.text.Html
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import itu.demo.R
import itu.demo.restapi.data.ResponseDataModel
import itu.demo.restapi.network.ApiInterface
import kotlinx.android.synthetic.main.activity_list_posts.*
import kotlinx.android.synthetic.main.custom_common_toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PostsListActivity : AppCompatActivity(), HomeAdapter.HomeListener {

    private lateinit var adapter: HomeAdapter
    private var apiInterface: ApiInterface?=null
    var post_lists : ArrayList<ResponseDataModel>?=null
    val SECOND_ACTIVITY_REQUEST_CODE = 0
    var SMS_PERMISSION_CODE : Int =  111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_posts)

        tv_toolbar_title.text = "List of Posts"
        iv_toolbar_back.setOnClickListener { finish() }
        iv_toolbar_plus.setOnClickListener {
            val intent = Intent(this, CreatPostActivity::class.java)
            startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE)
        }

        apiInterface = ApiInterface.getApiClient().create(ApiInterface::class.java)
        initAdapter()

        refresh_button.setOnClickListener {
            callAPI()
        }

        callAPI()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
        }
        else {
            receiveMsg()
        }

    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == SMS_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            receiveMsg()
        }
    }

    private fun receiveMsg()
    {
        var broadcast = object : BroadcastReceiver()
        {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                    for(sms in Telephony.Sms.Intents.getMessagesFromIntent(p1))
                    {
                        if(sms.displayMessageBody.contains("Post ID"))
                        {
                            val msg = sms.displayMessageBody
                            showToast("Message Received !")
                            try {
                                val id = Integer.parseInt(msg.substring(msg.indexOf("-") + 1, msg.lastIndexOf(".")).trim())
                                showDialog(id - 1, sms.displayOriginatingAddress)
                            }
                            catch (e:Exception)
                            {
                                showToast("Invalid or Blank ID")
                            }

                        }

                    }
                }
            }

        }
        registerReceiver(broadcast, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))
    }

    fun showDialog(post_id: Int, sender_number: String) {

        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.incoming_msg_alert, null)
        val tv_msg  = dialogLayout.findViewById<TextView>(R.id.tv_msg)
        val btn_yes  = dialogLayout.findViewById<Button>(R.id.btn_yes)
        val btn_no   = dialogLayout.findViewById<Button>(R.id.btn_no)
        val iv_close  = dialogLayout.findViewById<ImageView>(R.id.iv_close)
        builder.setView(dialogLayout)

        var post_title ="Unknown"

        if(post_id <= 99 && post_id >= 0)
        {
            post_title = post_lists?.get(post_id)?.title.toString()
        }

        tv_msg.text = Html.fromHtml("Incoming Message Request of Viewing Details of <br/> <b>\"" + "Post " + (post_id + 1) + " - " + post_title + "\"</b><br/><br/>Do You want to Reply with Details?")

        builder.setCancelable(false)
        val dialog = builder.show()


        btn_yes.setOnClickListener{

            var sms = SmsManager.getDefault()

            if(post_id<=99 && post_id >= 0)
            {
                val sms_full = post_lists?.get(post_id)?.body
                val parts: ArrayList<String> = sms.divideMessage(sms_full)

                sms.sendMultipartTextMessage(
                        sender_number,
                        "ME",
                        parts,
                        null,
                        null
                )

                showToast("Message Sent !")

            }
            else
            {
                sms.sendTextMessage(
                        sender_number,
                        "ME",
                        "InValid Post ID.Could not find the details of requested post id.",
                        null,
                        null
                )

                showToast("Invalid Post ID Reply Message Sent !")
            }
            dialog.dismiss()
        }


        btn_no.setOnClickListener{

            dialog.dismiss()

        }

        iv_close.setOnClickListener {
            dialog.dismiss()
        }

    }
    private fun showToast(message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get String data from Intent
                val title = data!!.getStringExtra("title")
                val body = data!!.getStringExtra("body")
                val id = data!!.getIntExtra("id", 101)

                var data : ResponseDataModel = ResponseDataModel(1, id?.toInt(), title, body, false)
                post_lists?.add(data)

                adapter.notifyDataSetChanged()
                rv_home.scrollToPosition(adapter.itemCount - 1)
                //post_lists?.let { rv_home.scrollToPosition(it.size) }
            }
        }
    }

    fun callAPI()
    {
        if (isOnline(applicationContext)) {
            fetchAllPosts()
        }
        else {
            refresh_button.visibility = View.VISIBLE
            progress_home.visibility = View.GONE
            Toast.makeText(baseContext, "Internet is not available. Please try again..!", Toast.LENGTH_SHORT).show()
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

    fun fetchAllPosts() {

        apiInterface?.fetchAllPosts()?.enqueue(object : Callback<List<ResponseDataModel>> {

            override fun onFailure(call: Call<List<ResponseDataModel>>, t: Throwable) {
                Log.e("ERROR", "" + t.message);
                progress_home.visibility = GONE
                refresh_button.visibility = View.VISIBLE
                Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                    call: Call<List<ResponseDataModel>>,
                    response: Response<List<ResponseDataModel>>
            ) {
                val res = response.body()
                progress_home.visibility = GONE
                refresh_button.visibility = View.GONE
                if (response.code() == 200 && res != null) {
                    rv_home.visibility = View.VISIBLE
                    post_lists = (response.body() as ArrayList<ResponseDataModel>?)!!
                    for (item in post_lists!!) {
                        item.liked = false
                    }
                    adapter.setData(post_lists as ArrayList<ResponseDataModel>)
                } else {
                    Toast.makeText(baseContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    private fun initAdapter() {
        adapter = HomeAdapter(this)
        rv_home.layoutManager = LinearLayoutManager(this)
        rv_home.adapter = adapter
    }

    override fun onItemDeleted(postModel: ResponseDataModel, position: Int) {

        deletePost(postModel.id!!, position)
    }

    fun deletePost(id: Int, position: Int) {

        apiInterface?.deletePost(id)?.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("error", "" + t.message)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
            }
        })

        adapter.removeData(position)

    }


}


