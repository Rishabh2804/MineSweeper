package com.example.minesweeper.structured_templates

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

// Template for a button that can be used to represent a mineField cell
class MineFieldCell @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    var isMine = false
    var isRevealed = false
    var isFlagged = false
    var value = 0
}