package com.example.minesweeper

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class Levels : AppCompatActivity() {

    var level = Difficulties.NONE

    val colors = arrayOf<Int>(
        R.color.green,
        R.color.cyan,
        R.color.red,
        R.color.white,
        R.color.selected
    )

    val buttons = arrayOf<Button>(
        findViewById<Button>(R.id.easy),
        findViewById<Button>(R.id.medium),
        findViewById<Button>(R.id.hard),
        findViewById<Button>(R.id.custom)
    )

    val playButton = findViewById<Button>(R.id.startGame)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)


        buttons.forEachIndexed { i, button ->
            button.setOnClickListener {
                level = Difficulties.values()[i]
                updateLayout(level)
            }
        }

        val increes = arrayOf<ImageButton>(
            findViewById(R.id.moreRows),
            findViewById(R.id.moreCols),
            findViewById(R.id.moreMines)
        )

        val decrees = arrayOf<ImageButton>(
            findViewById(R.id.lessRows),
            findViewById(R.id.lessCols),
            findViewById(R.id.lessMines)
        )

        val customValues = arrayOf<TextView>(
            findViewById(R.id.noOfRows),
            findViewById(R.id.noOfCols),
            findViewById(R.id.noOfMines)
        )

        increes.forEachIndexed { i, button ->
            button.setOnClickListener {
                val curr = customValues[i].text.toString().toInt()
                if (curr == 20) {
                    alert()
                } else {
                    (curr + 1).toString().also { customValues[i].text = it }
                }
            }
        }

        decrees.forEachIndexed { i, button ->
            button.setOnClickListener {
                val curr = customValues[i].text.toString().toInt()
                if (curr == 5) {
                    alert()
                } else {
                    (curr - 1).toString().also { customValues[i].text = it }
                }
            }
        }

        playButton.setOnClickListener {
            if(level == Difficulties.CUSTOM){

            }
            val intent = Intent(this, GameScreen::class.java).apply {
                putExtra("Board Type", level.ordinal)
            }
        }


    }

    private fun alert() {
        TODO("Not yet implemented")
    }

    fun updateLayout(level: Difficulties) {
        for (i in 0..3) {
            if (i == level.ordinal) {
                buttons[i].setBackgroundColor(ContextCompat.getColor(this, colors[4]))
            } else {
                buttons[i].setBackgroundColor(ContextCompat.getColor(this, colors[i]))
            }
        }

        val customDiffMenu = findViewById<ConstraintLayout>(R.id.customDiff)
        customDiffMenu.isVisible = level == Difficulties.CUSTOM
    }

    fun handleCustom() {

    }
}

