package com.ouri.vision

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class ChoiceVisionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = resources.getColor(R.color.cool_black, theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_vision)

        findViewById<ImageButton>(R.id.protanopiaButton).setOnClickListener {
            applyFilter("protanopia")
        }

        findViewById<ImageButton>(R.id.deuteranopiaButton).setOnClickListener {
            applyFilter("deuteranopia")
        }

        findViewById<ImageButton>(R.id.tritanopiaButton).setOnClickListener {
            applyFilter("tritanopia")
        }
    }

    private fun applyFilter(filterType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("FILTER_TYPE", filterType)
        startActivity(intent)
    }
}


