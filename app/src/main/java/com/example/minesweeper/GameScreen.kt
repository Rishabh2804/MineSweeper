package com.example.minesweeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class GameScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        val intent = intent
        val difficulty = intent.getStringExtra("difficulty")
        val rows = intent.getIntExtra("Rows", 0)
        val cols = intent.getIntExtra("Columns", 0)
        val mines = intent.getIntExtra("Mines", 0)

        Toast.makeText(this, "Difficulty: $difficulty\nRows: $rows\nColumns: $cols\nMines: $mines", Toast.LENGTH_LONG).show()
    }
}