package com.example.minesweeper

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.minesweeper.structured_templates.Difficulties

class Levels : AppCompatActivity() {
    private var username: String? = ""
    private var level = Difficulties.NONE

    // Color palette for difficulty levels
    private val colors = arrayOf(
        R.color.cyan, // Newbie -> Easy
        R.color.yellow, // Specialist -> Medium
        R.color.red, // Veteran -> Hard
        R.color.black, // Custom difficulty mode
        R.color.un_selected // Default color for all buttons
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)

        // Get username from previous activity
        val intent: Intent = intent
        username = intent.getStringExtra("Name")

        // Set up difficulty buttons
        val difficultyButtons = arrayOf<Button>(
            this.findViewById(R.id.easy),
            this.findViewById(R.id.medium),
            this.findViewById(R.id.hard),
            this.findViewById(R.id.custom)
        )

        // Set up play buttons
        val playButton: Button = findViewById(R.id.startGame)
        playButton.isEnabled = false // Game can't be started without difficulty selection

        updateLayout(level, difficultyButtons, playButton)

        // Define click actions for each difficulty button
        difficultyButtons.forEachIndexed { i, button ->
            button.setOnClickListener {
                // Since the difficulty selection is definite, enable the play button
                playButton.isEnabled = true

                // Update the game level to the selected difficulty level
                level = Difficulties.values()[i]

                // Update the layout to reflect the selected difficulty level
                updateLayout(level, difficultyButtons, playButton)
            }
        }

        // Handle cancelling of difficulty selection by clicking inactive area
        val whiteSpace = findViewById<ConstraintLayout>(R.id.EmptySpace)
        whiteSpace.setOnClickListener {
            // Reset all game values
            level = Difficulties.NONE
            playButton.isEnabled = false
            updateLayout(level, difficultyButtons, playButton)
        }

        /* Incrementers for custom mode properties
         * viz., number of rows, number of columns, and number of mines
         */
        val increes = arrayOf<ImageButton>(
            findViewById(R.id.moreRows), // +1 row
            findViewById(R.id.moreCols), // +1 column
            findViewById(R.id.moreMines) // +1 mine
        )

        /* Decrementers for custom mode properties
         * viz., number of rows, number of columns, and number of mines
         */
        val decrees = arrayOf<ImageButton>(
            findViewById(R.id.lessRows), // -1 row
            findViewById(R.id.lessCols), // -1 column
            findViewById(R.id.lessMines) // -1 mine
        )

        /* Text views for custom mode properties
         * viz., number of rows, number of columns, and number of mines
         */
        val customValues = arrayOf<TextView>(
            findViewById(R.id.noOfRows), // Number of rows
            findViewById(R.id.noOfCols), // Number of columns
            findViewById(R.id.noOfMines) // Number of mines
        )

        increes.forEachIndexed { i, button ->
            button.setOnClickListener {
                val curr = customValues[i].text.toString().toInt()
                if (curr == 20) { // Max value
                    maxAlert(  // Alert user that max value has been reached
                        when (i) {
                            0 -> "rows"
                            1 -> "columns"
                            2 -> "mines"
                            else -> "error"
                        }
                    )

                } else { // Increment value and update text view
                    (curr + 1).toString().also { customValues[i].text = it }
                }
            }
        }

        decrees.forEachIndexed { i, button ->
            button.setOnClickListener {
                val curr = customValues[i].text.toString().toInt()
                if (curr == 5) {  // Min value
                    minAlert(  // Alert user that min value has been reached
                        when (i) {
                            0 -> "rows"
                            1 -> "columns"
                            2 -> "mines"
                            else -> "error"
                        }
                    )
                } else { // Decrement value and update text view
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

                /* Pass the custom mode properties to the game activity
                 * and start the game activity
                 */
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

            /* Close the current activity to
             *prevent redundant activity in the back stack
             */
            finish()

            startActivity(intent)
        }
    }

    private fun minAlert(arg: String) {
        /* Alert user that min value has been reached
         * and do not allow the user to increment the value
         */
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Threshold value reached")
        alert.setMessage("You cannot have less than 5 $arg")
        alert.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }

    private fun maxAlert(arg: String) {

        /* Alert user that max value has been reached
         * and do not allow the user to increment the value
         */
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Maximum value reached")
        alert.setMessage("You cannot have more than 20 $arg")
        alert.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }

    private fun updateLayout(level: Difficulties, buttons: Array<Button>, playbutton: Button) {
        for (i in 0..3) {
            /* Display all buttons with same color except
             * the selected button, which is displayed
             * according to color pallet
             */
            if (i == level.ordinal) {
                buttons[i].setBackgroundColor(ContextCompat.getColor(this, colors[i]))
            } else {
                buttons[i].setBackgroundColor(ContextCompat.getColor(this, R.color.un_selected))
            }
        }

        /* In case of custom mode, display the custom values
         * and set the custom values selection menu to visible
         */
        val customDiffMenu = findViewById<ConstraintLayout>(R.id.customDiff)
        customDiffMenu.isVisible = level == Difficulties.CUSTOM
        customDiffMenu.setOnClickListener {
            if (level == Difficulties.CUSTOM) {
                it.isVisible = true
            }
        }

        /* Handle the functionality of play button;
         * if the user has selected a level,
         * then the play button is enabled
         */
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
        /* Prevent redundant activity in the back stack
         * by closing the current activity
         */
        finish()
        super.onBackPressed()
    }
}