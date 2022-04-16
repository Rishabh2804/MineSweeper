package com.example.minesweeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class Levels : AppCompatActivity() {

    private var level = Difficulties.NONE

    private val colors = arrayOf(
        R.color.green,
        R.color.cyan,
        R.color.red,
        R.color.white,
        R.color.selected
    )

    private val buttons = arrayOf<Button>(
        findViewById(R.id.easy),
        findViewById(R.id.medium),
        findViewById(R.id.hard),
        findViewById(R.id.custom)
    )

    private val playButton: Button = findViewById(R.id.startGame)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)

        playButton.isEnabled = false // Game can't be started without difficulty selection
        buttons.forEachIndexed { i, button ->
            button.setOnClickListener {
                playButton.isEnabled = true
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

            if (level == Difficulties.CUSTOM) {
                customValues.forEach {
                    if (it.text.isBlank()) {
                        Toast.makeText(this, "Constraints field is empty!!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                val intent = Intent(this, GameScreen::class.java).apply {
                    putExtra("Board Type", level.ordinal)
                    if (level == Difficulties.CUSTOM) {
                        putExtra("Rows", customValues[0].text.toString().toInt())
                        putExtra("Columns", customValues[1].text.toString().toInt())
                        putExtra("Mines", customValues[2].text.toString().toInt())
                    }
                }
                startActivity(intent)
            }
        }
    }

    private fun alert() {
        Toast.makeText(this, "Alert Message!!", Toast.LENGTH_SHORT).show()
    }

    private fun updateLayout(level: Difficulties) {
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

