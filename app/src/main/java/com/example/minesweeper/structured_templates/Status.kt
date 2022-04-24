package com.example.minesweeper.structured_templates

// Game status enum
enum class Status {
    NOT_STARTED, // Game has not started yet
    ONGOING, // Game is ongoing
    LOST, // Player lost the game by revealing a mine
    WON // All safe cells have been revealed, leaving only mined ones
}