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

        val preferences= getSharedPreferences("GameStats", Context.MODE_PRIVATE)
        val startGame = findViewById<Button>(R.id.enter)
        val textview= findViewById<EditText>(R.id.username)
        startGame.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        val loggedIn= preferences.getBoolean("loggedIn", false)
        if(loggedIn){
            textview.isVisible= false
        }
        startGame.setOnClickListener{
            val intent = Intent(this, Levels::class.java)
            if(!loggedIn) {
                if (textview.text.isEmpty()) {
                    preferences.edit().putString("Name", "Player1").apply()
                } else {
                    preferences.edit().putString("Name", textview.text.toString()).apply()
                    preferences.edit().putBoolean("loggedIn", true).apply()
                }
            }
            startActivity(intent)
        }
    }
}