package com.example.smartfit.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.DirectionsBike
import androidx.compose.material.icons.automirrored.twotone.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.twotone.*
import androidx.compose.material.icons.automirrored.twotone.List
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 全局统一管理 SmartFit 用到的 icons
 */
object SmartFitIcons {

    // Bottom bar
    val Home     = Icons.TwoTone.Home
    val Activity = Icons.AutoMirrored.TwoTone.List // 或 AutoMirrored.TwoTone.List，看你要不要镜像
    val Tips     = Icons.TwoTone.Lightbulb
    val Profile  = Icons.TwoTone.Person
    val Add: ImageVector = Icons.Filled.Add
    val Notifications = Icons.TwoTone.Notifications

    // Activity list
    val Running  = Icons.AutoMirrored.TwoTone.DirectionsRun
    val Cycling  = Icons.AutoMirrored.TwoTone.DirectionsBike
    val Food     = Icons.TwoTone.RoomService
    val Drink    = Icons.TwoTone.LocalDrink

    // 其它需要的可以慢慢加
}
