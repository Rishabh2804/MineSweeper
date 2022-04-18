package com.example.minesweeper

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.core.content.ContextCompat

class GameScreen : AppCompatActivity() {
    var firstMove: Boolean = true

    private lateinit var preferences: SharedPreferences
    private val cellColors = arrayOf(
        R.color.color1,
        R.color.color2,
        R.color.color3,
        R.color.color4,
        R.color.color5,
        R.color.color6,
        R.color.color7,
        R.color.color8,
    )

    private var rows = 0
    private var columns = 0
    private var mines = 0
    private var Status = status.NOT_STARTED

    private var flagMode = false
    private lateinit var flagBombSwitch: ImageButton
    private val difficulties = arrayOf(
        Dimensions(8, 8, 10),
        Dimensions(12, 12, 25),
        Dimensions(16, 16, 40),
    )

    private lateinit var MineField: Array<Array<MineFieldCell>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        val intent = intent
        val difficulty = intent.getIntExtra("Difficulty", 5)
        if (difficulty == Difficulties.CUSTOM.ordinal) {
            rows = intent.getIntExtra("Rows", 0)
            columns = intent.getIntExtra("Columns", 0)
            mines = intent.getIntExtra("Mines", 0)
        } else {

            rows = difficulties[difficulty].rows
            columns = difficulties[difficulty].columns
            mines = difficulties[difficulty].mines
        }

        val noOfMines = findViewById<TextView>(R.id.mines)
        noOfMines.text = mines.toString()

        flagBombSwitch = findViewById(R.id.flagBombSwitch)
        flagBombSwitch.setOnClickListener {
            flagMode = !flagMode
            if (flagMode) {
                flagBombSwitch.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.cutebomb
                    )
                )
            } else {
                flagBombSwitch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.flag))
            }
        }


    }

    private fun setField() { // Very Important
        MineField = Array(rows) { Array(columns) { MineFieldCell(this) } }
        val buttonParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val horizonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )

        var id = 1

    }

    private fun updateBoard(mineFieldCell: MineFieldCell) {
        with(mineFieldCell) {
            when (value) {
                0 -> text = ""
                else -> {
                    text = value.toString()
                    setTextColor(ContextCompat.getColor(context, cellColors[value - 1]))
                }

            }
        }

    }
}