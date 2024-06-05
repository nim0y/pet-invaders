package com.example.spaceinvaders

import android.app.Activity
import android.graphics.Point
import android.os.Bundle

class MainActivity : Activity() {
    private var invadersView: InvadersView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        invadersView = InvadersView(this,size)

        setContentView(invadersView)
    }

    override fun onResume() {
        super.onResume()
        invadersView?.resume()
    }

    override fun onPause() {
        super.onPause()
        invadersView?.onPause()
    }
}