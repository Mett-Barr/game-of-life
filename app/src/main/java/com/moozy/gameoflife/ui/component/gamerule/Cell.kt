package com.moozy.gameoflife.ui.component.gamerule

// 數據類型用來表示單個細胞
data class Cell(val x: Int = 0, val y: Int = 0, var isAlive: Boolean = false)