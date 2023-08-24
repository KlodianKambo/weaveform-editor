package com.klodian.kambo.weaveformeditor.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue

class WeaveformView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val verticalBarPaint = Paint().apply {
        color = Color.BLACK
    }
    private val dotPaint = Paint().apply {
        color = Color.RED
    }

    private var leftBarPositionX = -1f
    private var rightBarPositionX = -1f
    private var isLeftDragging = false
    private var isRightDragging = false
    private val barWidth = 5f

    private val linePaint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
    }
    private val pointPaint = Paint().apply {
        color = Color.BLACK
        textSize = 16f
    }

    private val coordinates = mutableListOf<Pair<Float, Float>>()

    fun setCoordinates(newCoordinates: List<Pair<Float, Float>>) {
        coordinates.clear()
        coordinates.addAll(newCoordinates)
        invalidate()
        weaveRendered = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

    }

    val p = Path()

    private var weaveRendered = false
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(leftBarPositionX == -1f || rightBarPositionX == -1f){
            leftBarPositionX = width / 3f  // Initial positions
            rightBarPositionX = 2 * width / 3f
        }

        canvas?.apply {
            if (coordinates.isEmpty()) return
            renderWeave(canvas)

            // Draw the left vertical bar
            canvas.drawRect(
                leftBarPositionX - barWidth / 2,
                0f,
                leftBarPositionX + barWidth / 2,
                height.toFloat(),
                verticalBarPaint
            )

            // Draw the right vertical bar
            canvas.drawRect(
                rightBarPositionX - barWidth / 2,
                0f,
                rightBarPositionX + barWidth / 2,
                height.toFloat(),
                verticalBarPaint
            )

            // Draw the fixed dot at the bottom
            canvas.drawCircle(leftBarPositionX, height.toFloat() - 15f, 15f, dotPaint)
            canvas.drawCircle(rightBarPositionX, 15f, 15f, dotPaint)
        }
    }

    private fun renderWeave(canvas: Canvas){
        val listP = getDrawablePoints()
        assert(listP.size == coordinates.size * 2)

        p.moveTo(0f, listP.first().yDraw)

        listP.onEach { p.lineTo(it.xDraw,it.yDraw) }

        p.close()

        canvas.drawPath(p,linePaint)

        listP.onEach {
            canvas.drawText(String.format("%.2f", it.yValue), it.xDraw, it.yDraw, pointPaint)
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isCloseToBar(leftBarPositionX, event) && !isCloseToBar(rightBarPositionX, event)) {
                    isLeftDragging = true
                } else if (!isCloseToBar(leftBarPositionX, event) && isCloseToBar(rightBarPositionX, event)) {
                    isRightDragging = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLeftDragging) {
                    leftBarPositionX = event.x.coerceIn(0f, rightBarPositionX - barWidth - 50f)
                    invalidate()
                } else if (isRightDragging) {
                    rightBarPositionX = event.x.coerceIn(leftBarPositionX + barWidth + 50f, width.toFloat())
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                isLeftDragging = false
                isRightDragging = false
            }
        }
        return true
    }

    private fun isCloseToBar(barX: Float, event: MotionEvent): Boolean {
        return Math.abs(event.x - barX) <= 15f
    }

    private data class DrawablePoint(
        val xDraw: Float,
        val yDraw: Float,
        val xValue: Float,
        val yValue: Float
    )

    private fun getDrawablePoints(): List<DrawablePoint> {
        val points = mutableListOf<DrawablePoint>()
        val s = width / (coordinates.size - 1)

        var it: Iterator<Pair<Float, Float>> = coordinates.iterator()
        var c = 0
        var n = it.next()

        points.add(
            DrawablePoint(
                c.toFloat() * s,
                n.first.absoluteValue * height / 2 + height / 2,
                c.toFloat(),
                n.first.absoluteValue
            )
        )


        while (it.hasNext()) {
            c++
            n = it.next()
            points.add(
                DrawablePoint(
                    c.toFloat() * s,
                    n.first.absoluteValue * height / 2 + height / 2,
                    c.toFloat(),
                    n.first
                )
            )
        }

        it = coordinates.reversed().iterator()

        while (it.hasNext()) {
            n = it.next()
            points.add(
                DrawablePoint(
                    c.toFloat() * s,
                    - n.second * height / 2 + height / 2,
                    c.toFloat(),
                    n.second
                )
            )

            c--
        }

        return points
    }
}