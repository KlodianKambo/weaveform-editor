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
import androidx.core.view.doOnLayout
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

    private val selectedWeavePaint = Paint().apply {
        color = Color.BLUE
        alpha = 25
    }

    private val referenceLinePaint = Paint().apply {
        strokeWidth = 3f
        color = Color.BLACK
        alpha = 25
        style = Paint.Style.STROKE
    }

    private val weaveLinePaint = Paint().apply {
        isAntiAlias = true
        color = Color.CYAN
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 16f
    }

    private val barToBarDistance = 50f
    private val barTouchTolerance = 40f

    private var leftBarPositionX = -1f
    private var rightBarPositionX = -1f

    private var isLeftDragging = false
    private var isRightDragging = false

    private val barWidth = 10f

    private var requestedClick = false

    private val coordinates = mutableListOf<UiWeaveFrequency>()
    private val drawablePoints = mutableListOf<DrawablePoint>()
    private val circleWidth = 20f


    fun setCoordinates(newCoordinates: List<UiWeaveFrequency>) {
        if (newCoordinates != coordinates) {
            coordinates.clear()
            drawablePoints.clear()

            coordinates.addAll(newCoordinates)

            doOnLayout {
                calculateNewPointsAndInvalidate(coordinates)
            }
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


        // reference line
        canvas.drawLine(
            paddingLeft.toFloat().coerceAtLeast(0f),
            getDrawableHeight() / 2 + paddingTop,
            width.toFloat() - paddingEnd,
            getDrawableHeight() / 2 + paddingTop,
            referenceLinePaint
        )

        // reference border rect
        canvas.drawRect(
            paddingLeft.toFloat().coerceAtLeast(0f),
            paddingTop.toFloat().coerceAtLeast(0f),
            getDrawableWidth() + paddingLeft.toFloat(),
            getDrawableHeight() + paddingTop,
            referenceLinePaint
        )

        // Draw the selected slice between the bars
        canvas.drawRect(
            leftBarPositionX,
            paddingTop.toFloat().coerceAtLeast(0f),
            rightBarPositionX,
            height.toFloat() - paddingBottom,
            selectedWeavePaint
        )

        // Draw the left vertical bar
        canvas.drawRect(
            leftBarPositionX - barWidth / 2,
            paddingTop.toFloat().coerceAtLeast(0f),
            leftBarPositionX + barWidth / 2,
            height.toFloat() - paddingBottom,
            verticalBarPaint
        )

        // Draw the right vertical bar
        canvas.drawRect(
            rightBarPositionX - barWidth / 2,
            paddingTop.toFloat().coerceAtLeast(0f),
            rightBarPositionX + barWidth / 2,
            height.toFloat() - paddingBottom,
            verticalBarPaint
        )

        // Draw the fixed dot at the bottom
        canvas.drawCircle(
            leftBarPositionX,
            getDrawableHeight() + paddingTop - circleWidth,
            circleWidth,
            verticalBarPaint
        )

        canvas.drawCircle(
            rightBarPositionX,
            paddingTop + circleWidth,
            circleWidth,
            verticalBarPaint
        )
    }

    private fun renderWeave(canvas: Canvas, drawablePoints: List<DrawablePoint>) {
        val weaveFormPath = Path()
        weaveFormPath.moveTo(
            drawablePoints.first().xDraw,
            drawablePoints.first().yDraw
        )

        drawablePoints.onEach {
            weaveFormPath.lineTo(it.xDraw, it.yDraw)
        }

        weaveFormPath.close()

        canvas.drawPath(weaveFormPath, weaveLinePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    (isCloseToBar(leftBarPositionX, event) &&
                            !isCloseToBar(rightBarPositionX, event)) &&
                            (event.y in paddingTop.toFloat()..(getDrawableHeight() + paddingTop))

                    -> isLeftDragging = true

                    (!isCloseToBar(leftBarPositionX, event) &&
                            isCloseToBar(rightBarPositionX, event)) &&
                            (event.y in paddingTop.toFloat()..(getDrawableHeight() + paddingTop))

                    -> isRightDragging = true

                    else -> requestedClick = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isLeftDragging) {
                    leftBarPositionX =
                        event.x.coerceIn(
                            paddingLeft.toFloat().coerceAtLeast(0f),
                            rightBarPositionX - barWidth - barToBarDistance
                        )

                    invalidate()
                } else if (isRightDragging) {
                    rightBarPositionX =
                        event.x.coerceIn(
                            leftBarPositionX + barWidth + barToBarDistance,
                            width.toFloat() - paddingEnd
                        )

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
            canvas.drawText(String.format("%.1f", it.yValue), it.xDraw, it.yDraw, textPaint)
        }
    }

    private fun scaleCenterHeight(y: Float): Float {
        val currentHeightFloat = getDrawableHeight()
        return (y * currentHeightFloat / 2) + currentHeightFloat / 2
    }

    private fun calculateDrawablePoints(
        xIndex: Int,
        widthMultiplier: Float,
        uiWeaveFrequencies: List<UiWeaveFrequency>,
        outputList: MutableList<DrawablePoint>
    ) {

        if (xIndex == uiWeaveFrequencies.size || xIndex < 0) return

        // add the min values
        outputList.add(
            DrawablePoint(
                xDraw = xIndex.toFloat() * widthMultiplier + paddingLeft,
                yDraw = scaleCenterHeight(uiWeaveFrequencies[xIndex].minValue.absoluteValue) + paddingTop,
                xValue = xIndex.toFloat(),
                yValue = uiWeaveFrequencies[xIndex].minValue
            )
        )

        calculateDrawablePoints(xIndex + 1, widthMultiplier, uiWeaveFrequencies, outputList)

        // add the max values
        outputList.add(
            DrawablePoint(
                xDraw = xIndex.toFloat() * widthMultiplier + paddingLeft,
                yDraw = scaleCenterHeight(-(uiWeaveFrequencies[xIndex].maxValue.absoluteValue)) + paddingTop,
                xValue = xIndex.toFloat(),
                yValue = uiWeaveFrequencies[xIndex].maxValue
            )
        )
    }

    private fun getDrawableHeight() = (height - paddingTop - paddingBottom)
        .coerceAtLeast(0)
        .toFloat()

    private fun getDrawableWidth() = (width - paddingLeft - paddingRight)
        .coerceAtLeast(0)
        .toFloat()


    private fun calculateNewPointsAndInvalidate(newCoordinates: List<UiWeaveFrequency>) {
        calculateDrawablePoints(
            xIndex = 0,
            widthMultiplier = getDrawableWidth() /
                    (newCoordinates.size - 1).coerceAtLeast(1),
            uiWeaveFrequencies = newCoordinates,
            outputList = drawablePoints
        )

        // TODO add proportions
        leftBarPositionX = 0f + paddingLeft
        rightBarPositionX = width.toFloat() - paddingEnd

        invalidate()
    }
}