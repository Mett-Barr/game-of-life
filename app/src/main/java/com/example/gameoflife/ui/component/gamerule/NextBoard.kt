package com.example.gameoflife.ui.component.gamerule

fun nextBoard(board: Board): Board {
    val newBoard = Array(board.size) { Array(board[0].size) { Cell(0, 0, false) } }
    for (i in board.indices) {
        for (j in 0 until board[0].size) {
            val count = countLiveNeighbors(board, i, j)
            val newCell = Cell(
                i, j, when {
                    board[i][j].isAlive && count in 2..3 -> true
                    !board[i][j].isAlive && count == 3 -> true
                    else -> false
                }
            )
            newBoard[i][j] = newCell
        }
    }
    return newBoard
}