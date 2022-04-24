package com.example.minesweeper

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // We link the play - button view provided in the XML file using View id
        // When the play button is clicked, we start the game
        val startGame = findViewById<Button>(R.id.enter)
        startGame.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))

        // Username input field
        val textview = findViewById<EditText>(R.id.username)

        // Get the user stats using Shared Preferences
        val preferences = getSharedPreferences("GameStats", Context.MODE_PRIVATE)

        // present the input field until
        // the user logged-in using a valid username
        val loggedIn = preferences.getBoolean("loggedIn", false)
        if (loggedIn) {
            textview.isVisible = false
        }

        startGame.setOnClickListener {
            val intent = Intent(this, Levels::class.java)
            if (!loggedIn) {
                if (textview.text.isEmpty()) {
                    // In case the user didn't enter a username,
                    // 'Player1' will be used as the default username
                    preferences.edit().putString("Name", "Player1").apply()
                } else {
                    preferences.edit().putString("Name", textview.text.toString()).apply()
                    preferences.edit().putBoolean("loggedIn", true).apply()
                }
            }
            startActivity(intent) // transfer control to Difficulty - Selection Menu
        }
    }
}