package com.anwesh.uiprojects.linkedbicircularview

/**
 * Created by anweshmishra on 08/06/18.
 */

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

val LBC_NODES : Int = 5

class LinkedBiCircularView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var j : Int = 0, var dir : Float = 0f, var prevScale : Float = 0f) {

        val scales : Array<Float> = arrayOf(0f, 0f, 0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += 0.1f * dir
            if (Math.abs(prevScale - prevScale) > 1) {
                scales[j]  = prevScale + dir
                if (Math.abs(scales[j] - prevScale) > 1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LBCNode(var i : Int, val state : State = State()) {

        var next : LBCNode? = null

        var prev : LBCNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < LBC_NODES - 1) {
                next = LBCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val gap : Float = (0.9f * w / LBC_NODES)
            paint.strokeWidth = Math.min(w, h) / 50
            canvas.save()
            canvas.translate(w/20 + i * gap, h / 2)
            for (i in 0..1) {
                canvas.save()
                canvas.translate(gap * state.scales[1 + (i + 1) % 2], 0f)
                canvas.drawArc(RectF(0f, -gap / 2, gap, gap / 2), 0f, 360f * state.scales[0] * (1 - state.scales[3]), false, paint)
                canvas.restore()
            }
            canvas.restore()
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : LBCNode {
            var curr : LBCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

}