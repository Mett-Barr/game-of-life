package com.moozy.gameoflife.ui.component

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moozy.gameoflife.ui.component.gamerule.*
import kotlinx.coroutines.delay

fun DrawScope.drawBoard(board: Board, cellStyle: CellStyle) {
    this.density
    val cellSizePx = cellStyle.size.toPx()
    val cornerRadiusPx = cellStyle.cornerRadius.toPx()
    val spacingPx = cellStyle.spacing.toPx()

    for (row in board) {
        for (cell in row) {
            if (cell.isAlive) {
                drawRoundRect(
                    cellStyle.color,
                    topLeft = Offset(
                        spacingPx / 2 + cell.x * (cellSizePx + spacingPx),
                        spacingPx / 2 + cell.y * (cellSizePx + spacingPx)
                    ),
                    size = Size(cellSizePx, cellSizePx),
                    cornerRadius = CornerRadius(cornerRadiusPx)
                )
            }
        }
    }
}

fun DrawScope.drawBoard(
    board: Board,
    cellStyle: CellStyle,
    cellStates: Array<Array<CellState>>,
    brighteningAlpha: Float,
    darkeningAlpha: Float
) {
    val cellSizePx = cellStyle.size.toPx()
    val cornerRadiusPx = cellStyle.cornerRadius.toPx()
    val spacingPx = cellStyle.spacing.toPx()

    val safeBrighteningAlpha = brighteningAlpha.coerceIn(0f, 1f)
    val safeDarkeningAlpha = darkeningAlpha.coerceIn(0f, 1f)

    for (i in board.indices) {
        for (j in board[i].indices) {
            val cell = board[i][j]


            val alpha = when (cellStates[i][j]) {
                CellState.BRIGHTENING -> safeBrighteningAlpha
                CellState.DARKENING -> safeDarkeningAlpha
                CellState.CONSTANT_BRIGHT -> 1f
                CellState.CONSTANT_DARK -> 0f
            }

            drawRoundRect(
                // 使用cellStyle.color.copy(alpha = alpha)就會失敗
                // 但使用cellStyle.color會正常顯示黑色
//                cellStyle.color,
                cellStyle.color.copy(alpha = alpha),
                topLeft = Offset(
                    spacingPx / 2 + cell.x * (cellSizePx + spacingPx),
                    spacingPx / 2 + cell.y * (cellSizePx + spacingPx)
                ),
                size = Size(cellSizePx, cellSizePx),
                cornerRadius = CornerRadius(cornerRadiusPx)
            )
        }
    }
}


@Preview
@Composable
fun GameOfLife() {
    val cellStyle = remember { CellStyle(30.dp, 4.dp, Color.Black) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val boardWidth =
        ((screenWidth - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()
    val boardHeight =
        ((screenHeight - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()

    val canvasWidth = boardWidth * cellStyle.size.value + (boardWidth + 1) * cellStyle.spacing.value
    val canvasHeight =
        boardHeight * cellStyle.size.value + (boardHeight + 1) * cellStyle.spacing.value

    val horizontalPadding = (screenWidth - canvasWidth.dp) / 2
    val verticalPadding = (screenHeight - canvasHeight.dp) / 2

    val (board, setBoard) = remember { mutableStateOf(initialBoard(boardWidth, boardHeight)) }

    LaunchedEffect(key1 = board) {
        while (true) {
            delay(200)
            setBoard(nextBoard(board))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .padding(start = horizontalPadding, top = verticalPadding)
                .size(canvasWidth.dp, canvasHeight.dp)
        ) {
            drawBoard(board, cellStyle)
        }
    }
}

@Composable
fun GameOfLife(board: Board) {
    val cellStyle = remember { CellStyle(30.dp, 4.dp, Color.Black) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val boardWidth =
        ((screenWidth - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()
    val boardHeight =
        ((screenHeight - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()

    val canvasWidth = boardWidth * cellStyle.size.value + (boardWidth + 1) * cellStyle.spacing.value
    val canvasHeight =
        boardHeight * cellStyle.size.value + (boardHeight + 1) * cellStyle.spacing.value

    val horizontalPadding = (screenWidth - canvasWidth.dp) / 2
    val verticalPadding = (screenHeight - canvasHeight.dp) / 2


    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .padding(start = horizontalPadding, top = verticalPadding)
                .size(canvasWidth.dp, canvasHeight.dp)
        ) {
            drawBoard(board, cellStyle)
        }
    }
}

@Preview
@Composable
fun GameOfLifeAnimation() {
    val cellStyle = remember { CellStyle(20.dp, 4.dp, Color.Black) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val boardWidth =
        ((screenWidth - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()
    val boardHeight =
        ((screenHeight - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()

    val canvasWidth = boardWidth * cellStyle.size.value + (boardWidth + 1) * cellStyle.spacing.value
    val canvasHeight =
        boardHeight * cellStyle.size.value + (boardHeight + 1) * cellStyle.spacing.value

    val horizontalPadding = (screenWidth - canvasWidth.dp) / 2
    val verticalPadding = (screenHeight - canvasHeight.dp) / 2

    val (board, setBoard) = remember { mutableStateOf(initialBoard(boardWidth, boardHeight)) }

    val cellStates by remember { mutableStateOf(Array(board.size) { Array(board[0].size) { CellState.CONSTANT_DARK } }) }

    // 使用infiniteTransition创建一个无限循环的动画
    val infiniteTransition = rememberInfiniteTransition()

    // 定义四种动画状态
    val brighteningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300),
            repeatMode = RepeatMode.Restart
        )
    )
    val darkeningAlpha = { 1f - brighteningAlpha }

    val newCycle by remember {
        derivedStateOf { brighteningAlpha >= 0.9999f }
    }

    val counter by remember { mutableStateOf(0) }

    LaunchedEffect(newCycle) {
        Log.d("!!!", "GameOfLifeAnimation: ${brighteningAlpha == 1f}")

        if (counter % 2 == 0) {
            val newBoard = nextBoard(board)

            for (i in board.indices) {
                for (j in 0 until board[i].size) {
                    cellStates[i][j] = when {
                        board[i][j].isAlive && !newBoard[i][j].isAlive -> CellState.DARKENING
                        !board[i][j].isAlive && newBoard[i][j].isAlive -> CellState.BRIGHTENING
                        newBoard[i][j].isAlive -> CellState.CONSTANT_BRIGHT
                        else -> CellState.CONSTANT_DARK
                    }
                }
            }

            setBoard(newBoard)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .padding(start = horizontalPadding, top = verticalPadding)
                .size(canvasWidth.dp, canvasHeight.dp)
        ) {
            drawBoard(board, cellStyle, cellStates, brighteningAlpha, darkeningAlpha())
        }
    }

//    LaunchedEffect(brighteningAlpha) {
//        Log.d("!!!", "brighteningAlpha == 1f: ${brighteningAlpha == 1f}")
//    }
    LaunchedEffect(newCycle) {
        Log.d("!!!", "brighteningAlpha == 1f: ${brighteningAlpha == 1f}")
    }
}


@Preview
@Composable
fun GameOfLifePreview() {
    GameOfLife()
}

