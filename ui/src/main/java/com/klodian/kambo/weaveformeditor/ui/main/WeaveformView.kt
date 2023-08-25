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

private data class DrawablePoint(
    val xDraw: Float,
    val yDraw: Float,
    val xValue: Float,
    val yValue: Float
)


class WeaveformView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val verticalBarPaint = Paint().apply {
        color = Color.GRAY
    }
    private val dotPaint = Paint().apply {
        color = Color.GRAY
    }

    private var leftBarPositionX = -1f
    private var rightBarPositionX = -1f
    private var isLeftDragging = false
    private var isRightDragging = false
    private val barWidth = 5f

    private val linePaint = Paint().apply {
        isAntiAlias = true
        color = Color.YELLOW
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
    }
    private val pointPaint = Paint().apply {
        color = Color.BLACK
        textSize = 16f
    }

    private val coordinates = mutableListOf<Pair<Float, Float>>()
    private val drawablePoints = mutableListOf<DrawablePoint>()

    fun setCoordinates(newCoordinates: List<Pair<Float, Float>>) {
        if (newCoordinates != coordinates) {
            coordinates.clear()
            coordinates.addAll(newCoordinates)

            drawablePoints.clear()
            drawablePoints.addAll(getDrawablePoints())

            leftBarPositionX = width / 3f
            rightBarPositionX = 2 * width / 3f

            invalidate()
        }
    }

    fun getSelectedRangeValues(): List<Pair<Float, Float>> {
        return drawablePoints
            .distinctBy { it.xValue }
            .filter { it.xDraw in leftBarPositionX..rightBarPositionX }
            .map { coordinates[it.xValue.toInt()] }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (coordinates.isEmpty()) return

        assert(drawablePoints.size == coordinates.size * 2)

        renderWeave(canvas, drawablePoints)

        drawTextValues(canvas, drawablePoints)

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

    private fun renderWeave(canvas: Canvas, drawablePoints: List<DrawablePoint>) {
        val weaveFormPath = Path()
        weaveFormPath.moveTo(0f, drawablePoints.first().yDraw)

        drawablePoints.onEach { weaveFormPath.lineTo(it.xDraw, it.yDraw) }

        weaveFormPath.close()

        canvas.drawPath(weaveFormPath, linePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                if (isCloseToBar(leftBarPositionX, event) && !isCloseToBar(
                        rightBarPositionX,
                        event
                    )
                ) {
                    isLeftDragging = true
                } else if (!isCloseToBar(leftBarPositionX, event) && isCloseToBar(
                        rightBarPositionX,
                        event
                    )
                ) {
                    isRightDragging = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isLeftDragging) {
                    leftBarPositionX = event.x.coerceIn(0f, rightBarPositionX - barWidth - 50f)
                    invalidate()
                } else if (isRightDragging) {
                    rightBarPositionX =
                        event.x.coerceIn(leftBarPositionX + barWidth + 50f, width.toFloat())
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

    private fun drawTextValues(canvas: Canvas, drawablePoints: List<DrawablePoint>){
        drawablePoints.onEach {
            canvas.drawText(String.format("%.1f", it.yValue), it.xDraw, it.yDraw, pointPaint)
        }
    }

    private fun getDrawablePoints(): List<DrawablePoint> {
        val drawablePoints = mutableListOf<DrawablePoint>()
        val widthMultiplier = width / ((coordinates.size - 1).takeIf { it > 0 } ?: 1)

        var coordinatesIterator: Iterator<Pair<Float, Float>> = coordinates.iterator()
        var xIndex = 0
        var point = coordinatesIterator.next()

        drawablePoints.add(
            DrawablePoint(
                xIndex.toFloat() * widthMultiplier,
                point.first.absoluteValue * height / 2 + height / 2,
                xIndex.toFloat(),
                point.first.absoluteValue
            )
        )

        while (coordinatesIterator.hasNext()) {
            xIndex++
            point = coordinatesIterator.next()
            drawablePoints.add(
                DrawablePoint(
                    xIndex.toFloat() * widthMultiplier,
                    point.first.absoluteValue * height / 2 + height / 2,
                    xIndex.toFloat(),
                    point.first
                )
            )
        }

        coordinatesIterator = coordinates.reversed().iterator()

        while (coordinatesIterator.hasNext()) {
            point = coordinatesIterator.next()
            drawablePoints.add(
                DrawablePoint(
                    xIndex.toFloat() * widthMultiplier,
                    -point.second * height / 2 + height / 2,
                    xIndex.toFloat(),
                    point.second
                )
            )
            xIndex--
        }
        return drawablePoints
    }
}