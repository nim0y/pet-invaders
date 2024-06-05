package com.example.spaceinvaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF

class Ship(context: Context, private val screenX: Int, screenY: Int) {

    var bitmap: Bitmap = BitmapFactory.decodeResource(
        context.resources,
        R.drawable.playership
    )

    val widthS = screenX / 20f
    private val heightS = screenY / 20f

    val position = RectF(
        screenX / 2f,
        screenY - heightS,
        screenX / 2 + widthS,
        screenY.toFloat()
    )

    val speed = 450f

    var move = 0

    companion object {
        const val stopped = 0
        const val left = 1
        const val right = 2
    }

    init {
        bitmap = Bitmap.createScaledBitmap(bitmap, widthS.toInt(), heightS.toInt(), false)
    }

    fun update(fps: Long) {
        if (move == left && position.left > 0) {
            position.left -= speed / fps
        } else if (move == right && position.left < screenX - widthS) {
            position.left += speed / fps
        }
        position.right = position.left + widthS
    }
}