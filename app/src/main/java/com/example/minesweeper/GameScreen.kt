package com.example.minesweeper

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.minesweeper.enums.Difficulties
import com.example.minesweeper.enums.MineMode
import com.example.minesweeper.enums.Status
import kotlin.random.Random

class GameScreen : AppCompatActivity() {

    private var rows = 0
    private var columns = 0
    private var mines = 0
    private var flags = 0
    private var revealedCells = 0

    private var gameStatus = Status.NOT_STARTED
    private lateinit var mode: MineMode

    private lateinit var mineIndicator: TextView
    private lateinit var flagBombSwitch: ImageButton
    private lateinit var clock: Chronometer

    private lateinit var mineField: Array<Array<MineFieldCell>>
    private lateinit var board: LinearLayout
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
                button.setBackgroundResource(R.drawable.unrevealed_cell)
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
//                    if (gameStatus != Status.ONGOING) {
//                        showResult()
//                    }

                }
                id++
                mineField[i][j] = button


            }
            board.addView(linearLayout)
        }
    }

    private fun showResult() {


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
                            setBackgroundResource(R.drawable.unrevealed_cell)
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
                revealedCells++
                reveal(i, j)
            }
        }
    }

    private fun checkWin() = if (rows * columns - revealedCells == mines) {
        gameStatus = Status.WON
    } else {
        // Do Nothing.
    }

    private fun reveal(i: Int, j: Int) {
        if (i < 0 || i >= rows || j < 0 || j >= columns)
            return
        if (mineField[i][j].isRevealed || mineField[i][j].isFlagged || mineField[i][j].isMine)
            return

        mineField[i][j].isRevealed = true
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
                0 -> setBackgroundResource(R.drawable.blank)
                else -> {
                    text = value.toString()
                    textSize = 34f
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setBackgroundResource(R.drawable.blank)
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
        while (mine <= mines) {
            val x = Random(System.nanoTime()).nextInt(0, rows)
            val y = Random(System.nanoTime()).nextInt(0, columns)

            if (x != i && y != j) {
                if (checkSafeNeighbour(x, y)) {
                    mineField[x][y].isMine = true
                    mineField[x][y].value = -1
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
                if (!mineField[x][y].isMine) {
                    mineField[x][y].value++
                }
            }
        }
    }

}