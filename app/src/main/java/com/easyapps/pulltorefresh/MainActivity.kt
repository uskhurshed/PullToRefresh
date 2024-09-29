package com.easyapps.pulltorefresh

import android.os.Bundle
import android.view.View
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


        val scrollView = findViewById<CustomRefreshScrollView>(R.id.customRefreshScrollView)

        val header = TextView(this)
        header.text = "Pull to refresh"

        scrollView.setHeaderView(header)

        scrollView.setOnRefreshListener {
            // Логика обновления данных
            Toast.makeText(this, "Обновление...", Toast.LENGTH_SHORT).show()

            // Пример запроса данных или выполнения других задач
        }

    }
}