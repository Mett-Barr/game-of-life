package com.moozy.gameoflife.ui.component.gamerule

fun countLiveNeighbors(board: Board, x: Int, y: Int): Int {
    var count = 0
    for (i in -1..1) {
        for (j in -1..1) {
            if (i == 0 && j == 0) continue
            val row = y + i
            val col = x + j
            if (row < 0 || row >= board[0].size || col < 0 || col >= board.size) continue
            if (board[col][row].isAlive) count++
        }
    }
    return count
}