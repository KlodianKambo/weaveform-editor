package com.klodian.kambo.weaveformeditor.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
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

    private val selectedBarPaint = Paint().apply {
        color = Color.BLUE  // You can set your preferred tint color here
        alpha = 25
    }

    private val barToBarDistance = 50f
    private val barTouchTolerance = 40f

    private var leftBarPositionX = -1f
    private var rightBarPositionX = -1f
    private var isLeftDragging = false
    private var isRightDragging = false
    private val barWidth = 10f
    private var requestedClick = false

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

    private val coordinates = mutableListOf<UiWeaveFrequency>()
    private val drawablePoints = mutableListOf<DrawablePoint>()

    fun setCoordinates(newCoordinates: List<UiWeaveFrequency>) {
        if (newCoordinates != coordinates) {
            coordinates.clear()
            drawablePoints.clear()

            if (newCoordinates.isNotEmpty()) {
                coordinates.addAll(newCoordinates)
                drawablePoints.addAll(getDrawablePoints(newCoordinates))
            }

            leftBarPositionX = 0f
            rightBarPositionX = width.toFloat()

            invalidate()
        }
    }

    fun getSelectedRangeValues(): List<UiWeaveFrequency> {
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

        // Draw the selected slice between the bars
        canvas.drawRect(
            leftBarPositionX, 0f, rightBarPositionX, height.toFloat(),
            selectedBarPaint
        )
    }

    private fun renderWeave(canvas: Canvas, drawablePoints: List<DrawablePoint>) {
        val weaveFormPath = Path()
        weaveFormPath.moveTo(0f, drawablePoints.first().yDraw)

        drawablePoints.onEach { weaveFormPath.lineTo(it.xDraw, it.yDraw) }

        weaveFormPath.close()

        canvas.drawPath(weaveFormPath, linePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                when {
                    (isCloseToBar(leftBarPositionX, event) &&
                            !isCloseToBar(rightBarPositionX, event)) -> isLeftDragging = true

                    (!isCloseToBar(leftBarPositionX, event) &&
                            isCloseToBar(rightBarPositionX, event)) -> isRightDragging = true

                    else -> requestedClick = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isLeftDragging) {
                    leftBarPositionX = event.x.coerceIn(0f, rightBarPositionX - barWidth - barToBarDistance)
                    invalidate()
                } else if (isRightDragging) {
                    rightBarPositionX =
                        event.x.coerceIn(leftBarPositionX + barWidth + barToBarDistance, width.toFloat())
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP -> {

                isLeftDragging = false
                isRightDragging = false

                if (requestedClick) {
                    requestedClick = false
                    performClick()
                }
            }
        }
        return true
    }

    private fun isCloseToBar(barX: Float, event: MotionEvent): Boolean {
        return abs(event.x - barX) <= barTouchTolerance
    }

    private fun drawTextValues(canvas: Canvas, drawablePoints: List<DrawablePoint>) {
        drawablePoints.onEach {
            canvas.drawText(String.format("%.1f", it.yValue), it.xDraw, it.yDraw, pointPaint)
        }
    }

    private fun getDrawablePoints(uiWeaveFrequencies: List<UiWeaveFrequency>): List<DrawablePoint> {
        val drawablePoints = mutableListOf<DrawablePoint>()
        val widthMultiplier = width / ((uiWeaveFrequencies.size - 1).takeIf { it > 0 } ?: 1)

        var coordinatesIterator: Iterator<UiWeaveFrequency> = uiWeaveFrequencies.iterator()
        var xIndex = 0
        var point = coordinatesIterator.next()

        drawablePoints.add(
            DrawablePoint(
                xIndex.toFloat() * widthMultiplier,
                scaleCenterHeight(point.minValue.absoluteValue),
                xIndex.toFloat(),
                point.minValue.absoluteValue
            )
        )

        while (coordinatesIterator.hasNext()) {
            xIndex++
            point = coordinatesIterator.next()
            drawablePoints.add(
                DrawablePoint(
                    xIndex.toFloat() * widthMultiplier,
                    scaleCenterHeight(point.minValue.absoluteValue),
                    xIndex.toFloat(),
                    point.minValue
                )
            )
        }

        coordinatesIterator = uiWeaveFrequencies.reversed().iterator()

        while (coordinatesIterator.hasNext()) {
            point = coordinatesIterator.next()
            drawablePoints.add(
                DrawablePoint(
                    xIndex.toFloat() * widthMultiplier,
                    scaleCenterHeight(-point.maxValue),
                    xIndex.toFloat(),
                    point.maxValue
                )
            )
            xIndex--
        }
        return drawablePoints
    }

    private fun scaleCenterHeight(y: Float): Float {
        return y * height / 2 + height / 2
    }
}