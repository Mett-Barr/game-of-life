package com.moozy.gameoflife.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun GameOfLife() {
    var board by remember { mutableStateOf(Array(40) { BooleanArray(40) }) }
    var isPlaying by remember { mutableStateOf(false) }

    Column {
        // 游戏板的绘制
        Canvas(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            drawBoard(board)
        }

        // 游戏控制按钮
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            Button(onClick = {
                isPlaying = !isPlaying
            }) {
                Text(if (isPlaying) "Pause" else "Start")
            }
            Spacer(Modifier.width(16.dp))
            Button(onClick = {
//                resetBoard(board)
            }) {
                Text("Reset")
            }
        }

        // 用户点击事件的处理
        Canvas(modifier = Modifier.fillMaxWidth().height(320.dp)
            .pointerInteropFilter {
                if (isPlaying) {
                    false
                } else {
                    val x = (it.x / 8).toInt()
                    val y = (it.y / 8).toInt()
                    if (x >= 0 && x < board.size && y >= 0 && y < board[0].size) {
                        board[x][y] = !board[x][y]
                    }
                    true
                }
            }) {}
    }

    // 游戏循环
    LaunchedEffect(isPlaying) {

        val random = Random
        for (i in board.indices) {
            for (j in 0 until board[0].size) {
                board[i][j] = random.nextBoolean()
            }
        }

        while (isPlaying) {
            board = nextBoard(board)
            delay(50)
        }
    }
}

// 绘制游戏板
fun DrawScope.drawBoard(board: Array<BooleanArray>) {
    for (i in board.indices) {
        for (j in 0 until board[0].size) {
            if (board[i][j]) {
                drawRect(Color.Black, Offset(i * 18f, j * 18f), size = Size(18f, 18f))
            }
        }
    }
}

// 计算下一个周期的状态
fun nextBoard(board: Array<BooleanArray>): Array<BooleanArray> {
    val newBoard = Array(board.size) { BooleanArray(board[0].size) }
    for (i in board.indices) {
        for (j in 0 until board[0].size) {
            val count = countLiveNeighbors(board, i, j)
            newBoard[i][j] = when {
                board[i][j] && count in 2..3 -> true
                !board[i][j] && count == 3 -> true
                else -> false
            }
        }
    }
    return newBoard
}

// 计算某个细胞周围的存活邻居数量
fun countLiveNeighbors(board: Array<BooleanArray>, x: Int, y: Int): Int {
    var count = 0
    for (i in -1..1) {
        for (j in -1..1) {
            if (i == 0 && j == 0) continue
            val row = y + i
            val col = x + j
            if (row < 0 || row >= board[0].size || col < 0 || col >= board.size) continue
            if (board[col][row]) count++
        }
    }
    return count
}