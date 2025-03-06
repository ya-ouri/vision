package com.ouri.vision

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = resources.getColor(R.color.cool_black, theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {
            // Открываем экран выбора фильтра
            val intent = Intent(this, ChoiceVisionActivity::class.java)
            startActivity(intent)
        }
    }
}
