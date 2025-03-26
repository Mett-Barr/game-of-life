package com.moozy.gameoflife

import android.content.Context
import android.graphics.*
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import com.moozy.gameoflife.ui.component.gamerule.Board
import com.moozy.gameoflife.ui.component.gamerule.initialBoard
import com.moozy.gameoflife.ui.component.gamerule.nextBoard
import kotlinx.coroutines.*
import kotlin.random.Random

const val CELL_SIZE = 20
const val SPACING = 4
const val DURATION = 200L

class GameOfLifeWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return GameOfLifeEngine()
    }

    inner class GameOfLifeEngine : Engine() {
        private val coroutineScope = CoroutineScope(Dispatchers.IO)
        private var isRunning = false
        private lateinit var board: Board


        private val cellStyle = CellStyle(CELL_SIZE, SPACING, Color.GRAY)

        private var touchX: Int = 0
        private var touchY: Int = 0
        private var lastTouchX: Int = 0
        private var lastTouchY: Int = 0

        // 添加手势检测器
        private val gestureDetector =
            GestureDetector(this@GameOfLifeWallpaperService, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    // 记录当前触摸位置
                    lastTouchX = e.x.toInt() ?: 0
                    lastTouchY = e.y.toInt() ?: 0
                    return true
                }

                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    // 根据滑动距离计算出需要翻转的 cell
                    touchX -= distanceX.toInt()
                    touchY -= distanceY.toInt()
                    val x = ((touchX - cellStyle.spacing) / (cellStyle.size + cellStyle.spacing)).toInt()
                    val y = ((touchY - cellStyle.spacing) / (cellStyle.size + cellStyle.spacing)).toInt()

                    if (x in board.indices && y in 0 until board[0].size) {
                        // 将对应的 cell 状态翻转
                        board[x][y].isAlive = !board[x][y].isAlive

                        var canvas: Canvas? = null
                        try {
                            canvas = surfaceHolder.lockCanvas()
                            if (canvas != null) {
                                canvas.drawColor(Color.DKGRAY, PorterDuff.Mode.SRC)
                                drawGameOfLifeCanvas(canvas, board, cellStyle)
                            }
                        } finally {
                            if (canvas != null) {
                                surfaceHolder.unlockCanvasAndPost(canvas)
                            }
                        }
                    }
                    return true
                }
            })


        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                continueDraw()
            } else {
                stopDraw()
            }
        }

        private fun continueDraw() {
            coroutineScope.launch {
                drawGameOfLife()
            }
        }

        private fun stopDraw() {
            isRunning = false
            coroutineScope.coroutineContext.cancelChildren()
        }

        private suspend fun drawGameOfLife() {

            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val displayCompat = display.getRealMetrics(displayMetrics)
            } else {
                @Suppress("DEPRECATION")
                display.getMetrics(displayMetrics)
            }

            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            val boardWidth =
                ((screenWidth - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()
            val boardHeight =
                ((screenHeight - cellStyle.spacing * 2) / (cellStyle.size + cellStyle.spacing)).toInt()

            Log.d("!!!", "boardWidth = $boardWidth , boardHeight = $boardHeight")

            board = initialBoard(boardWidth, boardHeight)

            isRunning = true
            while (isRunning) {
                board = nextBoard(board)

                var canvas: Canvas? = null
                try {
                    canvas = surfaceHolder.lockCanvas()
                    if (canvas != null) {
                        canvas.drawColor(Color.DKGRAY, PorterDuff.Mode.SRC)

                        drawGameOfLifeCanvas(canvas, board, cellStyle)
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }

                delay(DURATION) // 控制绘制速度，可以根据需要调整
            }
        }


        override fun onTouchEvent(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 点击事件，计算点击位置对应的cell索引
                    val x = ((event.x - cellStyle.spacing) / (cellStyle.size + cellStyle.spacing)).toInt()
                    val y = ((event.y - cellStyle.spacing) / (cellStyle.size + cellStyle.spacing)).toInt()

                    if (x in board.indices && y in board[0].indices) {
                        // 隨機翻轉周圍的cell
                        for (i in x-2..x+2) {
                            for (j in y-2..y+2) {
                                if (i in board.indices && j in board[0].indices && (i != x || j != y)) {
                                    val randomBoolean = Random.nextBoolean()
                                    board[i][j].isAlive = randomBoolean
                                }
                            }
                        }

                        // 更新UI
                        surfaceHolder.lockCanvas()?.let { canvas ->
                            canvas.drawColor(Color.DKGRAY, PorterDuff.Mode.SRC)
                            drawGameOfLifeCanvas(canvas, board, cellStyle)
                            surfaceHolder.unlockCanvasAndPost(canvas)
                        }
                    }
                }
            }
        }

    }

    private fun drawGameOfLifeCanvas(canvas: Canvas, board: Board, cellStyle: CellStyle) {
        val paint = Paint()
        paint.color = cellStyle.color

        val screenWidth = canvas.width
        val screenHeight = canvas.height

        val boardWidth = board.size * (cellStyle.size + cellStyle.spacing)
        val boardHeight = board[0].size * (cellStyle.size + cellStyle.spacing)

        val offsetX = (screenWidth - boardWidth) / 2
        val offsetY = (screenHeight - boardHeight) / 2

        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j].isAlive) {
                    val left = i * (cellStyle.size + cellStyle.spacing) + offsetX
                    val top = j * (cellStyle.size + cellStyle.spacing) + offsetY
                    val right = left + cellStyle.size
                    val bottom = top + cellStyle.size
                    val rect = Rect(left, top, right, bottom)
                    canvas.drawRect(rect, paint)
                }
            }
        }
    }
}

data class CellStyle(
    val size: Int,
    val spacing: Int,
    val color: Int
)