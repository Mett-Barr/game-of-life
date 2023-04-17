package com.example.gameoflife.ui.component.gamerule

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

// 用來表示細胞的大小、圓角和顏色
data class CellStyle(
    val size: Dp,
    val cornerRadius: Dp,
    val color: Color,
    val spacing: Dp = cornerRadius / 2
)