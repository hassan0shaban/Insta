package com.whiteside.insta

import Controller.Connection
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.whiteside.insta.R

class NoInternet : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)
    }

    fun checkInternet(view: View?) {
        if (Connection.isConnected(this)) {
            finish()
        }
    }
}