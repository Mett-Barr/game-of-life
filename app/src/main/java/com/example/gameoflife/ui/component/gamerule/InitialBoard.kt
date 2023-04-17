package com.example.gameoflife.ui.component.gamerule

fun initialBoard(width: Int, height: Int): Board {
    val board = Array(width) { Array(height) { Cell() } }
    for (i in 0 until width) {
        for (j in 0 until height) {
            board[i][j] = Cell(i, j, Math.random() > 0.5)
        }
    }
    return board
}