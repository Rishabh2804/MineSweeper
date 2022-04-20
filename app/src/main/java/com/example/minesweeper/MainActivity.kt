package com.example.minesweeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startGame = findViewById<Button>(R.id.enter)
        val textview= findViewById<EditText>(R.id.username)
        startGame.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
        startGame.setOnClickListener{
            val intent = Intent(this, Levels::class.java)

            if(textview.text.isEmpty()){
                intent.putExtra("Name", "Player1")
            }
            else{
                intent.putExtra("Name", textview.text.toString())
            }
            startActivity(intent)
        }
    }
}