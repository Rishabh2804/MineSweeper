package com.example.minesweeper

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.SystemClock
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
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

    // Shared preferences

    private lateinit var preferences: SharedPreferences
    private val difficulties = arrayOf(
        Dimensions(8, 8, 10),
        Dimensions(12, 12, 25),
        Dimensions(16, 16, 40),
    )

    private val xDir = arrayOf(0, 1, 0, -1, -1, 1, 1, -1)
    private val yDir = arrayOf(1, 0, -1, 0, 1, 1, -1, -1)

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

        flags = mines

        preferences = getSharedPreferences("GameStats", Context.MODE_PRIVATE)

        board = findViewById(R.id.playingArea)
        clock = findViewById(R.id.timer)
        mode = MineMode.REVEAL //Reveal mode by default

        mineIndicator = findViewById(R.id.mines)
        mineIndicator.text = mines.toString()

        flagBombSwitch = findViewById(R.id.flagBombSwitch)
        flagBombSwitch.setOnClickListener {

            mode = if (mode == MineMode.FLAG)
                MineMode.REVEAL
            else
                MineMode.FLAG

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
        restart = findViewById(R.id.restart)
        restart.setOnClickListener {
            restartGame()
        }
        setField()
    }

    private fun setField() { // Very Important
        mineField = Array(rows) { Array(columns) { MineFieldCell(this) } }
        val buttonParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val horizonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
        var id = 1
        for (i in 0 until rows) {
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = horizonParams
            linearLayout.orientation = LinearLayout.HORIZONTAL
            horizonParams.weight = 1f
            for (j in 0 until columns) {
                val button = MineFieldCell(this)
                button.id = id
                button.layoutParams = buttonParams
                buttonParams.weight = 1.0F
                button.setBackgroundResource(R.drawable.unrevealedcell)
                linearLayout.addView(button)
                button.setOnClickListener {
                    if (gameStatus == Status.NOT_STARTED) {
                        startTimer()
                        gameStatus = Status.ONGOING
                        Toast.makeText(this, "Don't die too soon!!", Toast.LENGTH_SHORT).show()
                        setMines(i, j)
                    }
                    makeMove(i, j)
                    displayBoard()
                    checkWin()
                    if (gameStatus != Status.ONGOING) {
                        showResult()
                    }
                }
                id++
                mineField[i][j] = button
            }
            board.addView(linearLayout)
        }
    }

    private fun showResult() {
        clock.stop()
        val timeElapsed = ((SystemClock.elapsedRealtime() - clock.base).toInt()) / 1000
        var bestTime = preferences.getInt("best_time", 0)
        var bestScore = preferences.getInt("best_score", 0)
        if (gameStatus == Status.WON && (timeElapsed < bestTime || bestTime == 0)) {
            bestTime = timeElapsed
            preferences.edit().putInt("best_time", bestTime).apply()
        }
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                mineField[i][j].isEnabled = false
            }
        }
        val layout = this.layoutInflater.inflate(R.layout.score_card, null)
        val resultInWords = layout.findViewById<TextView>(R.id.result_in_words)
        val name = preferences.getString("Name", "")
        val dialog = AlertDialog.Builder(this)
            .setView(layout)
            .setCancelable(false)
            .create()
        val timeTaken = layout.findViewById<TextView>(R.id.user_time)
        val bestTimeTaken = layout.findViewById<TextView>(R.id.user_best_time)
        if (bestTime == 0)
            "No best time yet".also { bestTimeTaken.text = it }
        else {
            val minute = bestTime / 60
            val second = bestTime % 60
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
            val text = this.getString(R.string.some_text, text1, text2)
            resultInWords.text = (HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
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

            if (timeElapsed == bestTime) {
                timeTaken.setTextColor(ContextCompat.getColor(this, R.color.red))
            } else {
                timeTaken.setTextColor(ContextCompat.getColor(this, R.color.green))
            }

            val expectedTime = (rows * columns * mines) / 2

            val winScore = (rows * rows) + (columns * columns) + (mines * mines)
            val bonusScore = (expectedTime - timeElapsed)

            val score = winScore + bonusScore
            currentScore.text = "$score"

            val bonusOrPenaltyIcon = layout.findViewById<TextView>(R.id.bonus)
            if (bonusScore > 0) {
                "Bonus Score ".also { bonusOrPenaltyIcon.text = it }
                bonusOrPenaltyIcon.setTextColor(
                    ContextCompat.getColor(
                        this,
                        android.R.color.holo_green_light
                    )
                )
                bonusOrPenalty.text = "$bonusScore"
            } else {
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
            if (score > bestScore || bestScore == 0) {
                bestScore = score
                preferences.edit().putInt("best_score", bestScore).apply()
            }
            currentScore.text = "$score"
            "$bestScore".also { bestScoreTextview.text = it }
            if (bestScore == score) {
                currentScore.setTextColor(ContextCompat.getColor(this, R.color.red))
                bestScoreTextview.setTextColor(ContextCompat.getColor(this, R.color.red))
            } else {
                currentScore.setTextColor(ContextCompat.getColor(this, R.color.green))
                bestScoreTextview.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        } else {
            val text1 = "Better Luck Next Time, "
            val text2 = "$name"
            val text = this.getString(R.string.some_text, text1, text2)
            resultInWords.text = (HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY))
            timeTaken.text = "N/A"
            currentScore.text = 0.toString()
            "$bestScore".also { bestScoreTextview.text = it }
            bestScoreTextview.setTextColor(ContextCompat.getColor(this, R.color.red))
            bonusOrPenalty.text = "N/A"
        }
        Handler().postDelayed({
            dialog.show()
        }, 707)
        val newGame = layout.findViewById<Button>(R.id.newGame)
        newGame.setOnClickListener {
            finish()
            val intent = Intent(this, Levels::class.java)
            startActivity(intent)
        }

        val replay = layout.findViewById<Button>(R.id.replay)
        replay.setOnClickListener {
            finish()
            startActivity(intent)
        }

        val exit = layout.findViewById<Button>(R.id.exit)
        exit.setOnClickListener {
            finish()
        }

        val shareSS = layout.findViewById<Button>(R.id.share)
        shareSS.setOnClickListener {
            val view = window.decorView
            shareScreenShot(view)
        }
    }

    private fun displayBoard() {
        for (i in 0 until rows)
            for (j in 0 until columns) {
                with(mineField[i][j]) {
                    if (isRevealed) {
                        displayRevealedCell(mineField[i][j])
                    } else {
                        if (isFlagged) {
                            if (gameStatus == Status.LOST && !isMine)
                                setBackgroundResource(R.drawable.wrong_flag)
                            else
                                setBackgroundResource(R.drawable.right_flag)
                        } else if (gameStatus == Status.LOST && isMine) {
                            setBackgroundResource(R.drawable.darawnabomb)
                        } else {
                            setBackgroundResource(R.drawable.unrevealedcell)
                        }
                    }
                }
            }
    }

    private fun makeMove(i: Int, j: Int) {
        if (mode == MineMode.FLAG) {
            if (mineField[i][j].isRevealed)
                return

            if (mineField[i][j].isFlagged) {
                mineField[i][j].isFlagged = false
                flags++
                mineIndicator.text = flags.toString()
                return
            } else {
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
            if (mineField[i][j].isRevealed || mineField[i][j].isFlagged)
                return

            if (mineField[i][j].isMine) {
                gameStatus = Status.LOST
            } else {
                reveal(i, j)
            }
        }
    }

    private fun checkWin() = if (((rows * columns) - revealedCells) == mines) {
        gameStatus = Status.WON
    } else {
        // Do Nothing.
    }

    private fun reveal(i: Int, j: Int) {
        if (i < 0 || i >= rows || j < 0 || j >= columns)
            return
        if (mineField[i][j].isRevealed || mineField[i][j].isFlagged || mineField[i][j].isMine)
            return
        revealedCells++
        mineField[i][j].isRevealed = true
        if (mineField[i][j].value != 0)
            return
        for (k in 0 until 8) {
            val x = i + xDir[k]
            val y = j + yDir[k]
            reveal(x, y)
        }
    }

    private fun startTimer() {
        clock.base = SystemClock.elapsedRealtime()
        clock.start()
    }

    private fun displayRevealedCell(mineFieldCell: MineFieldCell) {
        with(mineFieldCell) {
            when (value) {
                0 -> setBackgroundResource(R.drawable.shape)
                else -> {
                    text = value.toString()
                    if (rows <= 10)
                        textSize = 36f
                    else if (rows <= 15)
                        textSize = 28f
                    else
                        textSize = 20f
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setBackgroundResource(R.drawable.shape)
                    setTextColor(ContextCompat.getColor(context, cellColors[value - 1]))
                }

            }
        }
    }

    private fun checkSafeNeighbour(i: Int, j: Int): Boolean {

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
            val x = Random(System.nanoTime()).nextInt(0, rows)
            val y = Random(System.nanoTime()).nextInt(0, columns)

            if (x != i && y != j) {
                if (x in i - 1 until i + 2 && y in j - 1 until j + 2)
                    continue
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
            setTitle("Are you sure you want to quit?")
            setPositiveButton("Yes") { _, _ ->
                finish()
                val intent = Intent(this@GameScreen, Levels::class.java)
                startActivity(intent)
            }
            setNegativeButton("No") { _, _ ->
                // Do Nothing
            }
        }
        dialog.show()
    }


    private fun plantValues(i: Int, j: Int) {
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
            setTitle("Are you sure you want to restart?")
            setPositiveButton("Yes") { _, _ ->
                finish()
                startActivity(intent)
            }
            setNegativeButton("No") { _, _ ->
                // Do Nothing
            }
        }
        dialog.show()
    }

    private fun shareScreenShot(view: View?) {
        val date = Date()
        val format = DateFormat.format("dd-MM-yyyy_hh:mm:ss", date)
        val dir = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots")
        if (!dir.exists())
            dir.mkdirs()
        val path = "$dir/${format}.png"
        val bitmap = view?.let {
            Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(bitmap!!)
        view?.draw(canvas)
        val imageFile = File(path)
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        intent.putExtra(Intent.EXTRA_TITLE, "Minesweeper")
        intent.putExtra(Intent.EXTRA_SUBJECT, "This made my Day!!")
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

}