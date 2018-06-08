package com.anwesh.uiprojects.linkedbicircularview

/**
 * Created by anweshmishra on 08/06/18.
 */

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

val LBC_NODES : Int = 5

class LinkedBiCircularView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : LBCRenderer = LBCRenderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
            paint.strokeCap = Paint.Cap.ROUND
            paint.style = Paint.Style.STROKE
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

    data class LinkedBiCircularShape (var i : Int) {

        private var curr : LBCNode = LBCNode(0)

        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            paint.color = Color.parseColor("#673AB7")
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class LBCRenderer(var view : LinkedBiCircularView) {

        private val animator : Animator = Animator(view)

        private val lcb : LinkedBiCircularShape = LinkedBiCircularShape(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lcb.draw(canvas, paint)
            animator.animate {
                lcb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lcb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LinkedBiCircularView{
            val view : LinkedBiCircularView = LinkedBiCircularView(activity)
            activity.setContentView(view)
            return view
        }
    }
}