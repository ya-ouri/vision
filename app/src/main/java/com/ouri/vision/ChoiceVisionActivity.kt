package com.ouri.vision

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import androidx.cardview.widget.CardView

class ChoiceVisionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = resources.getColor(R.color.cool_black, theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_vision)


        findViewById<CardView>(R.id.protanopiaButton).setOnClickListener {
            applyFilter("protanopia")
        }

        findViewById<CardView>(R.id.deuteranopiaButton).setOnClickListener {
            applyFilter("deuteranopia")
        }

        findViewById<CardView>(R.id.tritanopiaButton).setOnClickListener {
            applyFilter("tritanopia")
        }
        findViewById<CardView>(R.id.achromatopsiaButton).setOnClickListener {
            applyFilter("achromatopsia")
        }
        findViewById<CardView>(R.id.dogButton).setOnClickListener {
            applyFilter("dog")
        }
        findViewById<CardView>(R.id.catButton).setOnClickListener {
            applyFilter("cat")
        }
    }

    private fun applyFilter(filterType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("FILTER_TYPE", filterType)
        startActivity(intent)
    }
}


