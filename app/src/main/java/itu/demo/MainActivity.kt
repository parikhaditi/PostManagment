package itu.demo

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import itu.demo.map.MapsActivity
import itu.demo.restapi.ui.PostsListActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_common_toolbar.*


class MainActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iv_toolbar_back.visibility = View.GONE
        iv_toolbar_plus.visibility = View.GONE

        btn_list_posts.setOnClickListener() {
            Intent(this, PostsListActivity::class.java).apply {
                startActivity(this)
            }
        }

        btn__goto_map.setOnClickListener() {
            Intent(this, MapsActivity::class.java).apply {
                startActivity(this)
            }
        }


    }



}


