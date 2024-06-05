package com.example.spaceinvaders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RectF
import android.view.MotionEvent
import android.view.SurfaceView
import android.widget.Toast

class InvadersView(context: Context, private val size: Point) : SurfaceView(context),
    Runnable {
    private val gameThread = Thread(this)
    var playerShip: Ship = Ship(context, size.x, size.y)
    val invaders = ArrayList<Invader>()
    private val bricks = ArrayList<DefenceBrick>()
    private var numBricks: Int = 0
    private var playerBullet = Bullet(size.y, 1200f, 40f)
    private val invadersBullets = ArrayList<Bullet>()
    private var nextBullet = 0
    private val maxInvaderBullets = 10
    var numInvader = 0
    var play = true
    var pause = true
    var canvas: Canvas = Canvas()
    val paint = Paint()
    var score = 0
    var waves = 1
    var lives = 3
    var hScore = 0
    var menace: Long = 1000
    var lastMenace = System.currentTimeMillis()
    var uhOrOh = false
    private fun getReady() {
        Invader.numberOfInvaders = 0
        numInvader = 0
        for (column in 0..10) {
            for (row in 0..5) {
                invaders.add(Invader(context, row, column, size.x, size.y))
                numInvader++
            }
        }
        numBricks = 0
        for (shelterNumber in 0..4) {
            for (column in 0..18) {
                for (row in 0..8) {
                    bricks.add(
                        DefenceBrick(
                            row,
                            column,
                            shelterNumber,
                            size.x,
                            size.y
                        )
                    )

                    numBricks++
                }
            }
        }
        for (i in 0 until maxInvaderBullets) {
            invadersBullets.add(Bullet(size.y))
        }
    }

    override fun run() {
        var fps: Long = 0
        while (play) {
            val startFrame = System.currentTimeMillis()
            if (pause) {
                update(fps)
            }
            draw()
            val timeFrame = System.currentTimeMillis() - startFrame
            if (timeFrame >= 1) {
                fps = 1000 / timeFrame
            }
        }
    }

    fun update(fps: Long) {
        playerShip.update(fps)
        var hit = false
        var lost = false


        for (enemy in invaders) {
            if (enemy.isVisible) {
                enemy.update(fps)
                if (enemy.takeAim(
                        playerShip.position.left,
                        playerShip.widthS,
                        waves
                    )
                ) {

                    if (invadersBullets[nextBullet].shoot(
                            enemy.position.left
                                    + enemy.width / 2,
                            enemy.position.top, playerBullet.down
                        )
                    ) {

                        nextBullet++

                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0
                        }
                    }
                }
                if (enemy.position.left > size.x - enemy.width || enemy.position.left < 0) {
                    hit = true
                }
            }
        }
        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                bullet.update(fps)
            }
        }


        if (hit) {
            for (enemy in invaders) {
                enemy.dropDownAndReverse(waves)
                if (enemy.position.bottom >= size.y && enemy.isVisible) {
                    lost = true
                }
            }
        }
        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive = false
        }

        for (bullet in invadersBullets) {
            if (bullet.position.top > size.y) {
                bullet.isActive = false
            }
        }

        if (playerBullet.isActive) {
            for (invader in invaders) {
                if (invader.isVisible) {
                    if (RectF.intersects(
                            playerBullet.position,
                            invader.position
                        )
                    ) {
                        invader.isVisible = false
                        playerBullet.isActive = false
                        Invader.numberOfInvaders--
                        score += 10
                        if (score > hScore) {
                            hScore = score
                        }

                        if (Invader.numberOfInvaders == 0) {
                            pause = true
                            lives++
                            invaders.clear()
                            bricks.clear()
                            invadersBullets.clear()
                            getReady()
                            waves++
                            break
                        }

                        break
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                for (brick in bricks) {
                    if (brick.isVisible) {
                        if (RectF.intersects(bullet.position, brick.position)) {
                            bullet.isActive = false
                            brick.isVisible = false
                        }
                    }
                }
            }
        }

        if (playerBullet.isActive) {
            for (brick in bricks) {
                if (brick.isVisible) {
                    if (RectF.intersects(playerBullet.position, brick.position)) {
                        playerBullet.isActive = false
                        brick.isVisible = false
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                if (RectF.intersects(playerShip.position, bullet.position)) {
                    bullet.isActive = false
                    lives--

                    if (lives == 0) {
                        lost = true
                        break
                    }
                }
            }
        }

        if (lost) {
            pause = true
            lives = 3
            score = 0
            waves = 1
            invaders.clear()
            bricks.clear()
            invadersBullets.clear()
            getReady()
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.argb(255, 0, 0, 0))
            paint.color = Color.argb(255, 0, 255, 0)
            canvas.drawBitmap(
                playerShip.bitmap,
                playerShip.position.left,
                playerShip.position.top,
                paint
            )
            for (enemy in invaders) {
                if (enemy.isVisible) {
                    if (uhOrOh) {
                        Invader.bitmap1?.let {
                            canvas.drawBitmap(
                                it,
                                enemy.position.left,
                                enemy.position.top,
                                paint
                            )
                        }
                    } else {
                        Invader.bitmap2?.let {
                            canvas.drawBitmap(
                                it,
                                enemy.position.left,
                                enemy.position.top,
                                paint
                            )
                        }
                    }
                }
            }
            for (brick in bricks) {
                if (brick.isVisible) {
                    canvas.drawRect(brick.position, paint)
                }
            }

            if (playerBullet.isActive) {
                canvas.drawRect(playerBullet.position, paint)
            }

            for (bullet in invadersBullets) {
                if (bullet.isActive) {
                    canvas.drawRect(bullet.position, paint)
                }
            }
            paint.color = Color.argb(255, 255, 255, 255)
            paint.textSize = 70f
            canvas.drawText("Score: $score   Lives: $lives", 20f, 75f, paint)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun resume() {
        play = true
        getReady()
        gameThread.start()
    }

    fun onPause() {
        play = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Toast.makeText(context, "Exeption", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                pause = true

                if (motionEvent.y > size.y - size.y / 8) {
                    if (motionEvent.x > size.x / 2) {
                        playerShip.move = Ship.right
                    } else {
                        playerShip.move = Ship.left
                    }

                }

                if (motionEvent.y < size.y - size.y / 8) {
                    if (playerBullet.shoot(
                            playerShip.position.left + playerShip.widthS / 2f,
                            playerShip.position.top,
                            playerBullet.up
                        )
                    ) {
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                if (motionEvent.y > size.y - size.y / 10) {
                    playerShip.move = Ship.stopped
                }
            }

        }
        return true
    }
}