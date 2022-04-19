package com.example.minesweeper

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.core.content.ContextCompat
import kotlin.random.Random

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
    private var flags = 0
    private var revealedCells = 0
    private lateinit var noOfMines: TextView
    private var Status = status.NOT_STARTED

    val xDir = arrayOf(0, 1, 0, -1, -1, 1, 1, -1)
    val yDir = arrayOf(1, 0, -1, 0, 1, 1, -1, -1)

    private var flagMode = false
    private lateinit var flagBombSwitch: ImageButton
    private val difficulties = arrayOf(
        Dimensions(8, 8, 10),
        Dimensions(12, 12, 25),
        Dimensions(16, 16, 40),
    )

    private lateinit var MineField: Array<Array<MineFieldCell>>
    private lateinit var Board: LinearLayout
    private lateinit var Clock: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_screen)

        val intent = intent

        val difficulty = intent.getIntExtra("Difficulty", 5)
        if (difficulty == Difficulties.CUSTOM.ordinal) {
            rows = intent.getIntExtra("Rows", 0)
            columns = intent.getIntExtra("Columns", 0)
            mines = intent.getIntExtra("Mines", 0)
            flags = mines
        } else {
            rows = difficulties[difficulty].rows
            columns = difficulties[difficulty].columns
            mines = difficulties[difficulty].mines
            flags = mines
        }

        Board = findViewById(R.id.playingArea)
        Clock = findViewById(R.id.timer)

        noOfMines = findViewById(R.id.mines)
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
        for (i in 0 until rows) {
            val linearLayout = LinearLayout(this)
            linearLayout.layoutParams = horizonParams
            linearLayout.orientation = LinearLayout.HORIZONTAL

            for (j in 0 until columns) {
                val button = MineFieldCell(this)
                button.id = id
                button.layoutParams = buttonParams
                buttonParams.weight = 1.0F
                button.setOnClickListener {
                    if (Status == status.NOT_STARTED) {
                        startTimer()
                        Status = status.ONGOING
                        Toast.makeText(this, "Don't die too soon!!", Toast.LENGTH_SHORT).show()
                        setMines(i, j)
                    }
                    makeMove(i, j)

                    displayBoard()
                    checkWin()
                    if (Status != status.ONGOING) {
                        showResult()
                    }

                }
                id++
                MineField[i][j] = button

                linearLayout.addView(button)
            }
            Board.addView(linearLayout)

        }
    }

    private fun showResult() {


    }

    private fun displayBoard() {
        TODO("Not yet implemented")
    }

    private fun makeMove(i: Int, j: Int) {
        if (flagMode) {
            if (MineField[i][j].isRevealed)
                return

            if (MineField[i][j].isFlagged) {
                MineField[i][j].isFlagged = false
                flags++
                noOfMines.text = flags.toString()
                return
            } else {
                if (flags <= 0) {
                    Toast.makeText(this, "No more flags left!!", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    MineField[i][j].isFlagged = true
                    flags--
                    noOfMines.text = flags.toString()
                }
            }
        } else {
            if (MineField[i][j].isRevealed || MineField[i][j].isFlagged)
                return

            if (MineField[i][j].isMine) {
                Status = status.LOST
            } else {
                revealedCells++
                reveal(i, j)
            }
        }
    }

    private fun checkWin() = if (rows * columns - revealedCells == mines) {
        Status = status.WON
    } else {
        // Do Nothing.
    }

    private fun reveal(i: Int, j: Int) {
        if (MineField[i][j].isRevealed || MineField[i][j].isFlagged || MineField[i][j].isMine)
            return

        MineField[i][j].isRevealed = true
        for (k in 0 until 8) {
            var x = i + xDir[k]
            var y = j + yDir[k]
            reveal(x, y)
        }

    }

    private fun startTimer() {
        Clock.base = SystemClock.elapsedRealtime()
        Clock.start()
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

    private fun checkSafeNeighbour(i: Int, j: Int): Boolean {

        for (k in 0 until 8) {
            val x = i + xDir[k]
            val y = j + yDir[k]
            if (x in 0 until rows && y in 0 until columns) {
                if (!MineField[x][y].isMine) {
                    return true
                }
            }
        }

        return false
    }

    private fun setMines(i: Int, j: Int) {

        var mine: Int = 0
        while (mine < mines) {
            val x = Random(System.nanoTime()).nextInt(0, rows)
            val y = Random(System.nanoTime()).nextInt(0, columns)

            if (x != i && y != j) {
                if (!checkSafeNeighbour(x, y)) {
                    MineField[x][y].isMine = true
                    MineField[x][y].value = -1
                    mine++
                    plantValues(x, y)
                }
            }
        }
    }

    private fun plantValues(i: Int, j: Int) {
        for (k in 0 until 8) {
            val x = i + xDir[k]
            val y = j + yDir[k]
            if (x in 0 until rows && y in 0 until columns) {
                if (!MineField[x][y].isMine) {
                    MineField[x][y].value++
                }
            }
        }
    }

}