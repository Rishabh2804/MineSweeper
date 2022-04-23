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
import com.example.minesweeper.enums.Difficulties

class Levels : AppCompatActivity() {
    private var username: String? = ""
    private var level = Difficulties.NONE

    private val colors = arrayOf(
        R.color.cyan,
        R.color.yellow,
        R.color.red,
        R.color.black,
        R.color.un_selected
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)
        val intent: Intent = intent
        username = intent.getStringExtra("Name")
        val buttons = arrayOf<Button>(
            this.findViewById(R.id.easy),
            this.findViewById(R.id.medium),
            this.findViewById(R.id.hard),
            this.findViewById(R.id.custom)
        )

        val playButton: Button = findViewById(R.id.startGame)

        playButton.isEnabled = false // Game can't be started without difficulty selection
        updateLayout(level, buttons, playButton)

        buttons.forEachIndexed { i, button ->
            button.setOnClickListener {
                playButton.isEnabled = true
                level = Difficulties.values()[i]
                updateLayout(level, buttons, playButton)
            }
        }

        val whiteSpace = findViewById<ConstraintLayout>(R.id.EmptySpace)
        whiteSpace.setOnClickListener {
            level = Difficulties.NONE
            playButton.isEnabled = false
            updateLayout(level, buttons, playButton)
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
            lateinit var intent: Intent
            if (level == Difficulties.CUSTOM) {
                val rows = customValues[0].text.toString().toInt()
                val cols = customValues[1].text.toString().toInt()
                val mines = customValues[2].text.toString().toInt()

                intent = Intent(this, GameScreen::class.java)
                intent.putExtra("Difficulty", level.ordinal)
                intent.putExtra("Rows", rows)
                intent.putExtra("Columns", cols)
                intent.putExtra("Mines", mines)
                intent.putExtra("Name", username)
            } else {
                intent = Intent(this, GameScreen::class.java).apply {
                    putExtra("Difficulty", level.ordinal)
                    putExtra("Name", username)
                }
            }
            finish()
            startActivity(intent)
        }
    }

    private fun alert() {
        Toast.makeText(this, "Alert Message!!", Toast.LENGTH_SHORT).show()
    }

    private fun updateLayout(level: Difficulties, buttons: Array<Button>, playbutton: Button) {
        for (i in 0..3) {
            if (i == level.ordinal) {
                buttons[i].setBackgroundColor(ContextCompat.getColor(this, colors[i]))
            } else {
                buttons[i].setBackgroundColor(ContextCompat.getColor(this, R.color.un_selected))
            }
        }

        val customDiffMenu = findViewById<ConstraintLayout>(R.id.customDiff)
        customDiffMenu.isVisible = level == Difficulties.CUSTOM
        customDiffMenu.setOnClickListener {
            if(level == Difficulties.CUSTOM){
                it.isVisible= true
            }
        }

        if (playbutton.isEnabled) {
            playbutton.setBackgroundColor(ContextCompat.getColor(this, R.color.play_button_enabled))
        } else {
            playbutton.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.play_button_disabled
                )
            )
        }

    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}

