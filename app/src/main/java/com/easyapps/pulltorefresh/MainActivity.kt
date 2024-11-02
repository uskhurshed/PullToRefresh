package com.easyapps.pulltorefresh

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val scrollView = findViewById<PullDownNestedScrollView>(R.id.customRefreshScrollView)

        scrollView.setOnRefreshListener(object : PullDownNestedScrollView.OnRefreshListener {
            override fun onRefresh() {
                Toast.makeText(this@MainActivity, "Обновление...", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    scrollView.setRefreshing(false) // Завершение обновления
                }, 2000)
            }
        })


    }
}