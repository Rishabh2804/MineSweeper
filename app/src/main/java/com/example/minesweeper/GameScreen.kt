package com.example.minesweeper

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import com.example.minesweeper.structured_templates.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.random.Random

class GameScreen : AppCompatActivity() {

    // Game variables
    private var rows = 0
    private var columns = 0
    private var mines = 0
    private var flags = 0
    private var revealedCells = 0

    // Game state variables
    private var gameStatus = Status.NOT_STARTED
    private lateinit var mode: MineMode

    // In-game stats indicators
    private lateinit var mineIndicator: TextView
    private lateinit var flagBombSwitch: ImageButton
    private lateinit var clock: Chronometer
    private lateinit var restart: ImageButton

    // Game board
    private lateinit var mineField: Array<Array<MineFieldCell>>
    private lateinit var board: LinearLayout

    // Color pallet for guider values (1 - 8)
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

    // Shared preferences to get
    // Difficulty-wise game board dimensions
    private lateinit var preferences: SharedPreferences
    private val difficulties = arrayOf(
        Dimensions(8, 8, 10),
        Dimensions(12, 12, 25),
        Dimensions(16, 16, 40),
    )

    // Direction map -> xDir : horizontal, yDir : vertical
    // To travel across all 8 adjacent cells of a cell
    private val xDir = arrayOf(0, 1, 0, -1, -1, 1, 1, -1)
    private val yDir = arrayOf(1, 0, -1, 0, 1, 1, -1, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        val intent = intent

        /* Set the mine field according to difficulty level
        *  chosen by user on previous screen.
        */
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

        // Number of flags would be exactly equal to number of mines
        flags = mines

        preferences = getSharedPreferences("GameStats", Context.MODE_PRIVATE)

        // In-game stats indicators
        board = findViewById(R.id.playingArea)
        clock = findViewById(R.id.timer)
        mineIndicator = findViewById(R.id.mines)
        mineIndicator.text = mines.toString()

        // mine mode to reveal or flag a cell
        mode = MineMode.REVEAL // Reveal mode by default

        // Switch to transition between flag and reveal modes
        flagBombSwitch = findViewById(R.id.flagBombSwitch)
        flagBombSwitch.setOnClickListener {

            // Toggle mine mode on click
            mode = if (mode == MineMode.FLAG)
                MineMode.REVEAL
            else
                MineMode.FLAG

            // Change the image of the switch accordingly
            if (mode == MineMode.FLAG) {
                flagBombSwitch.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.flag
                    )
                )
            } else {
                flagBombSwitch.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.cutebomb
                    )
                )
            }
        }

        // Restart button to reset the game
        restart = findViewById(R.id.restart)
        restart.setOnClickListener {
            restartGame()
        }

        // Prepare the playing field for the game
        setField()
    }

    private fun setField() { // Initialize the playing field

        // 2-D array to store the cells
        mineField = Array(rows) { Array(columns) { MineFieldCell(this) } }

        // Layout parameters for the mine field
        val buttonParams = LinearLayout.LayoutParams(

            0, // Width according to number of columns

            // Vertically fully stretched cell to fill the
            // row width of the playing field
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val horizonParams = LinearLayout.LayoutParams(
            // Horizontally fully stretched cell to fill the
            // column width of the playing field
            LinearLayout.LayoutParams.MATCH_PARENT,

            0 // Height according to the number of rows
        )

        // Provide unique IDs to each cell
        var id = 1 // ID = 1 for cell at (0,0)

        for (i in 0 until rows) {
            // Each row is a LinearLayout
            // to be added to the playing field
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = horizonParams
            linearLayout.orientation = LinearLayout.HORIZONTAL

            horizonParams.weight = 1f // Equal width for each row

            for (j in 0 until columns) {
                // Each cell is a Button view
                // to be added to the row linear layout
                val button = MineFieldCell(this)

                button.id = id // Set the ID to the cell
                button.layoutParams = buttonParams
                buttonParams.weight = 1.0F // Equal width for each cell

                button.setBackgroundResource(R.drawable.unrevealedcell) // Cell unrevealed by default
                linearLayout.addView(button)//cell added to layout

                button.setOnClickListener {
                    if (gameStatus == Status.NOT_STARTED) {
                        // Start the game as and when the first cell is clicked
                        startTimer()
                        //update the status to ongoing
                        gameStatus = Status.ONGOING
                        Toast.makeText(this, "Don't die too soon!!", Toast.LENGTH_SHORT).show()
                        setMines(i, j) // Set the mines in the field
                    }
                    //function called to make a move
                    makeMove(i, j)
                    //function called to display the board
                    displayBoard()
                    //check if the user has won the game
                    checkWin()
                    //if status is not ongoing, then show the result as the game is over
                    if (gameStatus != Status.ONGOING) {
                        showResult()
                    }
                }
                id++
                mineField[i][j] = button // Store the cell in the 2-D array
            }
            // Add the row to the playing field
            board.addView(linearLayout)
        }
    }

    private fun showResult() {
        //stop the clock as the game is over
        clock.stop()

        //calculate the time taken by the user
        val timeElapsed = ((SystemClock.elapsedRealtime() - clock.base).toInt()) / 1000

        //get the best time and best score through shared preferences
        var bestTime = preferences.getInt("best_time", 0)
        var bestScore = preferences.getInt("best_score", 0)

        //if the time taken by the user is less than the best time, then update the best time, but only if the user won
        if (gameStatus == Status.WON && (timeElapsed < bestTime || bestTime == 0)) {
            bestTime = timeElapsed
            preferences.edit().putInt("best_time", bestTime).apply()
        }

        //to avoid the clicking of cell if the game is over
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                mineField[i][j].isEnabled = false
            }
        }

        val layout = this.layoutInflater.inflate(R.layout.score_card, null)
        val resultInWords = layout.findViewById<TextView>(R.id.result_in_words)
        //get the name of the user through shared preferences
        val name = preferences.getString("Name", "")

        //alert dialog to display the result
        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setCancelable(false)
            .create()

        val timeTaken = layout.findViewById<TextView>(R.id.user_time)
        val bestTimeTaken = layout.findViewById<TextView>(R.id.user_best_time)

        //if best time is 0, user has not won the game yet
        if (bestTime == 0)
            "No best time yet".also { bestTimeTaken.text = it }
        else {
            //calculate the best minutes and seconds
            val minute = bestTime / 60
            val second = bestTime % 60

            //add 0 if minutes or seconds are less than 10 to prevent logical errors
            if (minute < 10 && second < 10)
                "0$minute:0$second".also { bestTimeTaken.text = it }
            else if (minute < 10)
                "0$minute:$second".also { bestTimeTaken.text = it }
            else if (second < 10)
                "$minute:0$second".also { bestTimeTaken.text = it }
            else
                "$minute:$second".also { bestTimeTaken.text = it }
            bestTimeTaken.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
        val currentScore = layout.findViewById<TextView>(R.id.user_score)
        val bestScoreTextview = layout.findViewById<TextView>(R.id.user_best_score)
        val bonusOrPenalty = layout.findViewById<TextView>(R.id.user_bonus_score)
        if (gameStatus == Status.WON) {
            val text1 = "Well Done, "
            val text2 = "$name!!"
            //printing the result in words
            val text = this.getString(R.string.some_text, text1, text2)
            resultInWords.text = (HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))

            //calculate the current time in minutes and seconds
            val minute = timeElapsed / 60
            val second = timeElapsed % 60
            if (minute < 10 && second < 10)
                "0$minute:0$second".also { timeTaken.text = it }
            else if (minute < 10)
                "0$minute:$second".also { timeTaken.text = it }
            else if (second < 10)
                "$minute:0$second".also { timeTaken.text = it }
            else
                "$minute:$second".also { timeTaken.text = it }

            //if current time is the best time, keep the colour of both as same
            if (timeElapsed == bestTime) {
                timeTaken.setTextColor(ContextCompat.getColor(this, R.color.red))
            } else {
                timeTaken.setTextColor(ContextCompat.getColor(this, R.color.green))
            }

            //our expected time to win the game
            val expectedTime = (rows * columns * mines) / 2

            //winning score given by the formula
            val winScore = (rows * rows) + (columns * columns) + (mines * mines)
            //bonus score if user wins in less time
            val bonusScore = (expectedTime - timeElapsed)

            //total score
            val score = winScore + bonusScore
            currentScore.text = "$score"

            val bonusOrPenaltyIcon = layout.findViewById<TextView>(R.id.bonus)
            if (bonusScore > 0) {
                //if bonus score is positive, show as bonus
                "Bonus Score ".also { bonusOrPenaltyIcon.text = it }
                bonusOrPenaltyIcon.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_green_light
                    )
                )
                bonusOrPenalty.text = "$bonusScore"
            } else {
                //if bonus score is negative, show as penalty
                "Penalty  ".also { bonusOrPenaltyIcon.text = it }
                bonusOrPenaltyIcon.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_red_light
                    )
                )
                bonusOrPenalty.text = "${-bonusScore}"
            }
            bonusOrPenalty.setTextColor(ContextCompat.getColor(this, R.color.black))

            //update the best time if not updated or if user won with better score
            if (score > bestScore || bestScore == 0) {
                bestScore = score
                preferences.edit().putInt("best_score", bestScore).apply()
            }
            currentScore.text = "$score"
            "$bestScore".also { bestScoreTextview.text = it }

            //if score is same as best score, print them in same colour
            if (bestScore == score) {
                currentScore.setTextColor(ContextCompat.getColor(this, R.color.red))
                bestScoreTextview.setTextColor(ContextCompat.getColor(this, R.color.red))
            } else {
                currentScore.setTextColor(ContextCompat.getColor(this, R.color.green))
                bestScoreTextview.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        } else {
            //printing the result in words
            val text1 = "Better Luck Next Time, "
            val text2 = "$name"
            val text = this.getString(R.string.some_text, text1, text2)
            resultInWords.text = (HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))

            //not showing the time taken as user lost
            timeTaken.text = "N/A"
            //current score=0
            currentScore.text = 0.toString()
            "$bestScore".also { bestScoreTextview.text = it }
            bestScoreTextview.setTextColor(ContextCompat.getColor(this, R.color.red))
            bonusOrPenalty.text = "N/A"
        }
        //to delay the result screen for a short period of time
        Handler().postDelayed({
            dialog.show()
        }, 512)

        //if user wants to play a new game, direct him to the screen of choosing the levels
        val newGame = layout.findViewById<Button>(R.id.newGame)
        newGame.setOnClickListener {
            finish()
            val intent = Intent(this, Levels::class.java)
            startActivity(intent)
        }

        //if user wants to replay, restart the game
        val replay = layout.findViewById<Button>(R.id.replay)
        replay.setOnClickListener {
            finish()
            startActivity(intent)
        }

        //if user wants to go to the main menu, direct him to the main menu
        val exit = layout.findViewById<Button>(R.id.mainMenu)
        exit.setOnClickListener {
            finish()
        }

        //if user wants to share the result with his friends, share the result
        val shareSS = layout.findViewById<ImageButton>(R.id.share)
        shareSS.setOnClickListener {
            val view = window.decorView
            shareScreenShot(view)
        }
    }

    private fun displayBoard() {
        for (i in 0 until rows)
            for (j in 0 until columns) {
                with(mineField[i][j]) {
                    //if the cell is revealed, call this function
                    if (isRevealed) {
                        displayRevealedCell(mineField[i][j])
                    } else {
                        if (isFlagged) {
                            //if user lost and at the flag there was not a mine, then display the falling flag
                            // else display the right flag
                            if (gameStatus == Status.LOST && !isMine)
                                setBackgroundResource(R.drawable.wrong_flag)
                            else
                                setBackgroundResource(R.drawable.right_flag)
                        } else if (gameStatus == Status.LOST && isMine) {
                            //if user lost and at the un-flagged cell there was a mine, then display a dangerous looking mine
                            setBackgroundResource(R.drawable.darawnabomb)
                        } else {
                            //have an unrevealed cel otherwise
                            setBackgroundResource(R.drawable.unrevealedcell)
                        }
                    }
                }
            }
    }

    private fun makeMove(i: Int, j: Int) {
        if (mode == MineMode.FLAG) {

            //if it is already revealed, do nothing
            if (mineField[i][j].isRevealed)
                return

            //if it is flagged, then unflag it and increase the number of flags
            if (mineField[i][j].isFlagged) {
                mineField[i][j].isFlagged = false
                flags++
                mineIndicator.text = flags.toString()
                return
            } else {
                //if it is not flagged, then flag it and decrease the number of flags
                //if the number of flags is 0, then warn the user that he can't flag
                if (flags <= 0) {
                    Toast.makeText(this, "No more flags left!!", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    mineField[i][j].isFlagged = true
                    flags--
                    mineIndicator.text = flags.toString()
                }
            }
        } else {
            //if the cell is revealed or flagged, do nothing
            if (mineField[i][j].isRevealed || mineField[i][j].isFlagged)
                return

            //if cell was mine, user lost the game
            if (mineField[i][j].isMine) {
                gameStatus = Status.LOST
            } else {
                //if the cell is not mine, then reveal it
                reveal(i, j)
            }
        }
    }

    private fun checkWin() = if (((rows * columns) - revealedCells) == mines) {
        //formula to check if the user won
        //update the game status
        gameStatus = Status.WON
    } else {
        // Do Nothing.
    }

    private fun reveal(i: Int, j: Int) {
        //if you got out of the board, return
        if (i < 0 || i >= rows || j < 0 || j >= columns)
            return
        //if the cell is flagged, mine, or already revealed, return
        if (mineField[i][j].isRevealed || mineField[i][j].isFlagged || mineField[i][j].isMine)
            return
        //increase the number of revealed cells
        revealedCells++
        mineField[i][j].isRevealed = true
        if (mineField[i][j].value != 0)
            return
        for (k in 0 until 8) {
            //recursively reveal the cells around the current cell
            val x = i + xDir[k]
            val y = j + yDir[k]
            reveal(x, y)
        }
    }

    //start the timer
    private fun startTimer() {
        clock.base = SystemClock.elapsedRealtime()
        clock.start()
    }

    private fun displayRevealedCell(mineFieldCell: MineFieldCell) {
        with(mineFieldCell) {
            when (value) {
                //if the value is 0, then display empty space
                0 -> setBackgroundResource(R.drawable.shape)
                else -> {
                    //display the number of mines around the cell
                    text = value.toString()
                    //size according to the grid size
                    textSize = when {
                        rows <= 10 -> 36f
                        rows <= 15 -> 28f
                        else -> 20f
                    }
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setBackgroundResource(R.drawable.shape)
                    setTextColor(ContextCompat.getColor(context, cellColors[value - 1]))
                }

            }
        }
    }

    private fun checkSafeNeighbour(i: Int, j: Int): Boolean {

        //to prevent keeping mine at the cell which has a mine in all 8 adjacent cells
        for (k in 0 until 8) {
            val x = i + xDir[k]
            val y = j + yDir[k]
            if (x in 0 until rows && y in 0 until columns) {
                if (!mineField[x][y].isMine) {
                    return true
                }
            }
        }
        return false
    }

    private fun setMines(i: Int, j: Int) {

        var mine = 0
        while (mine < mines) {
            //generating random numbers for mine placement
            val x = Random(System.nanoTime()).nextInt(0, rows)
            val y = Random(System.nanoTime()).nextInt(0, columns)

            if (x != i && y != j) {
                //preventing mine placement at the cell which has a mine in adjacent cell
                if (x in i - 1 until i + 2 && y in j - 1 until j + 2)
                    continue
                //place the mine otherwise
                if (!mineField[x][y].isMine && checkSafeNeighbour(x, y)) {
                    mineField[x][y].isMine = true
                    mineField[x][y].value = -1
                    mine++
                    plantValues(x, y)
                }
            }
        }
    }

    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this)
        with(dialog) {
            //confirming if the user wants to exit the game
            setTitle("Are you sure you want to quit?")
            //if yes, then exit the game
            setPositiveButton("Yes") { _, _ ->
                finish()
                val intent = Intent(this@GameScreen, Levels::class.java)
                startActivity(intent)
            }
            //if no, then continue the game
            setNegativeButton("No") { _, _ ->
                // Do Nothing
            }
        }
        dialog.show()
    }


    private fun plantValues(i: Int, j: Int) {
        //planting values around the cell
        for (k in 0 until 8) {
            val x = i + xDir[k]
            val y = j + yDir[k]
            if (x in 0 until rows && y in 0 until columns) {
                if (!mineField[x][y].isMine) {
                    mineField[x][y].value++
                }
            }
        }
    }

    private fun restartGame() {
        val dialog = AlertDialog.Builder(this)
        with(dialog) {
            //confirming if the user wants to restart the game
            setTitle("Are you sure you want to restart?")
            //if yes, then restart the game
            setPositiveButton("Yes") { _, _ ->
                finish()
                startActivity(intent)
            }
            //if no, then continue the game
            setNegativeButton("No") { _, _ ->
                // Do Nothing
            }
        }
        dialog.show()
    }

    private fun shareScreenShot(view: View) {

        // Generate image file credentials
        val date = Date()
        val format = DateFormat.format("dd-MM-yyyy_hh:mm:ss", date)

        // Search for 'Screenshots' directory in the external storage
        val dir = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")
        if (!dir.exists())
            dir.mkdirs() // Create 'Screenshots' directory if not found

        // Create image file name using current date and time as credential IDs
        val path = "$dir/${format}.png"
        view.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(view.drawingCache)
        view.isDrawingCacheEnabled = false

        // Generate bitmap image file to be written on specified path
        val imageFile = File(path)

        // Attach out-stream to the file
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)

        // Clear and close the output stream
        outputStream.flush()
        outputStream.close()

        // Generate user resource identifier for sharing the image
        val uri = FileProvider.getUriForFile(
            this,
            "com.example.minesweeper.$localClassName.provider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        // Message to be displayed at share destination
        intent.putExtra(Intent.EXTRA_TEXT, "Hey! Check out this awesome Minesweeper game I played!")
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

}